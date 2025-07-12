package agents.mcts;

import engine.core.MarioForwardModel;
import java.util.List;

public class NodoMCTS {
	public MarioForwardModel model; // estado del juego
	NodoMCTS padre;
	List<NodoMCTS> hijos;
	double recompensa;
	int visitas;
	private boolean[] action;
	
	public NodoMCTS () {
		model = null;
		padre = null;
		hijos = null;
		recompensa = -1;
		visitas = -1;
		action = null;
	}
	
	// constructor completo
	public NodoMCTS (MarioForwardModel nuevo_modelo, NodoMCTS nuevo_padre, List<NodoMCTS> nuevos_hijos, double nueva_recompensa, int nuevas_visitas, boolean[] accion_padre) {
		model = nuevo_modelo;
		padre = nuevo_padre;
		hijos = nuevos_hijos;
		recompensa = nueva_recompensa;
		visitas = nuevas_visitas;
		action = accion_padre;
	}
	
	// constructor simple
	public NodoMCTS (MarioForwardModel nuevo_modelo, NodoMCTS nuevo_padre, double nueva_recompensa, boolean[] accion_padre) {
		model = nuevo_modelo;
		padre = nuevo_padre;
		hijos = null;
		recompensa = nueva_recompensa;
		visitas = 0;
		action = accion_padre;
	}
	
	// constructor de copia
	public NodoMCTS (NodoMCTS otro) {
		model = otro.model;
		padre = otro.padre;
		hijos = otro.hijos;
		recompensa = otro.recompensa;
		visitas = otro.visitas;
		action = otro.action;
	}
}
