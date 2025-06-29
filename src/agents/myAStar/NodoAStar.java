package agents.myAStar;

import engine.core.MarioForwardModel;

public class NodoAStar implements Comparable{
	public int x;
	public int y;
	public int g;
	public int h;
	public MarioForwardModel model;
	
	// puntero al padre
	public NodoAStar padre;
	
	// accion que hizo el padre para llegar a el
	public boolean[] accion;
	
	public NodoAStar() {
		x = -1;
		y = -1;
		g = -1;
		h = -1;
		padre = null;
		accion = null;
		model = null;
	}
	
	public NodoAStar(int pos_x, int pos_y, MarioForwardModel nuevo_modelo) {
		x = pos_x;
		y = pos_y;
		g = 0;
		h = -1;
		padre = null;
		accion = null;
		model = nuevo_modelo;
	}
	
	public NodoAStar(int pos_x, int pos_y, int nueva_g, MarioForwardModel nuevo_modelo) {
		x = pos_x;
		y = pos_y;
		g = nueva_g;
		h = -1;
		padre = null;
		accion = null;
		model = nuevo_modelo;
	}
	
	// constructor de copia
	public NodoAStar(NodoAStar otro) {
		x = otro.x;
		y = otro.y;
		g = otro.g;
		h = otro.h;
		padre = otro.padre;
		accion = otro.accion;
		model = otro.model;
	}
	
	// solo calculo distancia horizontal para garantizar que sea admisible
	public void calculaDistancia(int meta_x, int meta_y) {
		h = (int)(Math.abs(x - meta_x));
	}
	
	public int compareTo(Object otro) {
		int comparacion = 0; // si son iguales devolvera 0
		
		int f1 = g + h;
		int f2 = ((NodoAStar)otro).g + ((NodoAStar)otro).h;
		
		if (f1 < f2) {
			comparacion = -1;
		}
		else if (f1 > f2) {
			comparacion = 1;
		}
		else { // desempate por g (coste hasta el nodo)
			if (g < ((NodoAStar)otro).g) {
				comparacion = -1;
			}
			else if (g > ((NodoAStar)otro).g) {
				comparacion = 1;
			}
		}
		
		return comparacion;
	}
	
	public int getF() {
		return g + h;
	}
}
