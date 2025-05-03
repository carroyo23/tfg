package agents.alphaBetaParalel;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;
import engine.helper.GameStatus;

import java.util.*;

import java.util.concurrent.atomic.AtomicReference;

class NodoAB{
	public MarioForwardModel model;
	public float valor;
	public boolean[] accion;
};

public class Agent implements MarioAgent {
	
	private boolean[] action;
	//private AtomicReference<boolean[]> action = new AtomicReference<>(new boolean[5]);
	private Thread decisionThread;
	private volatile boolean running = true;
	
	private static int MAX_PROFUNDIDAD = 5;

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
        
        NodoAB final_nodo = alphaBeta(nodo, MAX_PROFUNDIDAD);
        
        action = final_nodo.accion;
       
        return action;
    }
    
    public NodoAB alphaBeta(NodoAB nodo, int profundidad) {
    	NodoAB a_devolver = new NodoAB();
    	
    	// pasar las caracteristicas
    	a_devolver.model = nodo.model;
    	GameStatus status = a_devolver.model.getGameStatus();
    	a_devolver.accion = nodo.accion;
    	
    	// checkeo los nodos finales
    	if ((status != GameStatus.RUNNING) || (profundidad == 0)){
    		
    		a_devolver.valor = (a_devolver.model.getMarioFloatPos()[0] / a_devolver.model.getLevelFloatDimensions()[0]) * 50;
    		a_devolver.valor -= (a_devolver.model.getMarioFloatPos()[1] / a_devolver.model.getLevelFloatDimensions()[1]) * 10;
    		//a_devolver.valor += a_devolver.model.getKillsTotal() * 100;
    		
    		if (status == GameStatus.WIN) {
    			a_devolver.valor += 1000;
    		}
    		else if (status == GameStatus.TIME_OUT) {
    			a_devolver.valor += 300;
    		}
    		else if (status == GameStatus.LOSE) {
    			a_devolver.valor = 0;
    		}
    		
    		
    		return a_devolver;
    	}
    	
		// genero los hijos (genero desde el modelo actual todas las variantes de acciones)
    	List<boolean[]> hijos = generaNodosReduced();
		
		NodoAB mejor = new NodoAB();
		mejor.valor = -1000;
		NodoAB a_comparar = new NodoAB();
		NodoAB nuevo;
		
		// generar la poda para todos los hijos (creando nodos para asignar las acciones de generaNodos)
		for (int i = 0; i < hijos.size(); i++) {
			nuevo = new NodoAB();
			nuevo.model = a_devolver.model.clone();
			nuevo.valor = a_devolver.valor;
			
			nuevo.accion = hijos.get(i);
			
			nuevo.model.advance(nuevo.accion);
			
			a_comparar = alphaBeta(nuevo, profundidad - 1);
			a_comparar.accion = hijos.get(i);
			
			if (mejor.valor < a_comparar.valor) {
				mejor = a_comparar;
			}
		}
		
		a_devolver = mejor;
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodosReduced() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down
    	
    	List<boolean[]> result = new ArrayList<>();
    	
        result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        //result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        //result.add(new boolean[]{false, true, false, false, true});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, false, false, true});   // LEFT + JUMP
    	//result.add(new boolean[]{false, false, false, false, true});  // Solo JUMP
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
    	//result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
        result.add(new boolean[]{false, true, false, true, true});    // RIGHT + JUMP + SPEED
        //result.add(new boolean[]{true, false, false, true, true});    // LEFT + JUMP + SPEED
    	
    	return result;
    }
	
	@Override
	public String getAgentName() {
		return "AlphaBetaAgent";
	}
}
