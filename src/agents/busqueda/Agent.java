package agents.busqueda;

import engine.helper.MarioActions;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;

public class Agent implements MarioAgent {
	/*
	 * Array con las acciones en que cada posicion es una accion. Si en el array la
	 * accion es true hara ese movimiento
	 */
	private boolean[] actions = null;

	/*
	 * Array con los objetos que hay en la escena
	 */
	int[][] escena;

	
	/*
	 * Array con los enemigos que hay en la escena
	 */
	int[][] escena_enemigos;
	
	private final float MAX_SALTO_VERT = (float) 66.5;
	
	private boolean modo_debug;
	
	// posicion de Mario en la escena (el [0,0] es la esquina superior izquierda)
	private final int POS_MARIO_X = 8;
	private final int POS_MARIO_Y = 9;
	
	private final int MAX_HEIGHT_SEARCH = POS_MARIO_Y - 4;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		actions = new boolean[MarioActions.numberOfActions()];
		escena = new int[model.obsGridWidth][model.obsGridHeight];
		escena_enemigos = new int[model.obsGridWidth][model.obsGridHeight];

		// inicializo que siempre vaya hacia delante saltando
		actions[MarioActions.RIGHT.getValue()] = true;
		actions[MarioActions.SPEED.getValue()] = true;

		modo_debug = false;
	}

	/**
	 * Si hay un bloque de interrogacion lo buscare y lo pulsare
	 * 
	 */

	@Override
	public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {

		// mientras no vea ningun bloque de pregunta se desplaza saltando hacia delante
		boolean vista = false;
		//escena = model.getScreenCompleteObservation(); // guardo la escena
		escena = model.getMarioCompleteObservation(0,0);
		escena_enemigos = model.getScreenCompleteObservation(2,2);
		//actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		actions[MarioActions.JUMP.getValue()] = false;
		
		int casilla_pregunta = -1;
		int casilla_j = -1;

		// compruebo si en la escena hay un bloque pregunta y si lo hay
		// empezare a buscarlo
		// (solo lo buscare si puedo alcanzarlo de un salto)
		for (int i = 0; i < model.obsGridWidth && !vista; i++) {
			// solo miro entre donde puedo alcanzar y que este por encima de mario
			for (int j = MAX_HEIGHT_SEARCH; j < POS_MARIO_Y && !vista; j++) {
				if (escena[i][j] == model.OBS_QUESTION_BLOCK){
					vista = true;
					casilla_pregunta = i;
					casilla_j = j;

					// si esta en rango de salto dejo de saltar hasta ponerme debajo
					// tambien compruebo que no este debajo
					if ((j >= 5) && (j <= 9)) {
						
						// si esta justo debajo que salte y deje de moverse hacia delante
						if (i == 8) {
							actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
							actions[MarioActions.RIGHT.getValue()] = false;
							actions[MarioActions.LEFT.getValue()] = false;
							
						}
						// si no que siga moviendose hacia delante sin saltar
						else if (i > 8){
							actions[MarioActions.JUMP.getValue()] = false;
							actions[MarioActions.RIGHT.getValue()] = true;
							actions[MarioActions.LEFT.getValue()] = false;
						}
						// si se la ha dejado atras que retroceda
						else {
							actions[MarioActions.JUMP.getValue()] = false;
							actions[MarioActions.RIGHT.getValue()] = false;
							actions[MarioActions.LEFT.getValue()] = true;
						}
					}

				}
			}
		}

		if (!vista) {
			actions[MarioActions.RIGHT.getValue()] = true;
			actions[MarioActions.LEFT.getValue()] = false;
			actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		}
		
		boolean hay_planta = false;
		for (int i = 0; i < model.obsGridWidth && !hay_planta; i++) {
			for (int j = 0; j < model.obsGridHeight && !hay_planta; j++) {
				hay_planta = escena_enemigos[i][j] == model.OBS_ENEMY;
			}
		}
		
		// si delante hay un muro que lo salte salvo que haya flor arriba
		if ((escena[9][8] != model.OBS_NONE) && (actions[MarioActions.LEFT.getValue()] == false) && !hay_planta){
			System.out.println("AQUIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
			actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();;
			actions[MarioActions.RIGHT.getValue()] = true;
		}
		else if ((escena[9][8] != model.OBS_NONE) && (actions[MarioActions.LEFT.getValue()] == false) && hay_planta) {
			System.out.println("AQUIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
			actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
			actions[MarioActions.RIGHT.getValue()] = false;
		}
		else {
			System.out.println(escena[9][8]);
		}
		
		System.out.println(vista+" "+casilla_pregunta + " " + casilla_j);
		pintaEscena(escena, model);
		
		if (modo_debug && vista) {
			//modo_debug = false;
			
			pintaEscena(escena, model);
			actions[MarioActions.RIGHT.getValue()] = false;
			actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		}

		return actions;
	}
	
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
		return "BusquedaAgent";
	}
}
