package agents.alphaBetaGenetico;

public class Resumen {
	public int niveles_superados = -1;
	public float porcentaje_superado = -1;
	public float tiempo_restante = -1;
	public int monedas_conseguidas = -1;
	
	public Resumen(){
		
	}
	
	public Resumen(int nuevo_nivel, float nuevo_perc, float nuevo_tiempo, int nuevo_monedas){
		niveles_superados = nuevo_nivel;
		porcentaje_superado = nuevo_perc;
		tiempo_restante = nuevo_tiempo;
		monedas_conseguidas = nuevo_monedas;
	}
	
	public Resumen(final Resumen otro) {
		niveles_superados = otro.niveles_superados;
		porcentaje_superado = otro.porcentaje_superado;
		tiempo_restante = otro.tiempo_restante;
		monedas_conseguidas = otro.monedas_conseguidas;
	}
}
