package agents.mcts;

import engine.helper.GameStatus;
import engine.helper.MarioActions;

import java.util.ArrayList;
import java.util.List;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;

public class Agent implements MarioAgent {
	
	/*
	    Array con las acciones en que cada posicion es una accion (un boton pulsado)
	    Si en el array la accion es true hara ese movimiento
	*/
	private boolean[] action;
	
	private static int MAX_ITERACIONES = 4; // numero de iteraciones del bucle principal
	private static int MAX_PROFUNDIDAD = 12; // numero de acciones aleatorias a realizar en cada nodo al simular
	
	private static int NUM_REPS_ACTION = 4; // las veces que se repite una accion para que pueda mirar mas a futuro
	
	// valores para la heuristica de las recompensas
	private static float VALOR_HORIZONTAL = 500;
	private static float VALOR_VERTICAL = 10;
	private static float VALOR_KILL = 100;
	private static float VALOR_TIME_OUT = 300;
	private static float VALOR_WIN = 500;
	private static float VALOR_LOSE = 500;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		action = new boolean[MarioActions.numberOfActions()];
	}

	@Override
	public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {

		// creo un nodo inicial
		int recompensa = -1;
		NodoMCTS inicial = new NodoMCTS(model, null, null, recompensa, 0, null);
		
		boolean fin = false;
		
		// bucle principal mientras no encuentre un estado en el que gane o no se acabe el tiempo de pensar
		while (!fin) {
			
		}
		
		return action;
	}
	
	// genera todos los hijos de un nodo
	public List<NodoMCTS> generaHijos(NodoMCTS padre){
		List<NodoMCTS> a_devolver = new ArrayList<>();
		
		MarioForwardModel modelo_hijo;
		float recompensa_hijo;
		
		List<boolean[]> posibles_acciones = generaAcciones(padre.model);
		
		for (int i = 0; i < posibles_acciones.size(); i++) {
			modelo_hijo = padre.model.clone();
			
			// repito la accion un numero de ticks porque no afecta mucho al juego y permite mirar mas a futuro clonando menos estados
			for (int j = 0; j < NUM_REPS_ACTION; j++) {
				modelo_hijo.advance(posibles_acciones.get(i));
			}
			
			recompensa_hijo = generaRecompensa(modelo_hijo);
			
			a_devolver.add(new NodoMCTS(modelo_hijo, padre, null, recompensa_hijo, 0, posibles_acciones.get(i)));
		}
		
		return a_devolver;
	}
	
	// calcula la recompensa en un determinado estado del juego segun una heuristica
	public float generaRecompensa(MarioForwardModel model) {
		float a_devolver = 0;
		
		GameStatus status = model.getGameStatus();
		
		// heuristica que da puntos en funcion de como de a la derecha y arriba este Mario
		a_devolver = (model.getMarioFloatPos()[0] / model.getLevelFloatDimensions()[0]) * VALOR_HORIZONTAL;
		a_devolver -= (model.getMarioFloatPos()[1] / model.getLevelFloatDimensions()[1]) * VALOR_VERTICAL;
		
		// suma de puntos por matar enemigos
		a_devolver += model.getKillsTotal() * VALOR_KILL;
		
		// comprobacion del estado del juego si este ha terminado
		a_devolver = (status == GameStatus.WIN) ? Float.POSITIVE_INFINITY : a_devolver;
		a_devolver = (status == GameStatus.TIME_OUT) ? a_devolver + VALOR_TIME_OUT : a_devolver;
		a_devolver = (status == GameStatus.LOSE) ? Float.NEGATIVE_INFINITY : a_devolver;
		
		return a_devolver;
	}
	
	public List<boolean[]> generaAcciones(MarioForwardModel model) {
    	
    	List<boolean[]> a_devolver = new ArrayList<>();
    	
    	// filtro si no puedo saltar exploro el resto de acciones
    	if (!(model.mayMarioJump() || !model.isMarioOnGround())) {
			a_devolver = generaNodosNoJump();
		}
    	else {
    		a_devolver = generaNodosReduced();
    
    		// si puedo saltar que salte
    		//hijos = generaNodosJump();
    	}
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodosReduced() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	List<boolean[]> result = new ArrayList<>();
    	
        //result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        //result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        //result.add(new boolean[]{false, true, false, false, true});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, false, false, true});   // LEFT + JUMP
    	result.add(new boolean[]{false, false, false, false, true});  // Solo JUMP
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
    	//result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
        result.add(new boolean[]{false, true, false, true, true});    // RIGHT + JUMP + SPEED
        //result.add(new boolean[]{true, false, false, true, true});    // LEFT + JUMP + SPEED
    	
    	return result;
    }
    
    public List<boolean[]> generaNodosJump() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	List<boolean[]> result = new ArrayList<>();
    	
        result.add(new boolean[]{false, true, false, false, true});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, false, false, true});   // LEFT + JUMP
    	result.add(new boolean[]{false, false, false, false, true});  // Solo JUMP
        result.add(new boolean[]{false, true, false, true, true});    // RIGHT + JUMP + SPEED
    	
    	return result;
    }
    
    public List<boolean[]> generaNodosNoJump() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	List<boolean[]> result = new ArrayList<>();
    	
        result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
    	result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
    	
    	return result;
    }
	
	// para debug
	public void pintaEscena(int[][] a_pintar, MarioForwardModel model) {
		System.out.println("*****************************************");
		for (int i = 0; i < model.obsGridWidth; i++) {
			for (int j = 0; j < model.obsGridHeight; j++) {
				System.out.print(a_pintar[j][i]);
				System.out.print(" ");
			}
			System.out.println();
		}
		System.out.println("*****************************************");
	}

	@Override
	public String getAgentName() {
		return "MCTS";
	}
}
