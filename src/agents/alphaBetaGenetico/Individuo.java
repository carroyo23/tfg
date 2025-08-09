package agents.alphaBetaGenetico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

public  class Individuo implements Comparable<Individuo>{
	// la forma de este array sera la siguiente: [valor_horizontal, valor_vertical, valor_kill, valor_monedas]
	public float[] genoma = null;
	public static final int NUM_GENES = 4;
	public static final int [] NORMALIZER = {1000, 100, 100, 100}; // la puntuacion por avanzar es 1 orden de magnitud mayor que el resto
	
	public Resumen resultados = null;
	public float fitness = -1;
	
	public final float PESO_NIVEL = 100f;
	public final float PESO_PORCENTAJE = 70f;
	public final float PESO_TIEMPO = 30f;
	public final float PESO_MONEDAS = 5f;
	
	public Individuo() {
		genoma = null;
		resultados = null;
		fitness = -1;
	}
	
	public Individuo(float[] nuevo_genoma) {
		genoma = nuevo_genoma.clone();
		resultados = null;
		fitness = -1;
	}
	
	public Individuo(final Individuo otro) {
		genoma = otro.genoma.clone();
		fitness = otro.fitness;
		resultados = new Resumen(otro.resultados);
	}
	
	
	public float getFitness() {
		
		// primero normalizo todos los valores para que esten en la misma escala
		float niveles_superados_norm = Math.max(0f, Math.min(1f, resultados.niveles_superados / 15.0f)); // el maximo son 15 niveles
		float porcentaje_superado_norm = Math.max(0f, Math.min(1f, resultados.porcentaje_superado / 15.0f)); // el maximo son 15 niveles
		float tiempo_restante_norm = Math.max(0f, Math.min(1f, resultados.tiempo_restante / 20000.0f)); // el maximo de tiempo son 20 segundos (20 mil milisegundos)
		float monedas_conseguidas_norm = Math.max(0f, Math.min(1f, resultados.monedas_conseguidas / 1000.0f)); // ningun nivel tendra 1000 monedas
		
		// actualizo el fitness
		fitness = (niveles_superados_norm * PESO_NIVEL) + (porcentaje_superado_norm * PESO_PORCENTAJE) + (tiempo_restante_norm * PESO_TIEMPO) + (monedas_conseguidas_norm * PESO_MONEDAS);
		
		return fitness;
	}
	
	public void generaRandomSol(Random generador_random) {
		// anulo los resultados que hubiera
		resultados = null;
		fitness = -1;
		
		genoma = new float[NUM_GENES];
		
		for (int i = 0; i < NUM_GENES; i++) {
			genoma[i] = generador_random.nextFloat();
		}
		
		// actualizo el fitness y los resultados
		resultados = evaluaIndividuo();
		getFitness();
	}
	
	public void actualizaFitness() {
		resultados = evaluaIndividuo();
		getFitness();
	}
	
