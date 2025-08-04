import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

//para redirigir salida a fichero para guardar resultados
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Random;

class Gen {
	public float horizontal;
	public float vertical;
	public float kill;
	public float monedas;
}

class Resumen {
	public int niveles_superados;
	public float porcentaje_superado;
	public float tiempo_restante;
	public int monedas_conseguidas;
}

public class Genetico {
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
		
		Resumen resumenes;
		Gen valores;
		
		Random generador_random = new Random(42);
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
	}
	
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
          
          System.out.println("Suma de porcentaje pasado: " + sumaCompletion);
          System.out.format("Pasados %2d/15 → %.1f%%\n", pasa, sumaCompletion);
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
}
