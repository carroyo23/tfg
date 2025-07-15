package agents.mcts;

import engine.core.MarioForwardModel;
import java.util.List;

public class NodoMCTS {
	
	// dejo los atributos como public porque esta clase se usara como un struct
	
	public MarioForwardModel model; // estado del juego
	public NodoMCTS padre;
	public List<NodoMCTS> hijos;
	public float recompensa;
	public int visitas;
	public boolean[] action;
	
	public NodoMCTS () {
		model = null;
		padre = null;
		hijos = null;
		recompensa = -1;
		visitas = -1;
		action = null;
	}
	
	// constructor completo
	public NodoMCTS (MarioForwardModel nuevo_modelo, NodoMCTS nuevo_padre, List<NodoMCTS> nuevos_hijos, float nueva_recompensa, int nuevas_visitas, boolean[] accion_padre) {
		model = nuevo_modelo;
		padre = nuevo_padre;
		hijos = nuevos_hijos;
		recompensa = nueva_recompensa;
		visitas = nuevas_visitas;
		action = accion_padre;
	}
	
	// constructor simple
	public NodoMCTS (MarioForwardModel nuevo_modelo, NodoMCTS nuevo_padre, float nueva_recompensa, boolean[] accion_padre) {
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
