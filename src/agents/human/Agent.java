package agents.human;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public class Agent extends KeyAdapter implements MarioAgent {
    private boolean[] actions = null;
    
    int[][] escena;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        actions = new boolean[MarioActions.numberOfActions()];
        escena = new int[model.obsGridWidth][model.obsGridHeight];
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
    	
    	/*
    	escena = model.getMarioCompleteObservation(0,0);
    	
    	for (int i = 0; i < model.obsGridWidth; i++) {
			for (int j = 0; j < model.obsGridHeight; j++) {
				System.out.print(escena[j][i]);
				System.out.print(" ");
			}
			System.out.println();
		}
    	System.out.println("***************************");
    	*/
    	
        return actions;
    }

    @Override
    public String getAgentName() {
        return "HumanAgent";
    }

    @Override
    public void keyPressed(KeyEvent e) {
        toggleKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        toggleKey(e.getKeyCode(), false);
    }

    private void toggleKey(int keyCode, boolean isPressed) {
        if (this.actions == null) {
            return;
        }
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                this.actions[MarioActions.LEFT.getValue()] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                this.actions[MarioActions.RIGHT.getValue()] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                this.actions[MarioActions.DOWN.getValue()] = isPressed;
            	//System.out.println("AAAAAAAAAAA"+escena[15][5]+"AAAAAAAAAAAAAAAAAAA");
                break;
            case KeyEvent.VK_S:
                this.actions[MarioActions.JUMP.getValue()] = isPressed;
                break;
            case KeyEvent.VK_A:
                this.actions[MarioActions.SPEED.getValue()] = isPressed;
                break;
        }
    }

}
