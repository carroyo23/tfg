package agents.stayAlive;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;
import engine.helper.GameStatus;

public class Agent implements MarioAgent {
    private boolean[] action;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
        
        // Obtener matriz de entidades (enemigos y obstáculos)
        int[][] enemies = model.getScreenCompleteObservation(2,2);
        
        // Clonar el modelo y avanzar para simular el futuro
        MarioForwardModel futureModel = model.clone();
        futureModel.advance(action);
        futureModel.advance(action);
        futureModel.advance(action);
        int[][] futureEnemies = futureModel.getScreenCompleteObservation(2,2);
        
        System.out.println(futureModel.getGameStatus());
        // Verificar si Mario ha muerto en el futuro
        if (futureModel.getGameStatus() == GameStatus.LOSE) {
        	System.out.println("Hola");
            action[MarioActions.LEFT.getValue()] = true; // Retroceder si se prevé la muerte
        } else if (isEnemyClose(futureEnemies)) {
        	System.out.println("aqui");
            action[MarioActions.JUMP.getValue()] = true; // Saltar si hay un enemigo en la simulación
        } else {
        	System.out.println("adios");
            action[MarioActions.RIGHT.getValue()] = true; // Seguir avanzando si no hay peligro
        }
        
        return action;
    }

    private boolean isEnemyClose(int[][] enemies) {
        // Verifica si hay un enemigo en las casillas cercanas a Mario
        for (int i = 8; i < 12; i++) { // Rango delante de Mario
            for (int j = 8; j < 12; j++) {
                if (enemies[i][j] != 0) {
                    return true; // Hay un enemigo cerca
                }
            }
        }
        return false;
    }
    
    @Override
	public String getAgentName() {
		return "StayAliveAgent";
	}
}
