import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.PriorityQueue;
import java.util.Collections;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

//para redirigir salida a fichero para guardar resultados
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Random;

class Resumen {
	public int niveles_superados = -1;
	public float porcentaje_superado = -1;
	public float tiempo_restante = -1;
	public int monedas_conseguidas = -1;
	
	public Resumen(){
		
	}
	
	public Resumen(int nuevo_nivel, float nuevo_perc, float nuevo_tiempo, int nuevo_monedas){
		niveles_superados = nuevo_nivel;
		porcentaje_superado = nuevo_perc;
		tiempo_restante = nuevo_tiempo;
		monedas_conseguidas = nuevo_monedas;
	}
	
	public Resumen(final Resumen otro) {
		niveles_superados = otro.niveles_superados;
		porcentaje_superado = otro.porcentaje_superado;
		tiempo_restante = otro.tiempo_restante;
		monedas_conseguidas = otro.monedas_conseguidas;
	}
}

class Individuo implements Comparable<Individuo>{
	// la forma de este array sera la siguiente: [valor_horizontal, valor_vertical, valor_kill, valor_monedas]
	public float[] genoma = null;
	public static final int NUM_GENES = 4;
	
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
		resultados = evaluaIndividuo(genoma);
		getFitness();
	}
	
	public void actualizaFitness() {
		resultados = evaluaIndividuo(genoma);
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
	
	public static Resumen evaluaIndividuo(float[] genes) {
    	Resumen a_devolver = new Resumen(); // [num_niveles_pasados, porcentaje_total_pasado, tiempo_restante, monedas_conseguidas]
    	
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
	
	@Override
    public int compareTo(Individuo otro) {
        return Float.compare(fitness, otro.fitness);
    }
}

public class Genetico {
	
	public static Random generador_random = new Random(42);
	
	// el operador de seleccion sera por torneo de 3 individuos
	public static List<Individuo> op_seleccion(final List<Individuo> poblacion, int tam_reemplazo) {
		List<Individuo> nuevos = new ArrayList<>();
		
		int num_individuos = poblacion.size();
		int pos1 = -1, pos2 = -1, pos3 = -1; // las posiciones de los individuos en la poblacion
		float fit1, fit2, fit3; // las puntuaciones de cada individuo
		float max; // para calcular el mejor individuo
		
		for (int i = 0; i < tam_reemplazo; i++) {
			
			// escojo 3 individuos aleatoriamente asegurandome de que sean distintos
	        while((pos1 == pos2) || (pos1 == pos3) || (pos2 == pos3)){
	        	pos1 = generador_random.nextInt(0, num_individuos);
				pos2 = generador_random.nextInt(0, num_individuos);
				pos3 = generador_random.nextInt(0, num_individuos);
	        }
	        
	        fit1 = poblacion.get(pos1).getFitness();
	        fit2 = poblacion.get(pos2).getFitness();
	        fit3 = poblacion.get(pos3).getFitness();
	        
	        // me quedo con el que mejor fitness tenga
	        max = Math.max(fit1, fit2);
	        max = Math.max(max, fit3);
	        
	        if (fit1 == max){
	            nuevos.add(poblacion.get(pos1));
	        }
	        else if (fit2 == max){
	            nuevos.add(poblacion.get(pos2));   
	        }
	        else{
	            nuevos.add(poblacion.get(pos3));
	        }
		}
		
		return nuevos;
	}
	
	// operador de cruce (cruza 2 padres para generar 2 hijos) (solo paso los genomas porque el resto no lo necesito)
	// se usara el cruce BLX-alpha
	public static List<Individuo> op_cruce_BLX(final float[] uno, final float[] otro, float alpha) {
		List<Individuo> hijos = new ArrayList<Individuo>();
		
		float min, max, diff;
		float[] primer_genoma = uno.clone();
		float[] segundo_genoma = otro.clone();
		
		// inicializo cada hijo a un padre
		
		// itero sobre cada gen
		for (int i = 0; i < primer_genoma.length; i++) {
			
			// calculo la diferencia, el maximo y el minimo
			min = Math.min(primer_genoma[i], segundo_genoma[i]);
			max = Math.max(primer_genoma[i], segundo_genoma[i]);
			diff = max - min;
			
			// asigno en cada gen un valor aleatorio dentro del rango que marca el operador de cruce BLX-alpha
			primer_genoma[i] = (float) generador_random.nextDouble(min - alpha*diff, max + alpha*diff);
			segundo_genoma[i] = (float) generador_random.nextDouble(min - alpha*diff, max + alpha*diff);
		}
		
		// relleno los hijos (aniado el genoma y dejo los resultados como indeterminados hasta que se evaluen
		hijos.add(new Individuo(primer_genoma));
		hijos.add(new Individuo(segundo_genoma));
		
		return hijos;
	}
	
	
	public static Individuo op_mutacion(final float [] a_mutar, float delta) {
		Individuo mutado;
		float [] nuevo_genoma = a_mutar.clone();
		
		// cojo un gen aleatorio y lo muto
		int pos_a_mutar = generador_random.nextInt(a_mutar.length);
		
		// para mutarlo le sumare una componente z sacado de una distribucion N(0, delta^2)
		float z = (float)generador_random.nextGaussian(0, delta*delta);
		
		// creo al nuevo individuo a partir de la mutacion asegurandome de que sea valida, es decir, este en el intervalo [0,1]
		nuevo_genoma[pos_a_mutar] = Math.max(0, Math.min(1, nuevo_genoma[pos_a_mutar] + z));
		mutado = new Individuo(nuevo_genoma);
		
		return mutado;
	}
	
	// algoritmo genetico estacionario
	public static Individuo AGE() {
		final int NUM_INDIVIDUOS = 50; // tamaño de la poblacion
		final int MAX_EVAL = 1000; // maximo de soluciones a evaluar
		final int NUM_HIJOS = 2; // numero de hijos que devolvera el operador de seleccion (al ser estacionario solo 2)
		final float PROB_CRUCE = 1.0f;
		final float PROB_MUTA = 0.08f;
		final float ALPHA = 0.3f; // alpha para el operador de cruce BLX-alpha que sera BLX-0.3
		
		int num_eval = 0;
		
		// usare una lista para ir quitando y metiendo individuos con facilidad
		List<Individuo> poblacion = new ArrayList<Individuo>();
		List<Individuo> nuevos; // la nueva generacion
		
		// ReverseOrder porque quiero sacar los mejores del torneo por lo que ordeno de mayor a menor
		PriorityQueue<Individuo> torneo = new PriorityQueue<Individuo>(Comparator.reverseOrder());
		
		// genero la poblacion inicial de manera aleatoria
		for (int i = 0; i < NUM_INDIVIDUOS; i++) {
			System.out.println("INI");
			Individuo nuevo = new Individuo();
			nuevo.generaRandomSol(generador_random);
			poblacion.add(nuevo);
			num_eval++;
			System.out.println("**************************************************************");
		}
		
		while (num_eval <= MAX_EVAL) {
			
			// selecciono 2 padres
			nuevos = op_seleccion(poblacion, NUM_HIJOS);
			
			// los cruzo
			nuevos = op_cruce_BLX(nuevos.get(0).genoma, nuevos.get(1).genoma, ALPHA);
			
			// los muto o no segun la probabilidad de cruce
			for (Individuo hijo : nuevos) {
				if (generador_random.nextInt(100) < (PROB_MUTA*100)) {
					hijo = op_mutacion(hijo.genoma, 0.05f);
				}
				
				// al haberse cruzado, muten o no, actualizo su fitness
				hijo.actualizaFitness();
				num_eval++;
			}
			
			// hago un torneo con los peores de la poblacion y los hijos
			for (int i = 0; i < NUM_HIJOS; i++) {
				torneo.add(nuevos.get(i));
				Individuo peor = Collections.min(poblacion, Comparator.comparingDouble(j -> j.fitness));
				poblacion.remove(peor);
				torneo.add(peor);
			}
			
			// me quedo con los 2 mejores
			for (int i = 0; i < NUM_HIJOS; i++) {
				poblacion.add(torneo.element());
				torneo.poll();
			}
			
			Individuo mejor = Collections.max(poblacion, Comparator.comparingDouble(i -> i.fitness));
			
			System.out.println("**************************************************************");
			System.out.println("Horizontal: " + mejor.genoma[0]);
			System.out.println("Vertical: " + mejor.genoma[1]);
			System.out.println("kill: " + mejor.genoma[2]);
			System.out.println("monedas: " + mejor.genoma[3]);
			
			System.out.println("Niveles superados: " + mejor.resultados.niveles_superados);
			System.out.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
			System.out.println("**************************************************************");
		}
		
		return Collections.max(poblacion, Comparator.comparingDouble(i -> i.fitness));
	}
	
	public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }
	
	public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }
	
	public static void main(String[] args) {
		
		Individuo mejor = AGE();
		
		System.out.println("Horizontal: " + mejor.genoma[0]);
		System.out.println("Vertical: " + mejor.genoma[1]);
		System.out.println("kill: " + mejor.genoma[2]);
		System.out.println("monedas: " + mejor.genoma[3]);
		
		System.out.println("Niveles superados: " + mejor.resultados.niveles_superados);
		System.out.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
		
		/*
		//Random generador_random = new Random(42);
		System.out.println(generador_random.nextDouble());
		System.out.println(generador_random.nextDouble());
		System.out.println(generador_random.nextDouble());
		System.out.println(generador_random.nextDouble());
		System.out.println(generador_random.nextDouble());
		System.out.println(generador_random.nextDouble());
		
		try {
      	  PrintWriter salida_fichero = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\tfg\\resultados\\prueba_paralela.txt"));
      	  
      	  resumenes = pruebaAlphaBetaGeneticoTodosNivelesParalelo(700, 30, 10, 12);
      	  salida_fichero.println("Niveles superados: " + resumenes.niveles_superados);
      	  salida_fichero.println("Porcentaje: " + resumenes.porcentaje_superado);
      	  salida_fichero.println("Tiempo_restante: " + resumenes.tiempo_restante);
      	  salida_fichero.println("Monedas_conseguidas: " + resumenes.monedas_conseguidas);
      	  salida_fichero.println("*************************************************************************");
      	  resumenes = pruebaAlphaBetaGeneticoTodosNivelesParalelo(700, 30, 11, 13);
      	  salida_fichero.println("Niveles superados: " + resumenes.niveles_superados);
    	  salida_fichero.println("Porcentaje: " + resumenes.porcentaje_superado);
    	  salida_fichero.println("Tiempo_restante: " + resumenes.tiempo_restante);
    	  salida_fichero.println("Monedas_conseguidas: " + resumenes.monedas_conseguidas);
    	  salida_fichero.println("*************************************************************************");
      	  salida_fichero.close();
        } catch (IOException e) {
      	  e.printStackTrace();
        }
        */
	}
	
	/*
	public static Resumen pruebaAlphaBetaGeneticoTodosNivelesParalelo(float horizontal, float vertical, float kill, float moneda) {
    	Resumen a_devolver = new Resumen(); // [num_niveles_pasados, porcentaje_total_pasado, tiempo_restante, monedas_conseguidas]
    	
    	int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores-1);
        //System.out.println(cores);

        try {
          List<Callable<MarioResult>> tareas = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> (Callable<MarioResult>) () -> {
              MarioGame mg = new MarioGame();	
              MarioAgent agent = new agents.alphaBetaGenetico.Agent(horizontal, vertical, kill, moneda);
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
    */
}
