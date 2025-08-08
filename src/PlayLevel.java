import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;
import engine.core.MarioAgent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
//para redirigir salida a fichero para guardar resultados
import java.io.FileWriter;
import java.io.PrintWriter;

// paralelizar codigo
import java.util.concurrent.*;
import java.util.List;
import java.util.stream.*;

public class PlayLevel {
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
        MarioGame game = new MarioGame();
        
        int level_pass;
        float perc_pass;
        int tiempo_restante;
        int monedas_conseguidas;
        
        // jugar como humano
        //printResults(game.playGame(getLevel("./levels/original/lvl-10.txt"), 200, 0));
        
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// PRUEBAS FICHEROS
        /*
        try {
        	PrintWriter salida_fichero;
        	
        	for(double utc = 3; utc <= 4; utc = utc + 0.2) {
        		
        		salida_fichero = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\tfg\\resultados\\greedy\\mcts_utc\\salida_mctsOptimized_utc_" + utc + ".txt"));
        		
        		salida_fichero.println("*******************************************");
        		salida_fichero.println("*******************************************");
            	salida_fichero.println("Puntuacion por uct: " + utc);
            	
            	System.out.println("kill: " + utc);
        		
        		for (int veces = 0; veces < 15; veces++) {
	            	level_pass = 0;
	                perc_pass = 0;
	                monedas_conseguidas = 0;
	                tiempo_restante = 0;
	                
	                System.out.println("Valor monedas: " + veces);
	            
	                salida_fichero.println("///////////////////////////////////////////////////////////");
	            	salida_fichero.println("Puntuacion por vez: " + veces);
	            	for (int i = 1; i <= 15; i++) {
	            		
	            		//System.out.print("Nivel: " + i + " ");
	            		
	    	        	MarioResult result = game.runGame(new agents.mctsOptimized.Agent((float)utc), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, false);
	    	            printResults(result);
	    	            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
	    	            perc_pass += result.getCompletionPercentage();
	    	            monedas_conseguidas += result.getCurrentCoins();
	    	            tiempo_restante += result.getRemainingTime();
	    	            salida_fichero.println("Nivel " + i + " porcentaje: " + result.getCompletionPercentage() + " Tiempo restante: " + result.getRemainingTime() + " Monedas: " + result.getCurrentCoins());
	            	}
	            	salida_fichero.println("Niveles pasados: " + level_pass);
	                salida_fichero.println("Porcentaje pasado: " + perc_pass);
	                salida_fichero.println("Tiempo restante: " + tiempo_restante);
	                salida_fichero.println("Monedas conseguidas: " + monedas_conseguidas);
	            	salida_fichero.println("*******************************************");
	            	salida_fichero.println("*******************************************");
        		}
        		
        		salida_fichero.close();
            }
        	
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    */
        
        
        // FIN PRUEBAS FICHEROS
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       
        
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// PRUEBAS COMPLETAS AGENTES
        
        // mi agente alphaBeta optimizado (todos los niveles)
        
        
        level_pass = 0;
        perc_pass = 0; 
        
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(new agents.alphaBetaGenetico.Agent(906.56567f, 16.587923f, 56.484187f, 68.577754f), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
            perc_pass += result.getCompletionPercentage();
        }
        System.out.println("Niveles pasados: " + level_pass);
        System.out.println("Porcentaje pasado: " + perc_pass);
        
        
        /*
        level_pass = 0;
        perc_pass = 0;
        
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(new agents.alphaBeta.Agent(), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
            perc_pass += result.getCompletionPercentage();
        }
        System.out.println("Niveles pasados: " + level_pass);
        System.out.println("Porcentaje pasado: " + perc_pass);
        */
        
        // FIN PRUEBAS COMPLETAS AGENTES
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // INICIO PRUEBAS PARALELAS
        
