package agents.mctsOptimized;

import engine.helper.GameStatus;
import engine.helper.MarioActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;

public class Agent implements MarioAgent {
	
	/*
	    Array con las acciones en que cada posicion es una accion (un boton pulsado)
	    Si en el array la accion es true hara ese movimiento
	*/
	private boolean[] action;
	
	private static int MAX_ITERACIONES = 3; // numero de iteraciones del bucle principal
	private static int MAX_PROFUNDIDAD = 5; // numero de acciones aleatorias a realizar en cada nodo al simular
	
	private static int NUM_REPS_ACTION = 1; // las veces que se repite una accion para que pueda mirar mas a futuro
	
	// valores para la heuristica de las recompensas
	private static float VALOR_HORIZONTAL = 500;
	private static float VALOR_VERTICAL = 10;
	private static float VALOR_KILL = 100;
	private static float VALOR_TIME_OUT = 300;
	private static float VALOR_WIN = Float.POSITIVE_INFINITY;
	private static float VALOR_LOSE = -10000000;
	
	private static float MAX_TIEMPO = 5;
	
	private static float CONST_UCT = (float) 1.3; // TODO: AJUSTAR ESTE VALOR
	
	int cont;
	
	Random random;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		action = new boolean[MarioActions.numberOfActions()];
		cont = 0;
		random = new Random(42);
	}

	@Override
	public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
		
		// inicializo action como andar a la derecha por si no decidiera nada
		//action[MarioActions.RIGHT.getValue()] = true; // LO COMENTO PARA VER SI EN ALGUN MOMENTO NO DECIDE NADA VER EL ERROR

		// creo un nodo inicial
		float recompensa = -1;
		NodoMCTS inicial = new NodoMCTS(model, null, null, recompensa, 1, null);
		inicial.hijos = generaHijos(inicial);
		inicial.recompensa = generaRecompensa(inicial.model);
		
		// empiezo mirando el nodo inicial
		NodoMCTS actual;
		
		
		NodoMCTS nodo_backpropagation;
		float mejor_recompensa_hijos;
		
		// bucle principal mientras no encuentre un estado en el que gane o no se acabe el tiempo de pensar
		boolean fin = false;
		boolean hay_nodo_final = false;
		while (!fin) {
			cont++;
			
			// seleccion del mejor nodo
			actual = seleccionaNodo(inicial, timer);
			
			if (actual.model.getGameStatus() != GameStatus.WIN) {
			
				// expando el nodo (genero sus hijos)
				actual.hijos = generaHijos(actual);
				
				mejor_recompensa_hijos = Float.NEGATIVE_INFINITY;
				
				// simulo sus hijos al azar un numero de acciones
				for (NodoMCTS a_simular : actual.hijos) {
					
					// genero el numero de acciones al azar que necesito
					for (int i = 0; i < MAX_PROFUNDIDAD; i++) {
						a_simular.model.advance(Acciones.ACCIONES_COMPLETE.get(random.nextInt(Acciones.ACCIONES_COMPLETE.size())));
					}
					
					// calculo la nueva recompensa
					a_simular.recompensa = generaRecompensa(a_simular.model);
					a_simular.visitas = 1;
					
					mejor_recompensa_hijos = (mejor_recompensa_hijos < a_simular.recompensa) ? a_simular.recompensa : mejor_recompensa_hijos;
					
					// hago backpropagation con la recompensa
					nodo_backpropagation = a_simular;
					while (nodo_backpropagation.padre != null) {
						nodo_backpropagation.padre.visitas += 1;
						nodo_backpropagation.padre.recompensa += nodo_backpropagation.recompensa / (float) nodo_backpropagation.padre.hijos.size();
						nodo_backpropagation = nodo_backpropagation.padre;
						
					}
					
				}
				
				/*
				// hago backpropagation con la recompensa
				nodo_backpropagation = actual;
				while (nodo_backpropagation.padre != null) {
					nodo_backpropagation = nodo_backpropagation.padre;
					nodo_backpropagation.visitas += 1;
					nodo_backpropagation.recompensa += a_simular.recompensa / (float) actual.hijos.size();
					
				}
				*/
				
				fin = timer.getRemainingTime() <= MAX_TIEMPO;
			}
			else {
				fin = true;
				hay_nodo_final = true;
				action = actual.action;
			}
			
		}
		
		if (!hay_nodo_final) {
			// me quedo con la mejor accion
			float mejor_recompensa = Float.NEGATIVE_INFINITY;
			boolean[] mejor_accion = null;
			
			for (NodoMCTS a_comparar : inicial.hijos) {
				if (mejor_recompensa < a_comparar.recompensa) {
					mejor_recompensa = a_comparar.recompensa;
					mejor_accion = a_comparar.action;
				}
			}
			
			action = mejor_accion;
		}
		
		System.out.println("iteraciones bucle: " + cont);
		cont = 0;
		
		return action;
	}
	
	// selecciona el mejor nodo basandose en la puntuación UCT
	public NodoMCTS seleccionaNodo(NodoMCTS raiz, MarioTimer timer) {
		NodoMCTS mejor_nodo = raiz;
		float valor_mejor_nodo = Float.NEGATIVE_INFINITY;
		float nuevo_valor;
		List<NodoMCTS> hijos_a_comparar;
		int mejor_nodo_indice = -1;
		
		//pintaNodo(raiz);
		//pintaNodo(mejor_nodo);
		
		while (((mejor_nodo.hijos != null) && !mejor_nodo.hijos.isEmpty()) && (timer.getRemainingTime() > MAX_TIEMPO) && !esNodoFinal(mejor_nodo)) {
			
			//pintaNodo(mejor_nodo);
			
			// reinicio variables para cada nivel del arbol
			mejor_nodo_indice = -1;
			hijos_a_comparar = mejor_nodo.hijos;
			valor_mejor_nodo = Float.NEGATIVE_INFINITY;
			
			// recorro los hijos
			for (int i = 0; i < hijos_a_comparar.size(); i++) {
				nuevo_valor = getUCT(hijos_a_comparar.get(i));
				
				//pintaNodo(hijos_a_comparar.get(i));
				//System.out.println(nuevo_valor);
				
				// actualizo el mejor nodo
				if (nuevo_valor >= valor_mejor_nodo) {
					valor_mejor_nodo = nuevo_valor;
					//mejor_nodo = hijos_a_comparar.get(i);
					mejor_nodo_indice = i;
				}
			}
			
			mejor_nodo = mejor_nodo.hijos.get(mejor_nodo_indice);
			
			//pintaNodo(mejor_nodo);
		}
		
		return mejor_nodo;
	}
	
	public boolean esNodoFinal(NodoMCTS nodo) {
		return nodo.model.getGameStatus() != GameStatus.RUNNING;
	}
	
	// Calcula la puntuacion UCT de un nodo
	public float getUCT(NodoMCTS nodo) {
		float a_devolver;
		float explotacion;
		float exploracion;
		
		if (nodo.visitas != 0) {
			explotacion = (float) nodo.recompensa / (float) nodo.padre.visitas;
			exploracion = (float) (CONST_UCT * Math.sqrt(Math.log(nodo.padre.visitas) / nodo.visitas));
			
			a_devolver = explotacion + exploracion;
			
			/*
			System.out.println("-------------------------------------");
			System.out.println(explotacion);
			System.out.println(exploracion);
			System.out.println("-------------------------------------");
			*/
		}
		else {
			a_devolver = Float.POSITIVE_INFINITY;
		}
		
		return a_devolver;
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
		a_devolver = (status == GameStatus.WIN) ? VALOR_WIN : a_devolver;
		a_devolver = (status == GameStatus.TIME_OUT) ? a_devolver - VALOR_TIME_OUT : a_devolver;
		a_devolver = (status == GameStatus.LOSE) ? VALOR_LOSE : a_devolver;
		
		return a_devolver;
	}
	
	public List<boolean[]> generaAcciones(MarioForwardModel model) {
    	
    	List<boolean[]> a_devolver = new ArrayList<>();
    	
    	// filtro si no puedo saltar exploro el resto de acciones
    	//a_devolver = (!(model.mayMarioJump() || !model.isMarioOnGround())) ? generaNodosNoJump() : generaNodosReduced();
    	
    	//a_devolver = Acciones.ACCIONES_REDUCED;
    	
    	a_devolver = Acciones.ACCIONES_COMPLETE;
    	
    	return a_devolver;
    }
    
    public List<boolean[]> generaNodosReduced() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down (de hecho down no parece util)
    	
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
    
    public List<boolean[]> generaNodosReducedYNoJump() {
    	// heuristica: left y right no se pulsan a la vez ni saltar y down (de hecho down no parece util)
    	
    	List<boolean[]> result = new ArrayList<>();
    	
        result.add(new boolean[]{true, false, false, false, false});  // Solo LEFT
        result.add(new boolean[]{false, true, false, false, false});  // Solo RIGHT
        //result.add(new boolean[]{false, true, false, false, true});   // RIGHT + JUMP
        result.add(new boolean[]{true, false, false, false, true});   // LEFT + JUMP
    	result.add(new boolean[]{false, false, false, false, true});  // Solo JUMP
        result.add(new boolean[]{false, true, false, true, false});   // RIGHT + SPEED
    	result.add(new boolean[]{true, false, false, true, false});   // LEFT + SPEED
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
	
	public void pintaNodo(NodoMCTS a_pintar) {
		System.out.println("*****************************************");
		if (a_pintar.hijos == null) {
			System.out.println("No tiene hijos (es null)");
		}
		else {
			System.out.println("Numero hijos: " + a_pintar.hijos.size());
		}
		System.out.println(a_pintar.recompensa);
		System.out.println(a_pintar.visitas);
		System.out.println("*****************************************");
	}

	@Override
	public String getAgentName() {
		return "MCTS OPTIMIZED";
	}
}
