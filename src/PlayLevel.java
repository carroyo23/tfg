import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        
        int level_pass = 0;
        
        // jugar como humano
        //printResults(game.playGame(getLevel("./levels/original/lvl-7.txt"), 200, 0));
       
        // mi agente (todos los niveles
        /*
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(new agents.alphaBeta.Agent(), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, false);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
        }
        System.out.println(level_pass);
        */
        
        // mi agente
        printResults(game.runGame(new agents.mcts.Agent(), getLevel("./levels/testLevels/short.txt"), 20, 0, true));
        //printResults(game.runGame(new agents.mcts.Agent(), getLevel("./levels/original/lvl-1.txt"), 20, 0, true));
        
        // A* Robin Baumgarten
        //printResults(game.runGame(new agents.robinBaumgarten.Agent(), getLevel("./levels/original/lvl-3.txt"), 20, 0, true));
    }
}
