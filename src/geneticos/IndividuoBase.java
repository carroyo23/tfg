package geneticos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public abstract class IndividuoBase implements Comparable<IndividuoBase> {
	public float[] genoma;
	public Resumen resultados;
	public float fitness;
	
	// el numero de genes y los normalizer seran propios de cada clase
	
	public final float PESO_NIVEL = 100f;
	public final float PESO_PORCENTAJE = 70f;
	public final float PESO_TIEMPO = 30f;
	public final float PESO_MONEDAS = 5f;
	
	// metodos comunes
	
	public IndividuoBase() {
		genoma = null;
		resultados = null;
		fitness = -1;
	}
	
	public IndividuoBase(float[] nuevo_genoma) {
		genoma = nuevo_genoma.clone();
		resultados = null;
		fitness = -1;
	}
	
	public IndividuoBase(final IndividuoBase otro) {
		genoma = otro.genoma.clone();
		fitness = otro.fitness;
		resultados = new Resumen(otro.resultados);
	}
	
	public void actualizaFitness() {
		resultados = evaluaIndividuo();
		getFitness();
	}
	
	public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }
	
	@Override
    public int compareTo(IndividuoBase otro) {
        return Float.compare(fitness, otro.fitness);
    }
	
	// los metodos que cada subclase debe implementar
	public abstract float getFitness();
	public abstract void generaRandomSol(Random generador_random);
	public abstract Resumen evaluaIndividuo();
}
