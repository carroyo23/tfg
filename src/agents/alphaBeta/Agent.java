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
        nodo.accion = new boolean[]{false, false, false, false, false};
        
        NodoAB final_nodo = alphaBeta(nodo, MAX_PROFUNDIDAD);
        
        action = final_nodo.accion;
        
        /*
        System.out.println(nodo.model.getGameStatus());
        System.out.print(model.getMarioFloatPos()[0]);
        
        System.out.print(" ");
        System.out.print(model.getLevelFloatDimensions()[0]);
        System.out.print(" ");
        System.out.println(model.getMarioFloatPos()[0] / model.getLevelFloatDimensions()[0]);
        System.out.println(final_nodo.valor);
        */
        
        /*
        System.out.println("LAS ACCIONES");
		System.out.println(action[0]);
		System.out.println(action[1]);
		System.out.println(action[2]);
		System.out.println(action[3]);
		System.out.println(action[4]);
        
		System.out.println("FIN ACCIONES");
		*/
        
        /*
        for (int i = 0; i < 5; i++) {
        	System.out.print(action[i] + " ");
        }
        System.out.println();
        */
		
        
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
    
    public NodoAB alphaBeta(NodoAB nodo, int profundidad) {
    	NodoAB a_devolver = new NodoAB();
    	
    	// pasar las caracteristicas
    	a_devolver.model = nodo.model;
    	GameStatus status = a_devolver.model.getGameStatus();
    	a_devolver.accion = nodo.accion;
    	
    	// checkeo los nodos finales
    	if ((status != GameStatus.RUNNING) || (profundidad <= 0)){
    		
    		a_devolver.valor = (a_devolver.model.getMarioFloatPos()[0] / a_devolver.model.getLevelFloatDimensions()[0]) * 500;
    		a_devolver.valor -= (a_devolver.model.getMarioFloatPos()[1] / a_devolver.model.getLevelFloatDimensions()[1]) * 10;
    		System.out.println(a_devolver.valor);
    		a_devolver.valor += a_devolver.model.getKillsTotal() * 100;
    		
    		if (status == GameStatus.WIN) {
    			a_devolver.valor = Float.POSITIVE_INFINITY;
    		}
    		else if (status == GameStatus.TIME_OUT) {
    			a_devolver.valor += 300;
    		}
    		else if (status == GameStatus.LOSE) {
    			a_devolver.valor = Float.NEGATIVE_INFINITY;
    		}
    		
    		/*
    		if (a_devolver.accion[MarioActions.RIGHT.getValue()] && a_devolver.accion[MarioActions.SPEED.getValue()]) {
    			a_devolver.valor += 30;
    		}
    		
    		if (a_devolver.accion[MarioActions.JUMP.getValue()]) {
    			a_devolver.valor += 200;
    		}
    		*/
    		
    		
    		return a_devolver;
    	}
    	
		// genero los hijos (genero desde el modelo actual todas las variantes de acciones)
    	List<boolean[]> hijos;
    	
    	// filtro si no puedo saltar exploro el resto de acciones
    	if (!(a_devolver.model.mayMarioJump() || !a_devolver.model.isMarioOnGround())) {
			hijos = generaNodosNoJump();
			//System.out.println("holaaaaa");
		}
    	else {
    		// si puedo saltar que salte
    		hijos = generaNodosReduced();
    		//hijos = generaNodosJump();
    		//System.out.println("adiosssss");
    	}
		
		NodoAB mejor = new NodoAB();
		mejor.valor = Float.NEGATIVE_INFINITY;
		NodoAB a_comparar = new NodoAB();
		NodoAB nuevo;
		
		// generar la poda para todos los hijos (creando nodos para asignar las acciones de generaNodos)
		for (int i = 0; i < hijos.size(); i++) {
			nuevo = new NodoAB();
			nuevo.model = a_devolver.model.clone();
			nuevo.valor = a_devolver.valor;
			
			nuevo.accion = hijos.get(i);
			
			// hago que avance 3 veces para tener que clonar menos veces y hacer que pueda explorar mas
			nuevo.model.advance(nuevo.accion);
			nuevo.model.advance(nuevo.accion);
			nuevo.model.advance(nuevo.accion);
			
			a_comparar = alphaBeta(nuevo, profundidad - 1);
			a_comparar.accion = hijos.get(i);
			
			if (mejor.valor <= a_comparar.valor) {
				mejor = a_comparar;
			}
		}
		
		a_devolver = mejor;
		
		if (a_devolver.accion == null) {
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");
			System.out.println(a_devolver.valor);
			System.out.println("fallaaaaaaaaaaaaaaa");
		}
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodos() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	
    	
    	List<boolean[]> result = new ArrayList<>();
    	int length = 5;
    	int total_combination = 32;
    	
    	//result.add(new boolean[]{false, false, false, false, false}); // Ninguna tecla presionada
        result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        result.add(new boolean[]{false, false, false, false, true});  // Solo JUMP
        //result.add(new boolean[]{false, false, false, true, false});  // Solo SPEED
        //result.add(new boolean[]{false, false, true, false, false});  // Solo DOWN
        result.add(new boolean[]{false, true, false, false, true});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, false, false, true});   // LEFT + JUMP
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
        result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
        result.add(new boolean[]{false, true, false, true, true});    // RIGHT + JUMP + SPEED
        result.add(new boolean[]{true, false, false, true, true});    // LEFT + JUMP + SPEED
    	
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
    
    
	
	@Override
	public String getAgentName() {
		return "AlphaBetaAgent";
	}
}
