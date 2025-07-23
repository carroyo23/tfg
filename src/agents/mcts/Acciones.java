package agents.mcts;

import java.util.List;

public class Acciones {
	public static final List<boolean[]> ACCIONES_NO_JUMP = List.of(
	        new boolean[]{true, false, false, false, false},   // LEFT
	        new boolean[]{false, true, false, false, false},   // RIGHT
	        new boolean[]{false, true, false, true, false},    // RIGHT + SPEED
	        new boolean[]{true, false, false, true, false}     // LEFT + SPEED
	    );
	
	public static final List<boolean[]> ACCIONES_SOLO_JUMP = List.of(
			new boolean[]{false, true, false, false, true},   // RIGHT + JUMP
			new boolean[]{true, false, false, false, true},   // LEFT + JUMP
			new boolean[]{false, false, false, false, true},    // JUMP
			new boolean[]{false, true, false, true, true}     // RIGHT + JUMP + SPEED
	    );
	
	public static final List<boolean[]> ACCIONES_REDUCED = List.of(
			new boolean[]{true, false, false, false, true},   // LEFT + JUMP
			new boolean[]{false, false, false, false, true},    // JUMP
			new boolean[]{false, true, false, true, false},    // RIGHT + SPEED
			new boolean[]{false, true, false, true, true}     // RIGHT + JUMP + SPEED
	    );
	
	public static final List<boolean[]> ACCIONES_REDUCED_Y_NO_JUMP = List.of(
			new boolean[]{true, false, false, false, true},   // LEFT + JUMP
			new boolean[]{false, false, false, false, true},    // JUMP
			new boolean[]{false, true, false, true, false},    // RIGHT + SPEED
			new boolean[]{false, true, false, true, true},     // RIGHT + JUMP + SPEED
			new boolean[]{true, false, false, false, false},   // LEFT
			new boolean[]{false, true, false, false, false},   // RIGHT
			new boolean[]{true, false, false, true, false}     // LEFT + SPEED
	    );
	
	public static final List<boolean[]> ACCIONES_COMPLETE = List.of(
			new boolean[]{true, false, false, false, false},  // Solo LEFT
	        new boolean[]{false, true, false, false, false},  // Solo RIGHT
	        new boolean[]{false, true, false, false, true},   // RIGHT + JUMP
	        new boolean[]{true, false, false, false, true},   // LEFT + JUMP
	    	new boolean[]{false, false, false, false, true},  // Solo JUMP
	        new boolean[]{false, true, false, true, false},   // RIGHT + SPEED
	    	new boolean[]{true, false, false, true, false},   // LEFT + SPEED
	        new boolean[]{false, true, false, true, true},    // RIGHT + JUMP + SPEED
	        new boolean[]{true, false, false, true, true}    // LEFT + JUMP + SPEED
	    );
	
	public static final List<boolean[]> ACCIONES_MCTS = List.of(
			//new boolean[]{true, false, false, false, true},   // LEFT + JUMP
			new boolean[]{false, false, false, false, true},    // JUMP
			new boolean[]{false, true, false, true, false},    // RIGHT + SPEED
			new boolean[]{false, true, false, true, true},     // RIGHT + JUMP + SPEED
			//new boolean[]{true, false, false, false, false},   // LEFT
			new boolean[]{false, true, false, false, false}   // RIGHT
			//new boolean[]{true, false, false, true, false}     // LEFT + SPEED
	    );
}
