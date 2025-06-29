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
    
    // posicion de Mario en la escena (el [0,0] es la esquina superior izquierda)
 	private final int[] POS_MARIO_GRID = {9,8};

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];

       debug = true;
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        
    	if (debug) {
    		pinta_escena(model, model.getMarioCompleteObservation(0,0));
    		get_info_casillas_y_float_pos(model);
    		debug = false;
    		
    		System.out.println("Casilla a buscar (goomba): ");
    		System.out.println("Pos absoluta: " + model.getEnemiesFloatPos()[1] + " " + model.getEnemiesFloatPos()[2]);
    		int [] casilla_a_buscar = getCasillaRelativa(new float[]{model.getEnemiesFloatPos()[1],model.getEnemiesFloatPos()[2]}, model.getMarioFloatPos());
    		System.out.println("Pos relativa: " + casilla_a_buscar[0] + " " + casilla_a_buscar[1]);
    	}
    	
    	//actions[MarioActions.JUMP.getValue()] = model.mayMarioJump() || !model.isMarioOnGround();
		//actions[MarioActions.RIGHT.getValue()] = true;

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
    
    // devuelve la posicion dentro de la escena de una casilla dada la posicion absoluta de la casilla y de Mario
    public int[] getCasillaRelativa(float[] pos_abs_casilla, float[] pos_mario){
    	int[] a_devolver = new int[2];
    	
    	// calculo la distancia absoluta desde mario a la casilla
    	a_devolver[0] = ((int) pos_abs_casilla[0]) - ((int) pos_mario[0]);
    	a_devolver[1] = ((int) pos_abs_casilla[1]) - ((int) pos_mario[1]);
    	
    	System.out.println("Distancia absoluta desde Mario: " + a_devolver[0] + " " + a_devolver[1]);
    	
    	// la divido entre 16
    	a_devolver[0] = a_devolver[0] >> 4;
    	a_devolver[1] = a_devolver[1] >> 4;
    	
    	System.out.println("Distancia en casillas desde Mario: " + a_devolver[0] + " " + a_devolver[1]);
    	
    	// le sumo la posicion relativa de Mario en la escena
    	a_devolver[0] = a_devolver[0] + POS_MARIO_GRID[0];
    	a_devolver[1] = a_devolver[1] + POS_MARIO_GRID[1];
    	
    	return a_devolver;
    }

    @Override
    public String getAgentName() {
        return "PruebaAgent";
    }
}