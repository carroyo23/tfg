import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//para redirigir salida a fichero para guardar resultados
import java.io.FileWriter;
import java.io.PrintWriter;

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
        
        try {
        	PrintWriter salida_fichero;
        	
        	for(double utc = 1; utc <= 3; utc = utc + 0.2) {
        		
        		salida_fichero = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\resultados\\greedy\\mcts_utc\\salida_mctsOptimized_utc_" + utc + ".txt"));
        		
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
        
        
        // FIN PRUEBAS FICHEROS
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       
        
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// PRUEBAS COMPLETAS AGENTES
        
        // mi agente alphaBeta optimizado (todos los niveles)
        
        level_pass = 0;
        perc_pass = 0; 
        /* 
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(new agents.alphaBetaOptimized.Agent(700, 30), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
            perc_pass += result.getCompletionPercentage();
        }
        System.out.println("Niveles pasados: " + level_pass);
        System.out.println("Porcentaje pasado: " + perc_pass);
        */
        
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
}
