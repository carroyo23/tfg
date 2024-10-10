package agents.prueba;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

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

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];
        escena = new int[model.obsGridWidth][model.obsGridHeight];

        // inicializo que siempre vaya hacia delante saltando
        actions[MarioActions.RIGHT.getValue()] = true;
        actions[MarioActions.SPEED.getValue()] = true;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
        // hago que Mario salte siempre que pueda
    	// tambien hago que mientras este en el aire mantenga el boton de saltar
    	// para que los saltos sean mayores
        actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
        
        // si ve una interrogacion va a la izquierda
        boolean vista = false;
        escena = model.getMarioSceneObservation(0); // guardo la escena
        
        
        // compruebo si en la escena hay una interrogacion
        for (int i = 0; i < model.obsGridWidth && !vista; i++) {
        	for (int j = 0; j < model.obsGridHeight && !vista; j++) {
        		if (escena[i][j] == model.OBS_QUESTION_BLOCK) {
        			actions[MarioActions.RIGHT.getValue()] = false;
        			actions[MarioActions.LEFT.getValue()] = true;
        			vista = true;
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
        return "PruebaAgent";
    }
}