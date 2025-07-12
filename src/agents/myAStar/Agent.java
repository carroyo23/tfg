package agents.myAStar;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.GameStatus;
import engine.helper.MarioActions;

public class Agent implements MarioAgent {
    /*
        Array con las acciones en que cada posicion es una accion
        Si en el array la accion es true hara ese movimiento
    */
    private boolean[] actions = null;
    
    // coordenadas de la meta en casillas
    int[] a_buscar_casilla;
    float[] a_buscar_global;
    		
    // Estructura para guardar nodos (abiertos y cerrados apuntaran aqui)
    NodoAStar [][] nodos;
    
    // abiertos:
    PriorityQueue<NodoAStar> abiertos_cola; // cola con prioridad para ir eligiendo el siguiente nodo a expandir
	boolean [][] abiertos_matriz; // matriz con las casillas de cada nivel para comprobar si el nodo esta en abiertos
	
	// matriz para comprobar si un nodo esta en cerrados
	boolean [][] cerrados;
	
	/*
	 * Array con los objetos que hay en la escena
	 */
	int[][] escena;
	
	boolean debug;
	
	boolean hay_meta;
	
	// tiempo total (el tiempo sera la g(x)
	private int tiempo_ini;
	
	public final int TAM_CASILLA = 16;
	public final int FACTOR_DIV_CASILLA = 4; // para dividir en casillas hare pos >> 4 (por eficiencia)
	
	// dimensiones del nivel
	private float[] dim_nivel_float;
	private int[] dim_nivel_casillas;
	
	// posicion de Mario en la escena (el [0,0] es la esquina superior izquierda)
	private final int[] POS_MARIO_GRID = {9,8};
	