        //pruebaAlphaBetaOptimizedTodosNivelesParalelo(700, 30, 10, 10);
        
        
        /*
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores-1);
        System.out.println(cores);

        try {
          List<Callable<MarioResult>> tareas = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> (Callable<MarioResult>) () -> {
              MarioGame mg = new MarioGame();	
              MarioAgent agent = new agents.mctsOptimized.Agent();
              String level = getLevel("./levels/original/lvl-" + i + ".txt");
              return mg.runGame(agent, level, 20, 0, false);
            })
            .collect(Collectors.toList());

          // invokeAll sí arroja InterruptedException (hay que tratarlo)
          List<Future<MarioResult>> futuros = pool.invokeAll(tareas);

          int pasa = 0;
          double sumaCompletion = 0.0;

          for (Future<MarioResult> f : futuros) {
            try {
              MarioResult r = f.get(); // también arroja InterruptedException
              if (r.getGameStatus() == GameStatus.WIN) {
            	  pasa++;
              }
              sumaCompletion += r.getCompletionPercentage();
              printResults(r);
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              System.err.println("Hilo interrumpido mientras esperaba resultados");
              // quizá quieras salir del ciclo
              break;
            } catch (ExecutionException ee) {
              System.err.println("Falló nivel: " + ee.getCause());
            }
          }
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
		*/

        
        // FIN PRUEBAS PARALELAS
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        // mi agente mtcs optimizando constantes
        //printResults(game.runGame(new agents.mctsOptimized.Agent((float)2), getLevel("./levels/testLevels/short.txt"), 20, 0, true));
        //printResults(game.runGame(new agents.mctsOptimized.Agent((float)2), getLevel("./levels/original/lvl-1.txt"), 20, 0, true));
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // AGENTES FUNCIONALES
        
        // mi agente alphaBeta
        //printResults(game.runGame(new agents.alphaBeta.Agent(), getLevel("./levels/original/lvl-3.txt"), 20, 0, true));
        
        // mi agente mtcs
        //printResults(game.runGame(new agents.mctsOptimized.Agent(), getLevel("./levels/testLevels/short.txt"), 20, 0, true));
        //printResults(game.runGame(new agents.mcts.Agent(), getLevel("./levels/original/lvl-1.txt"), 20, 0, true));
        
        // FIN AGENTES FUNCIONALES
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        // A* Robin Baumgarten
        //printResults(game.runGame(new agents.robinBaumgarten.Agent(), getLevel("./levels/original/lvl-15.txt"), 20, 0, true));
    }
    
    public static void pruebaAgenteTodosNiveles(MarioAgent agente, MarioGame game) {
    	int level_pass = 0;
        float perc_pass = 0;
        
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(agente, getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
            perc_pass += result.getCompletionPercentage();
        }
        System.out.println("Niveles pasados: " + level_pass);
        System.out.println("Porcentaje pasado: " + perc_pass);
    }
    
    public static float[] pruebaAlphaBetaOptimizedTodosNivelesParalelo(float horizontal, float vertical, float kill, float moneda) {
    	float [] a_devolver = new float[4]; // [num_niveles_pasados, porcentaje_total_pasado, tiempo_restante, monedas_conseguidas]
    	
    	int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores-1);
        System.out.println(cores);

        try {
          List<Callable<MarioResult>> tareas = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> (Callable<MarioResult>) () -> {
              MarioGame mg = new MarioGame();	
              MarioAgent agent = new agents.alphaBetaOptimized.Agent(horizontal, vertical, kill, moneda);
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
              printResults(r);
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
              System.err.println("Hilo interrumpido mientras esperaba resultados");
              // quizá quieras salir del ciclo
              break;
            } catch (ExecutionException ee) {
              System.err.println("Falló nivel: " + ee.getCause());
            }
          }
          a_devolver[0] = (float)pasa;
          a_devolver[1] = (float)sumaCompletion;
          a_devolver[2] = (float)tiempoRestante;
          a_devolver[3] = (float)monedasConseguidas;
          
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
