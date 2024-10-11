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
    
    // variables para comprobar la maxima altura de salto de Mario en parado
    public boolean ha_saltado; // comprobar si ha saltado
    public boolean medido;
    public float iniX, iniY;
    public float max_altura, maxX;
    public float espera;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];

        // inicializo que siempre vaya hacia delante saltando
        actions[MarioActions.RIGHT.getValue()] = false;
        actions[MarioActions.SPEED.getValue()] = true;
        
        // todavia no ha saltado
        ha_saltado = false;
        medido = false;
        max_altura = 0;
        espera = 100;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
        // hago que Mario salte siempre que pueda
    	// tambien hago que mientras este en el aire mantenga el boton de saltar
    	// para que los saltos sean mayores
        //actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
    	
    	// funcion para calcular cuantos bloques salta Mario al satar solo hacia arriba
    	if (!medido) {
    		iniX = model.getMarioFloatPos()[0];
    		iniY = model.getMarioFloatPos()[1];
    		medido = true;
    	}
    	else {
    		
    		// codigo para que solo salte una vez
    		if(!ha_saltado) {
        		ha_saltado = !model.isMarioOnGround();
        		actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
        	}
    		else {
    			actions[MarioActions.JUMP.getValue()] = !model.isMarioOnGround();
    		}
    		
    		// calculo de las coordenadas a maxima altura
    		if (max_altura > model.getMarioFloatPos()[1]) {
    			max_altura = model.getMarioFloatPos()[1];
    			maxX = model.getMarioFloatPos()[0];
    			
    		}
    	}
    	
    	if (espera > 0) {
    		espera = espera - 1;
    	}
    	else {
    		System.out.print("Coordenadas iniciales: ");
    	}

        return actions;
    }

    @Override
    public String getAgentName() {
        return "PruebaAgent";
    }
}