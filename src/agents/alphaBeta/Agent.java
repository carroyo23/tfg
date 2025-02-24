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
	public float valor;
	public boolean[] accion;
};

public class Agent implements MarioAgent {
	
	private boolean[] action;
	private static int MAX_PROFUNDIDAD = 4;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
    	/*
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
        */
        
        NodoAB nodo = new NodoAB();
        nodo.valor = -1000;
        nodo.model = model;
        
        NodoAB final_nodo = alphaBeta(nodo, MAX_PROFUNDIDAD, 0, 0);
        
        action = final_nodo.accion;
        
        System.out.println(nodo.model.getGameStatus());
        System.out.print(model.getMarioFloatPos()[0]);
        
        System.out.print(" ");
        System.out.print(model.getLevelFloatDimensions()[0]);
        System.out.print(" ");
        System.out.println(model.getMarioFloatPos()[0] / model.getLevelFloatDimensions()[0]);
        System.out.println(final_nodo.valor);
        
        System.out.println("LAS ACCIONES");
		System.out.println(action[0]);
		System.out.println(action[1]);
		System.out.println(action[2]);
		System.out.println(action[3]);
		System.out.println(action[4]);
		System.out.println("FIN ACCIONES");
        
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
    
    public NodoAB alphaBeta(NodoAB nodo, int profundidad, int alpha, int beta) {
    	NodoAB a_devolver = new NodoAB();
    	a_devolver.model = nodo.model;
    	GameStatus status = a_devolver.model.getGameStatus();
    	
    	a_devolver.accion = nodo.accion;
    	
    	// checkeo los nodos finales
    	if ((status != GameStatus.RUNNING) || (profundidad == 0)){
    		
    		if (status == GameStatus.WIN) {
    			a_devolver.valor = 1000;
    		}
    		else if (status == GameStatus.RUNNING) {
    			a_devolver.valor = 800;
    		}
    		else if (status == GameStatus.TIME_OUT) {
    			a_devolver.valor = 300;
    		}
    		else if (status == GameStatus.LOSE) {
    			a_devolver.valor = -200;
    			return a_devolver;
    		}
    		else {
    			System.out.println("RARO");
    		}
    		
    		if (a_devolver.accion[MarioActions.RIGHT.getValue()] && a_devolver.accion[MarioActions.SPEED.getValue()]) {
    			a_devolver.valor += 30;
    		}
    		if (a_devolver.accion[MarioActions.JUMP.getValue()] && a_devolver.accion[MarioActions.SPEED.getValue()]) {
    			a_devolver.valor += 20;
    		}
    		
    		a_devolver.valor += a_devolver.model.getMarioFloatPos()[0] / a_devolver.model.getLevelFloatDimensions()[0];
    		
    		return a_devolver;
    	}
    	else {
    		// genero los hijos (genero desde el modelo actual todas las variantes de acciones)
    		List<boolean[]> hijos = generaNodos();
    		
    		NodoAB mejor = new NodoAB();
    		NodoAB a_comparar = new NodoAB();
    		
    		NodoAB nuevo = new NodoAB();
    		nuevo = a_devolver;
    		
    		// generar la poda para todos los hijos (creando nodos para asignar las acciones de generaNodos)
    		for (int i = 0; i < hijos.size(); i++) {
    			nuevo = new NodoAB();
    			nuevo.model = a_devolver.model.clone();
    			nuevo.valor = a_devolver.valor;
    			nuevo.accion = hijos.get(i);
    			
    			nuevo.model.advance(nuevo.accion);
    			
    			a_comparar = alphaBeta(nuevo, profundidad - 1, alpha, beta);
    			
    			if (mejor.valor < a_comparar.valor) {
    				mejor = a_comparar;
    			}
    		}
    		
    		a_devolver = mejor;
    	}
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodos() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	
    	
    	List<boolean[]> result = new ArrayList<>();
    	int length = 5;
    	int total_combination = 32;
    	
    	result.add(new boolean[]{false, false, false, false, false}); // Ninguna tecla presionada
        result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        result.add(new boolean[]{false, false, true, false, false});  // Solo JUMP
        result.add(new boolean[]{false, false, false, true, false});  // Solo SPEED
        result.add(new boolean[]{false, false, false, false, true});  // Solo DOWN
        result.add(new boolean[]{false, true, true, false, false});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, true, false, false});   // LEFT + JUMP
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
        result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
        result.add(new boolean[]{false, true, true, true, false});    // RIGHT + JUMP + SPEED
        result.add(new boolean[]{true, false, true, true, false});    // LEFT + JUMP + SPEED
    	
        /*
    	for (int i = 0; i < total_combination; i++) {
    		boolean[] array = new boolean[length];
    		for (int j = 0; j < length; j++) {
    			array[j] = (i & (1 << j)) != 0;
    		}
    		result.add(array);
    	}
    	*/
    	
    	return result;
    }
	
	@Override
	public String getAgentName() {
		return "AlphaBetaAgent";
	}
}
