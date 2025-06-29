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
    
    // coordenadas de la meta
    int a_buscar_x;
    int a_buscar_y;
    
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
	
	// posicion de Mario en la escena (el [0,0] es la esquina superior izquierda)
	private final int POS_MARIO_X = 8;
	private final int POS_MARIO_Y = 9;
	
	boolean debug;
	
	boolean hay_meta;
	
	private final int LIM_X = POS_MARIO_X - 4;
	
	// tiempo total (el tiempo sera la g(x)
	private int tiempo_ini;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];
        escena = new int[model.obsGridWidth][model.obsGridHeight];
        
        debug = true;
        
        tiempo_ini = model.getRemainingTime();
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
    	a_buscar_x = model.obsGridWidth - 1; // de momento busco a la misma altura que Mario
    	a_buscar_y = POS_MARIO_Y;
    	/*
    	for (int i = 0; i < model.obsGridWidth && !hay_meta; i++) {
			for (int j = 0; j < model.obsGridHeight && !hay_meta; j++) {
				
				if(escena[i][j] == 2) { // 2 es un goomba (o un enemigo ns)
					hay_meta = true;
					
					// marco la posicion relativa porque en cada escena lanzare un A*
					a_buscar_x = i;
					a_buscar_y = j;
				}
			}
		}
		*/
    	
    	
    	// meto el estado actual en abiertos (la g sera 0 porque no ha pasado tiempo)
    	NodoAStar actual = new NodoAStar(POS_MARIO_X, POS_MARIO_Y, 0, model.clone());
    	actual.calculaDistancia(a_buscar_x, a_buscar_y);
    	
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
    					hijo = new NodoAStar(POS_MARIO_X, POS_MARIO_Y, tiempo_ini - modelo_hijo.getRemainingTime(), modelo_hijo);
    					hijo.calculaDistancia(a_buscar_x, a_buscar_y);
    					
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

    @Override
    public String getAgentName() {
        return "myAStar";
    }
}