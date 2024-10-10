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

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];

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

        return actions;
    }

    @Override
    public String getAgentName() {
        return "PruebaAgent";
    }
}