	private final int LIM_X = POS_MARIO_GRID[0] - 4;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];
        escena = new int[model.obsGridWidth][model.obsGridHeight];
        
        a_buscar_casilla = new int[2];
        a_buscar_global = new float[2];
        
        debug = true;
        
        tiempo_ini = model.getRemainingTime();
        
        // guardo el tamaño del nivel
        dim_nivel_float = model.getLevelFloatDimensions();
        
        dim_nivel_casillas = new int[] {((int) dim_nivel_float[0]) >> FACTOR_DIV_CASILLA, ((int) dim_nivel_float[1]) >> FACTOR_DIV_CASILLA};
        
        
        // inicializo las matrices completamente a false
        abiertos_matriz = new boolean[dim_nivel_casillas[0]][dim_nivel_casillas[1]];
        cerrados = new boolean[dim_nivel_casillas[0]][dim_nivel_casillas[1]];
        
        for (int i = 0; i < dim_nivel_casillas[0]; i++) {
        	for (int j = 0; j < dim_nivel_casillas[1]; j++) {
        		abiertos_matriz[i][j] = false;
        		cerrados[i][j] = false;
        	}
        }
        
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
        // calculare el camino en cada paso
    	
    	/*
    	 * 	el bloque a buscar sera el mas lejano en la pantalla sobre el que se pueda posar Mario
    	 *	a ser posible en la misma y que este Mario, si no empezar a buscar hacia abajo, luego arriba y luego retorceder una
    	*/
    	
    	escena = model.getMarioCompleteObservation(0,0); // array con los bloques de la escena
    	
    	if (debug) {
    		for (int i = 0; i < model.obsGridWidth; i++) {
    			for (int j = 0; j < model.obsGridHeight; j++) {
    				System.out.print(escena[j][i]);
    			}
    			System.out.println();
    		}
    		
    		debug = false;
    	}
    	
    	// TODO: MARCAR LA CASILLA QUE VAMOS A IR BUSCANDO
    
    	// busco la ultima casilla a la altura de Mario que pueda ver
    	a_buscar_casilla[0] = model.obsGridWidth - 1;
    	a_buscar_casilla[1] = POS_MARIO_GRID[1]; // de momento busco a la misma altura que Mario
    	
    	// identifico la posicion global de la casilla
    	a_buscar_global[0] = ((a_buscar_casilla[0] - POS_MARIO_GRID[0]) * TAM_CASILLA) + model.getMarioFloatPos()[0];
    	a_buscar_global[1] = model.getMarioFloatPos()[1];
    	
    	/*
    	for (int i = 0; i < model.obsGridWidth && !hay_meta; i++) {
			for (int j = 0; j < model.obsGridHeight && !hay_meta; j++) {
				
				if(escena[i][j] == 2) { // 2 es un goomba (o un enemigo ns)
					hay_meta = true;
					
					// marco la posicion relativa porque en cada escena lanzare un A*
					a_buscar_casilla[0] = i;
					a_buscar_casilla[1] = j;
				}
			}
		}
		*/
    	
    	
    	// meto el estado actual en abiertos (la g sera 0 porque no ha pasado tiempo)
    	NodoAStar actual = new NodoAStar(POS_MARIO_GRID[0], POS_MARIO_GRID[1], 0, model.clone());
    	actual.calculaDistancia(a_buscar_casilla[0], a_buscar_casilla[1]);
    	
    	nodos[actual.x][actual.y] = actual;
    	abiertos_cola.add(actual);
    	abiertos_matriz[actual.x][actual.y] = true;
    	
    	List<boolean[]> nuevas_acciones; // aqui guardare las posibles acciones en cada momento
    	NodoAStar hijo;
    	MarioForwardModel modelo_hijo;
    	
    	boolean fin = false;
    	
    	while (!fin) {
    		// saco el mejor de abiertos
    		actual = new NodoAStar(abiertos_cola.poll());
    		abiertos_matriz[actual.x][actual.y] = false;
    		
    		// compruebo si en este gana
    		if (actual.model.getGameStatus() == GameStatus.WIN) {
    			fin = true;
    		}
    		else {
    			// lo meto en cerrados
    			cerrados[actual.x][actual.y] = true;
    			
    			// genero nuevos nodos
    			// TODO: generarlos (el minimo posible de ramificaciones) (de momento 4)
    			
    			nuevas_acciones = generaAcciones(actual.model);
    			
    			// compruebo cada hijo generado
    			for (int i = 0; i < nuevas_acciones.size() && !fin; i++) {
    				
    				modelo_hijo = actual.model.clone();
    				
    				// avanzo 3 veces cada accion por eficiencia
    				modelo_hijo.advance(nuevas_acciones.get(i));
    				modelo_hijo.advance(nuevas_acciones.get(i));
    				modelo_hijo.advance(nuevas_acciones.get(i));
    				
    				// compruebo que Mario no haya muerto
    				if (modelo_hijo.getGameStatus() == GameStatus.WIN) {
    					// si ha ganado devuelvo la accion del padre
    					fin = true;
    					
    					while (actual.accion != null) {
    						actions = actual.accion;
    						actual = actual.padre;
    					}
    				}
    				else if(modelo_hijo.getGameStatus() == GameStatus.RUNNING) {
    					hijo = new NodoAStar(POS_MARIO_GRID[0], POS_MARIO_GRID[1], tiempo_ini - modelo_hijo.getRemainingTime(), modelo_hijo);
    					hijo.calculaDistancia(a_buscar_casilla[0], a_buscar_casilla[1]);
    					
    					// TODO: PARA COMPROBAR SI ESTA EN ABIERTOS O EN CERRADOS NO ME VALE LA CUADRICULA PQ SE VA MOVIENDO (?)
    				}
    				
    				// si no ha ganado o sigue vivo no se hace nada con el nodo
    			}
    		}
    	}
    	
    	// TODO: meter una comprobacion por tiempo y que si la accion es null meter una con la que no insta muera
    	
        return actions;
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
    
    // devuelve la posicion dentro de la escena de una casilla dada la posicion absoluta de la casilla y de Mario
    public int[] getCasillaRelativa(float[] pos_abs_casilla, float[] pos_mario){
    	int[] a_devolver = new int[2];
    	
    	// calculo la distancia absoluta desde mario a la casilla
    	a_devolver[0] = ((int) pos_abs_casilla[0]) - ((int) pos_mario[0]);
    	a_devolver[1] = ((int) pos_abs_casilla[1]) - ((int) pos_mario[1]);
    	
    	// la divido entre el numero de posiciones que abarca una casilla
    	a_devolver[0] = a_devolver[0] >> FACTOR_DIV_CASILLA;
    	a_devolver[1] = a_devolver[1] >> FACTOR_DIV_CASILLA;
    	
    	// le sumo la posicion relativa de Mario en la escena
    	a_devolver[0] = a_devolver[0] + POS_MARIO_GRID[0];
    	a_devolver[1] = a_devolver[1] + POS_MARIO_GRID[1];
    	
    	return a_devolver;
    }

    @Override
    public String getAgentName() {
        return "myAStar";
    }
}