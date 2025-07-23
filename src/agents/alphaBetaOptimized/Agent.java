package agents.alphaBetaOptimized;

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
	
	// valores a optimizar
	
	// metadatos sobre la busqueda para eficiencia
	private static int MAX_PROFUNDIDAD = 4; // cuantos niveles de profundidad explorar en el arbol
	private static int NUM_REPS_ACTION = 4; // las veces que se repite una accion para que pueda mirar mas a futuro
	
	// valores para la heuristica de las recompensas
	private static float VALOR_HORIZONTAL = 500;
	private static float VALOR_VERTICAL = 30;
	private static float VALOR_KILL = 10;
	private static float VALOR_MONEDA = 10;
	private static float VALOR_TIME_OUT = -300;
	private static float VALOR_WIN = Float.POSITIVE_INFINITY;
	private static float VALOR_LOSE = Float.NEGATIVE_INFINITY;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        action = new boolean[MarioActions.numberOfActions()];
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
    	
        NodoAB nodo = new NodoAB();
        nodo.valor = -1000;
        nodo.model = model;
        nodo.accion = new boolean[]{false, false, false, false, false};
        
        NodoAB final_nodo = alphaBeta(nodo, MAX_PROFUNDIDAD, timer);
        
        action = final_nodo.accion;
        
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
    
    public NodoAB alphaBeta(NodoAB nodo, int profundidad, MarioTimer timer) {
    	NodoAB a_devolver = new NodoAB();
    	
    	// pasar las caracteristicas
    	a_devolver.model = nodo.model;
    	GameStatus status = a_devolver.model.getGameStatus();
    	a_devolver.accion = nodo.accion;
    	
    	// checkeo los nodos finales
    	if ((status != GameStatus.RUNNING) || (profundidad <= 0)){
    		
    		a_devolver.valor = (a_devolver.model.getMarioFloatPos()[0] / a_devolver.model.getLevelFloatDimensions()[0]) * VALOR_HORIZONTAL;
    		a_devolver.valor -= (a_devolver.model.getMarioFloatPos()[1] / a_devolver.model.getLevelFloatDimensions()[1]) * VALOR_VERTICAL;
    		a_devolver.valor += a_devolver.model.getKillsTotal() * VALOR_KILL;
    		a_devolver.valor += a_devolver.model.getNumCollectedCoins() * VALOR_MONEDA;
    		
    		if (status == GameStatus.WIN) {
    			a_devolver.valor = VALOR_WIN;
    		}
    		else if (status == GameStatus.TIME_OUT) {
    			a_devolver.valor += VALOR_TIME_OUT;
    		}
    		else if (status == GameStatus.LOSE) {
    			a_devolver.valor = VALOR_LOSE;
    		}
    		
    		
    		return a_devolver;
    	}
    	
		// genero los hijos (genero desde el modelo actual todas las variantes de acciones)
    	List<boolean[]> hijos;
    	
    	// filtro si no puedo saltar exploro el resto de acciones
    	if (!(a_devolver.model.mayMarioJump() || !a_devolver.model.isMarioOnGround())) {
			hijos = generaNodosNoJump();
		}
    	else {
    		hijos = generaNodosReduced();
    		
    		// si puedo saltar que salte
    		//hijos = generaNodosJump();
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
			
			// hago que avance 4 veces para tener que clonar menos veces y hacer que pueda explorar mas
			// esto hace que evite mejor las caidas porque le da tiempo a verlas
			nuevo.model.advance(nuevo.accion);
			nuevo.model.advance(nuevo.accion);
			nuevo.model.advance(nuevo.accion);
			nuevo.model.advance(nuevo.accion);
			
			a_comparar = alphaBeta(nuevo, profundidad - 1, timer);
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
		return "AlphaBetaOptimizedAgent";
	}
}
