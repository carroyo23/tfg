package agents.busqueda;

import engine.helper.MarioActions;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;

public class Agent implements MarioAgent {
    /*
        Array con las acciones en que cada posicion es una accion
        Si en el array la accion es true hara ese movimiento
    */
    private boolean[] actions = null;
    
    /*
     Array con los objetos que hay en la escena
     */
    int [][] escena;
    
    final int T_ESPERA = 22;
    int espera; // tiempo de espera para cambiar de accion

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];
        escena = new int[model.obsGridWidth][model.obsGridHeight];

        // inicializo que siempre vaya hacia delante saltando
        actions[MarioActions.RIGHT.getValue()] = true;
        actions[MarioActions.SPEED.getValue()] = true;
        
        espera = T_ESPERA; // tiempo de espera arbitrario
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
        // hago que Mario salte siempre que pueda
    	// tambien hago que mientras este en el aire mantenga el boton de saltar
    	// para que los saltos sean mayores
        actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
        
        // si ve una interrogacion va a la izquierda
        boolean vista = false;
        escena = model.getScreenCompleteObservation(); // guardo la escena
        
        
        // compruebo si en la escena hay un bloque pregunta y si lo hay lo golpeo
        // yendo de izquierda a derecha
        for (int i = 0; i < model.obsGridWidth && !vista; i++) {
        	for (int j = 0; j < model.obsGridHeight && !vista; j++) {
        		if (escena[i][j] == model.OBS_QUESTION_BLOCK) {
        			vista = true;
        			
        			// dejo de saltar
        			actions[MarioActions.JUMP.getValue()] = false;
        			
        			if (espera >= 0) {
        				espera--;
        			}
        			else {
        				espera = T_ESPERA;
        				if ((actions[MarioActions.RIGHT.getValue()] == true)) {
            				actions[MarioActions.RIGHT.getValue()] = false;
                			actions[MarioActions.LEFT.getValue()] = true;
            			}
            			else {
            				actions[MarioActions.RIGHT.getValue()] = true;
                			actions[MarioActions.LEFT.getValue()] = false;
            			}
        			}
        			
        			// cada media espera salto para intentar darle al bloque
        			if ((espera >= 0) && (espera < (T_ESPERA/2))) {
        				actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
        			}
        			
        		}
        	}
        }
        
        if (!vista) {
        	actions[MarioActions.RIGHT.getValue()] = true;
			actions[MarioActions.LEFT.getValue()] = false;
        }

        return actions;
    }

    @Override
    public String getAgentName() {
        return "BussquedaAgent";
    }
}