	public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }
	
	public Resumen evaluaIndividuo() {
    	Resumen a_devolver = new Resumen(); // [num_niveles_pasados, porcentaje_total_pasado, tiempo_restante, monedas_conseguidas]
    	
    	float[] genes = genoma.clone();
    	
    	// denormalizo cada gen
    	for (int i = 0; i < NUM_GENES; i++) {
    		genes[i] = genes[i] * NORMALIZER[i];
    	}
    	
    	int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores-1);
        //System.out.println(cores);

        try {
          List<Callable<MarioResult>> tareas = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> (Callable<MarioResult>) () -> {
              MarioGame mg = new MarioGame();	
              MarioAgent agent = new agents.alphaBetaGenetico.Agent(genes[0], genes[1], genes[2], genes[3]);
              String level = getLevel("./levels/original/lvl-" + i + ".txt");
              return mg.runGame(agent, level, 20, 0, false);
            })
            .collect(Collectors.toList());

          // invokeAll sí arroja InterruptedException (hay que tratarlo)
          List<Future<MarioResult>> futuros = pool.invokeAll(tareas);

          int pasa = 0;
          double sumaCompletion = 0.0;
          int monedasConseguidas = 0;
          double tiempoRestante = 0.0;

          for (Future<MarioResult> f : futuros) {
            try {
              MarioResult r = f.get(); // también arroja InterruptedException
              if (r.getGameStatus() == GameStatus.WIN) {
            	  pasa++;
              }
              sumaCompletion += r.getCompletionPercentage();
              monedasConseguidas += r.getCurrentCoins();
              tiempoRestante += r.getRemainingTime();
              //printResults(r);
              
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              System.err.println("Hilo interrumpido mientras esperaba resultados");
              // quizá quieras salir del ciclo
              break;
            } catch (ExecutionException ee) {
              System.err.println("Falló nivel: " + ee.getCause());
            }
          }
          a_devolver.niveles_superados = pasa;
          a_devolver.porcentaje_superado = (float)sumaCompletion;
          a_devolver.tiempo_restante = (float)tiempoRestante;
          a_devolver.monedas_conseguidas = monedasConseguidas;
          
          //System.out.println("Suma de porcentaje pasado: " + sumaCompletion);
          //System.out.format("Pasados %2d/15 → %.1f%%\n", pasa, sumaCompletion);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          System.err.println("El hilo principal fue interrumpido durante invokeAll");
        } finally {
          pool.shutdown();
          try {
            if (!pool.awaitTermination(5, TimeUnit.MINUTES)) {
              pool.shutdownNow();
              System.err.println("Pool no terminó en tiempo — apagado forzado");
            }
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción mientras cerraba el pool");
          }
        }
        
        return a_devolver;
    }
	
	public Resumen evaluaIndividuoMCTS(final int veces) {
    	Resumen a_devolver = new Resumen(); // [num_niveles_pasados, porcentaje_total_pasado, tiempo_restante, monedas_conseguidas]
    	
    	int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores-1);

        try {
          List<Callable<MarioResult>> tareas = new ArrayList<>();
          
          // lo ejecutare veces para sacar medias de cada individuo
          for (int rep = 0; rep < veces; rep++) {
        	  float[] genes = genoma.clone();
          	
          	// denormalizo cada gen
          	for (int i = 0; i < NUM_GENES; i++) {
          		genes[i] = genes[i] * NORMALIZER[i];
          	}
          	
          	for (int i = 1; i < 15; i++) {
          		// los hago constantes para evitar condiciones de carrera
          		final int NIVEL_CONST = i;
          		final float[] GENES_CONST = genes.clone();
          		
          		tareas.add(() -> {
          			MarioGame mg = new MarioGame();
          			MarioAgent agent = new agents.alphaBetaGenetico.Agent(GENES_CONST[0], GENES_CONST[1], GENES_CONST[2], GENES_CONST[3]);
          			String nivel = getLevel("./levels/original/lvl-" + NIVEL_CONST + ".txt");
          			return mg.runGame(agent, nivel, 20, 0, false);
          		});
          	}
          }

          List<Future<MarioResult>> futuros = pool.invokeAll(tareas);

          int pasa = 0;
          double sumaCompletion = 0.0;
          int monedasConseguidas = 0;
          double tiempoRestante = 0.0;

          for (Future<MarioResult> f : futuros) {
            try {
              MarioResult r = f.get();
              if (r.getGameStatus() == GameStatus.WIN) {
            	  pasa++;
            	  tiempoRestante += r.getRemainingTime(); // solo sumare el tiempo si ha ganado
              }
              sumaCompletion += r.getCompletionPercentage();
              monedasConseguidas += r.getCurrentCoins();
              
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              System.err.println("Hilo interrumpido mientras esperaba resultados");
              break;
            } catch (ExecutionException ee) {
              System.err.println("Fallo tarea: " + ee.getCause());
            }
          }
          a_devolver.niveles_superados = pasa;
          a_devolver.porcentaje_superado = (float)sumaCompletion;
          a_devolver.tiempo_restante = (float)tiempoRestante;
          a_devolver.monedas_conseguidas = monedasConseguidas;
          
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          System.err.println("El hilo principal fue interrumpido durante invokeAll");
        } finally {
          pool.shutdown();
          try {
            if (!pool.awaitTermination(10, TimeUnit.MINUTES)) {
              pool.shutdownNow();
              System.err.println("Pool no terminó en tiempo — apagado forzado");
            }
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupción mientras cerraba el pool");
          }
        }
        
        // divido los valores entre las veces que se ha ejecutado para obtener las medias
        a_devolver.niveles_superados /= (float)veces;
        a_devolver.porcentaje_superado /= (float)veces;
        a_devolver.tiempo_restante /= (float)veces;
        a_devolver.monedas_conseguidas /= (float)veces;
        
        return a_devolver;
    }
	
	@Override
    public int compareTo(Individuo otro) {
        return Float.compare(fitness, otro.fitness);
    }
}
