package agents.alphaBeta;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;
import engine.helper.GameStatus;

import java.util.List;
import java.util.ArrayList;

class NodoAB{
	public MarioForwardModel model;
	public int valor;
	public boolean[] accion;
};

public class Agent implements MarioAgent {
	
	private boolean[] action;
	private static int MAX_PROFUNDIDAD = 5;

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
    
    private NodoAB aplhaBeta(NodoAB nodo, int profundidad, int alpha, int beta) {
    	NodoAB a_devolver = new NodoAB();
    	GameStatus status = nodo.model.getGameStatus();
    	
    	a_devolver.accion = nodo.accion;
    	
    	// checkeo los nodos finales
    	if ((status != GameStatus.RUNNING) || (profundidad == 0)){
    		if (status == GameStatus.WIN) {
    			a_devolver.valor = 100;
    		}
    		else if (status == GameStatus.RUNNING) {
    			a_devolver.valor = 80;
    		}
    		else if (status == GameStatus.TIME_OUT) {
    			a_devolver.valor = 30;
    		}
    		else if (status == GameStatus.LOSE) {
    			a_devolver.valor = 0;
    		}
    		
    		return a_devolver;
    	}
    	else {
    		// genero los hijos (genero desde el modelo actual todas las variantes de acciones)
    		List<boolean[]> hijos = generaNodos();
    		
    		//for 
    		// generar la poda para todos los hijos (creando nodos para asignar las acciones de generaNodos)
    		
    	}
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodos() {
    	List<boolean[]> result = new ArrayList<>();
    	int length = 5;
    	int total_combination = 32;
    	
    	for (int i = 0; i < total_combination; i++) {
    		boolean[] array = new boolean[length];
    		for (int j = 0; j < length; j++) {
    			array[j] = (i & (1 << j)) != 0;
    		}
    		result.add(array);
    	}
    	
    	return result;
    }
	
	@Override
	public String getAgentName() {
		return "AlphaBetaAgent";
	}
}
