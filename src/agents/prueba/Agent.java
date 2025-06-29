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
    
    public boolean debug;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];

       debug = true;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
    	if (debug) {
    		get_info_casillas_y_float_pos(model);
    		debug = false;
    	}
    	
    	
    	pinta_escena(model, model.getMarioCompleteObservation(0,0));
    	actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		actions[MarioActions.RIGHT.getValue()] = true;

        return actions;
    }
    
    public void pinta_escena(MarioForwardModel model, int [][] a_pintar) {
    	System.out.println("*************************************************************");
    	for (int i = 0; i < model.obsGridWidth; i++) {
			for (int j = 0; j < model.obsGridHeight; j++) {
				System.out.print(a_pintar[j][i]);
			}
			System.out.println();
		}
    	System.out.println("*************************************************************");
    }
    
    public void get_info_casillas_y_float_pos(MarioForwardModel model) {
    	for (int i = 0; i < model.getEnemiesFloatPos().length; i++) {
    		System.out.println(model.getEnemiesFloatPos()[i]);
    	}
    	System.out.println("Mario pos:");
    	for (int i = 0; i < model.getMarioFloatPos().length; i++) {
    		System.out.println(model.getMarioFloatPos()[i]);
    	}
    	
    	System.out.println("Nivel dimension:");
    	for (int i = 0; i < model.getLevelFloatDimensions().length; i++) {
    		System.out.println(model.getLevelFloatDimensions()[i]);
    	}
    	
    	System.out.println("Escena dimension:");
    	System.out.println(model.obsGridWidth);
    	System.out.println(model.obsGridHeight);
    	System.out.println("Escena dimension a mano:");
    	System.out.println(model.getMarioCompleteObservation().length);
    	System.out.println(model.getMarioCompleteObservation()[0].length);
    }

    @Override
    public String getAgentName() {
        return "PruebaAgent";
    }
}