package geneticos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Collections;

import engine.core.MarioResult;

//para redirigir salida a fichero para guardar resultados
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Random;

public class AGEAlphaBeta {
	
	public static Random generador_random = new Random(42);
	
	// el operador de seleccion sera por torneo de 3 individuos
	public static List<IndividuoAlphaBeta> op_seleccion(final List<IndividuoAlphaBeta> poblacion, int tam_reemplazo) {
		List<IndividuoAlphaBeta> nuevos = new ArrayList<>();
		
		int num_individuos = poblacion.size();
		int pos1 = -1, pos2 = -1, pos3 = -1; // las posiciones de los individuos en la poblacion
		float fit1, fit2, fit3; // las puntuaciones de cada individuo
		float max; // para calcular el mejor individuo
		
		for (int i = 0; i < tam_reemplazo; i++) {
			pos1 = -1;
			pos2 = -1;
			pos3 = -1;
			
			// escojo 3 individuos aleatoriamente asegurandome de que sean distintos
	        while((pos1 == pos2) || (pos1 == pos3) || (pos2 == pos3)){
	        	pos1 = generador_random.nextInt(0, num_individuos);
				pos2 = generador_random.nextInt(0, num_individuos);
				pos3 = generador_random.nextInt(0, num_individuos);
	        }
	        
	        fit1 = poblacion.get(pos1).getFitness();
	        fit2 = poblacion.get(pos2).getFitness();
	        fit3 = poblacion.get(pos3).getFitness();
	        
	        // me quedo con el que mejor fitness tenga
	        max = Math.max(fit1, fit2);
	        max = Math.max(max, fit3);
	        
	        if (fit1 == max){
	            nuevos.add(new IndividuoAlphaBeta(poblacion.get(pos1)));
	        }
	        else if (fit2 == max){
	            nuevos.add(new IndividuoAlphaBeta(poblacion.get(pos2)));   
	        }
	        else{
	            nuevos.add(new IndividuoAlphaBeta(poblacion.get(pos3)));
	        }
		}
		
		return nuevos;
	}
	
	// operador de cruce (cruza 2 padres para generar 2 hijos) (solo paso los genomas porque el resto no lo necesito)
	// se usara el cruce BLX-alpha
	public static List<IndividuoAlphaBeta> op_cruce_BLX(final float[] uno, final float[] otro, float alpha) {
		List<IndividuoAlphaBeta> hijos = new ArrayList<IndividuoAlphaBeta>();
		
		float min, max, diff;
		float[] primer_genoma = uno.clone();
		float[] segundo_genoma = otro.clone();
		
		// inicializo cada hijo a un padre
		
		// itero sobre cada gen
		for (int i = 0; i < primer_genoma.length; i++) {
			
			// calculo la diferencia, el maximo y el minimo
			
			min = Math.min(primer_genoma[i], segundo_genoma[i]);
			max = Math.max(primer_genoma[i], segundo_genoma[i]);
			diff = max - min;
			
			// asigno en cada gen un valor aleatorio dentro del rango que marca el operador de cruce BLX-alpha
			if (diff != 0) {
				primer_genoma[i] = (float) generador_random.nextDouble(min - alpha*diff, max + alpha*diff);
				segundo_genoma[i] = (float) generador_random.nextDouble(min - alpha*diff, max + alpha*diff);
			}
		}
		
		// relleno los hijos (aniado el genoma y dejo los resultados como indeterminados hasta que se evaluen
		hijos.add(new IndividuoAlphaBeta(primer_genoma));
		hijos.add(new IndividuoAlphaBeta(segundo_genoma));
		
		return hijos;
	}
	
	
	public static IndividuoAlphaBeta op_mutacion(final float [] a_mutar, float delta) {
		IndividuoAlphaBeta mutado;
		float [] nuevo_genoma = a_mutar.clone();
		
		// cojo un gen aleatorio y lo muto
		int pos_a_mutar = generador_random.nextInt(a_mutar.length);
		
		// para mutarlo le sumare una componente z sacado de una distribucion N(0, delta^2)
		float z = (float)generador_random.nextGaussian(0, delta*delta);
		
		// creo al nuevo individuo a partir de la mutacion asegurandome de que sea valida, es decir, este en el intervalo [0,1]
		nuevo_genoma[pos_a_mutar] = Math.max(0, Math.min(1, nuevo_genoma[pos_a_mutar] + z));
		mutado = new IndividuoAlphaBeta(nuevo_genoma);
		
		return mutado;
	}
	
