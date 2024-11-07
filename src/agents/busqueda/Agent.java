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

	private final float MAX_SALTO_VERT = (float) 66.5;
	
	private boolean modo_debug;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		actions = new boolean[MarioActions.numberOfActions()];
		escena = new int[model.obsGridWidth][model.obsGridHeight];

		// inicializo que siempre vaya hacia delante saltando
		actions[MarioActions.RIGHT.getValue()] = true;
		actions[MarioActions.SPEED.getValue()] = true;

		modo_debug = true;
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
		//actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		actions[MarioActions.JUMP.getValue()] = false;
		
		modo_debug = false;
		if (modo_debug) {
			//modo_debug = false;
			
			for (int i = 0; i < model.obsGridWidth; i++) {
				for (int j = 0; j < model.obsGridHeight; j++) {
					System.out.print(escena[j][i]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("*****************************************");
		}
		
		int casilla_pregunta = -1;

		// compruebo si en la escena hay un bloque pregunta y si lo hay
		// empezare a buscarlo
		// (solo lo buscare si puedo alcanzarlo de un salto)
		for (int i = 0; i < model.obsGridWidth && !vista; i++) {
			// empiezo a mirar en 4 porque por encima no me importa
			for (int j = 4; j < model.obsGridHeight && !vista; j++) {
				if (escena[i][j] == model.OBS_QUESTION_BLOCK) {
					vista = true;
					casilla_pregunta = i;

					// si esta en rango de salto dejo de saltar hasta ponerme debajo
					if (j >= 4) {
						
						// si esta justo debajo que salte y deje de moverse hacia delante
						if (i == 9) {
							actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
							actions[MarioActions.RIGHT.getValue()] = false;
							actions[MarioActions.LEFT.getValue()] = false;
							
						}
						// si no que siga moviendose hacia delante sin saltar
						else if (i > 9){
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
		
		if(!model.isMarioOnGround()) {
			for (int i = 0; i < model.obsGridWidth; i++) {
				for (int j = 0; j < model.obsGridHeight; j++) {
					System.out.print(escena[j][i]);
					System.out.print(" ");
				}
				System.out.println();
			}
			System.out.println("*****************************************");
			
		}
		
		System.out.println(vista+" "+casilla_pregunta);

		return actions;
	}

	@Override
	public String getAgentName() {
		return "BusquedaAgent";
	}
}