	public static float getFitnessMedio(List<IndividuoAlphaBeta> poblacion) {
		float a_devolver = 0;
		
		for (int i = 0; i < poblacion.size(); i++) {
			a_devolver += poblacion.get(i).fitness;
		}
		
		return a_devolver / ((float)poblacion.size());
	}
	
	public List<IndividuoAlphaBeta> leePoblacionFichero(String path_fichero){
		List<IndividuoAlphaBeta> nueva_poblacion = new ArrayList();
		IndividuoAlphaBeta nuevo;

        try (BufferedReader br = new BufferedReader(new FileReader(path_fichero))) {
            String linea;
            float [] genoma;
            while ((linea = br.readLine()) != null) {
                // Separar por comas
                String[] valores = linea.split(",");
                genoma = new float [valores.length - 1];
                for (int i = 0; i < (valores.length - 1); i++) {
                    genoma[i] = Float.parseFloat(valores[i]);
                }
                nuevo = new IndividuoAlphaBeta(genoma);
                nuevo.fitness = Float.parseFloat(valores[valores.length - 1]);
                // TODO: LEER LOS RESUMENES
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return nueva_poblacion;
	}
	
	// algoritmo genetico estacionario
	public static IndividuoAlphaBeta AGE() {
		final int NUM_INDIVIDUOS = 50; // tamaño de la poblacion
		final int MAX_EVAL = 1000; // maximo de soluciones a evaluar
		final int NUM_HIJOS = 2; // numero de hijos que devolvera el operador de seleccion (al ser estacionario solo 2)
		final float PROB_CRUCE = 1.0f;
		final float PROB_MUTA = 0.08f;
		final float ALPHA = 0.3f; // alpha para el operador de cruce BLX-alpha que sera BLX-0.3
		
		int num_eval = 0;
		
		// usare una lista para ir quitando y metiendo individuos con facilidad
		List<IndividuoAlphaBeta> poblacion = new ArrayList<IndividuoAlphaBeta>();
		List<IndividuoAlphaBeta> nuevos; // la nueva generacion
		
		// ReverseOrder porque quiero sacar los mejores del torneo por lo que ordeno de mayor a menor
		PriorityQueue<IndividuoAlphaBeta> torneo = new PriorityQueue<IndividuoAlphaBeta>(Comparator.reverseOrder());
		
		int contador = 0;
		float fitness_medio = 0;
		
		// genero la poblacion inicial de manera aleatoria
		for (int i = 0; i < NUM_INDIVIDUOS; i++) {
			IndividuoAlphaBeta nuevo = new IndividuoAlphaBeta();
			nuevo.generaRandomSol(generador_random);
			poblacion.add(nuevo);
			System.out.println("GENERADO INDIVIDO: " + i);
			num_eval++;
		}
		
		System.out.println("FIN INCIACION");
		System.out.println("Fitness medio: " + getFitnessMedio(poblacion));
		
		
		try {
	      	  PrintWriter salida_fichero = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\tfg\\resultados\\genetico\\alphaBeta\\prueba_refactor_otro.txt"));
		
			while (num_eval <= MAX_EVAL) {
				
				// cada 10 evaluaciones guardo la poblacion en un fichero
				if ((num_eval % 10) == 0) {
					try {
						PrintWriter fichero_poblacion = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\tfg\\resultados\\genetico\\alphaBeta\\poblacion_" + num_eval + ".csv"));
						
						for (int i = 0; i < poblacion.size(); i++) {
							for (int j = 0; j < (poblacion.get(i).genoma.length); j++) {
								fichero_poblacion.print(poblacion.get(i).genoma[j] + ",");
							}
							// TODO: GUARDAR LOS RESUMENES
							fichero_poblacion.println(poblacion.get(i).getFitness()); // la ultima columna sera el fitness para no tener que recalcularlo luego
						}
						
						fichero_poblacion.close();
					}
					catch (IOException e) {
			        	e.printStackTrace();
			        }
				}
				
				// selecciono 2 padres
				nuevos = op_seleccion(poblacion, NUM_HIJOS);
				
				// los cruzo
				nuevos = op_cruce_BLX(nuevos.get(0).genoma, nuevos.get(1).genoma, ALPHA);
				
				// los muto o no segun la probabilidad de cruce
				for (IndividuoAlphaBeta hijo : nuevos) {
					if (generador_random.nextInt(100) < (PROB_MUTA*100)) {
						hijo = op_mutacion(hijo.genoma, 0.05f);
					}
					
					// al haberse cruzado, muten o no, actualizo su fitness
					hijo.actualizaFitness();
					num_eval++;
				}
				
				// hago un torneo con los peores de la poblacion y los hijos
				for (int i = 0; i < NUM_HIJOS; i++) {
					torneo.add(nuevos.get(i));
					IndividuoAlphaBeta peor = Collections.min(poblacion, Comparator.comparingDouble(j -> j.fitness));
					poblacion.remove(peor);
					torneo.add(peor);
				}
				
				// me quedo con los 2 mejores
				for (int i = 0; i < NUM_HIJOS; i++) {
					poblacion.add(torneo.element());
					torneo.poll();
				}
				
				IndividuoAlphaBeta mejor = Collections.max(poblacion, Comparator.comparingDouble(i -> i.fitness));
				
				System.out.println("**************************************************************");
				System.out.println("FITNESS MEDIO GENERACION " + contador + ":" + getFitnessMedio(poblacion));
				System.out.println("MEJOR INDIVIDUO GENERACION " + contador + ":");
				System.out.println("Horizontal: " + mejor.genoma[0]);
				System.out.println("Vertical: " + mejor.genoma[1]);
				System.out.println("kill: " + mejor.genoma[2]);
				System.out.println("monedas: " + mejor.genoma[3]);
				
				System.out.println("Niveles superados: " + mejor.resultados.niveles_superados);
				System.out.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
				System.out.println("**************************************************************");
				
				salida_fichero.println("**************************************************************");
				salida_fichero.println("FITNESS MEDIO GENERACION " + contador + ":" + getFitnessMedio(poblacion));
				salida_fichero.println("MEJOR INDIVIDUO GENERACION " + contador + ":");
		      	salida_fichero.println("Horizontal: " + mejor.genoma[0]);
		      	salida_fichero.println("Vertical: " + mejor.genoma[1]);
		      	salida_fichero.println("kill: " + mejor.genoma[2]);
		      	salida_fichero.println("monedas: " + mejor.genoma[3]);
				
		      	salida_fichero.println("Niveles superados: " + mejor.resultados.niveles_superados);
		      	salida_fichero.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
		      	salida_fichero.println("**************************************************************");
		      	
		      	contador++;
		      	/*
		      	System.out.println("//////////////////////////////////////////////////////////////////");
				for (int i = 0; i < poblacion.size(); i++) {
					System.out.println("Individuo " + i + ": Pasa: " + poblacion.get(i).resultados.niveles_superados + " Porcentaje: " + poblacion.get(i).resultados.porcentaje_superado);
					System.out.println("Genes: " + poblacion.get(i).genoma[0] + " " + poblacion.get(i).genoma[1] + poblacion.get(i).genoma[2] + " " + poblacion.get(i).genoma[3]);
				}
				System.out.println("//////////////////////////////////////////////////////////////////");
				*/
			}
		
			salida_fichero.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		
		return Collections.max(poblacion, Comparator.comparingDouble(i -> i.fitness));
	}
	
	public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }
	
	public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }
	
	public static void main(String[] args) {
		
		IndividuoAlphaBeta mejor = AGE();
		
		System.out.println("Horizontal: " + mejor.genoma[0]);
		System.out.println("Vertical: " + mejor.genoma[1]);
		System.out.println("kill: " + mejor.genoma[2]);
		System.out.println("monedas: " + mejor.genoma[3]);
		
		System.out.println("Niveles superados: " + mejor.resultados.niveles_superados);
		System.out.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
		
		try {
	      	  PrintWriter salida_fichero = new PrintWriter(new FileWriter("C:\\Users\\Usuario\\Desktop\\uni\\TFG\\tfg\\resultados\\genetico\\alphaBeta\\prueba_refactor2.txt"));
	      	  
	      	salida_fichero.println("Horizontal: " + mejor.genoma[0]);
	      	salida_fichero.println("Vertical: " + mejor.genoma[1]);
	      	salida_fichero.println("kill: " + mejor.genoma[2]);
	      	salida_fichero.println("monedas: " + mejor.genoma[3]);
			
	      	salida_fichero.println("Niveles superados: " + mejor.resultados.niveles_superados);
	      	salida_fichero.println("Porcentaje superado: " + mejor.resultados.porcentaje_superado);
	      	  
	      	  salida_fichero.close();
	        } catch (IOException e) {
	      	  e.printStackTrace();
	        }
	}
}
