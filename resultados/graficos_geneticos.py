import argparse
from collections import defaultdict
import os
import csv
import numpy as np
import matplotlib.pyplot as plt
import re

def lectura_csv(file_path):
    resumen = []
    fitness_medio = 0
    mejor_fitness = -1
    mejor_genoma = []
    num_individuos = 0

    with open(file_path, 'r') as f:
        lector_csv = csv.reader(f, delimiter=",")

        for line in lector_csv:
            if mejor_fitness <= float(line[-1]):
                mejor_fitness = float(line[-1])
                mejor_genoma = [float(x) for x in line[:-2]]
            
            fitness_medio += float(line[-1])
            num_individuos += 1
        
    fitness_medio /= num_individuos
    resumen = [fitness_medio, mejor_genoma, mejor_fitness]

    return resumen

def lectura_csv_calculada(file_path):
    resumen = []
    fitness_medio = 0
    mejor_fitness = -1
    mejor_genoma = []
    num_individuos = 0

    with open(file_path, 'r') as f:
        lector_csv = csv.reader(f, delimiter=",")

        for line in lector_csv:
            resultados = [float(x) for x in (line[-2].split(";")) if x != '']
            #print(resultados)
            fitness = calculaFitness(resultados)

            if mejor_fitness <= fitness:
                mejor_fitness = fitness
                mejor_genoma = [float(x) for x in line[:-2]]
            
            fitness_medio += fitness
            num_individuos += 1
        
    fitness_medio /= num_individuos
    resumen = [fitness_medio, mejor_genoma, mejor_fitness]

    return resumen

def lectura_directorio(dir_path):
    resumenes = []

    for file_name in os.listdir(dir_path):
        file_path = os.path.join(dir_path, file_name)

        if os.path.isfile(file_path):
            resumenes.append(lectura_csv(file_path))

    return resumenes

def lectura_directorio_ordenado(dir_path):
    resumenes = []

    # obtener lista de archivos que coincidan con el patrón y extraer el número
    archivos = []
    for file_name in os.listdir(dir_path):
        match = re.match(r"poblacion_(\d+)\.csv", file_name)
        if match:
            num = int(match.group(1))
            archivos.append((num, file_name))

    # ordenar por el número X
    archivos.sort(key=lambda x: x[0])

    # leer los archivos en orden
    for _, file_name in archivos:
        file_path = os.path.join(dir_path, file_name)
        if os.path.isfile(file_path):
            resumenes.append(lectura_csv_calculada(file_path))

    return resumenes

def getFitnessMedio(resumenes):
    fitness_medio = [r[0] for r in resumenes]

    return fitness_medio

def getMejorFitness(resumenes):
    mejor = [r[2] for r in resumenes]

    return mejor

def calculaFitness(resultados):
    
    fitness = 30.0 * (resultados[2]/20000.0) + 100.0*(resultados[0]/15.0) + 70.0*(resultados[1] / 15.0) + 5.0*(resultados[3]/1000.0)

    return fitness

def pinta_grafico_fitness(datos):
    # podemos crear un array de índices para el eje x
    x = np.arange(0, 10*len(datos), 10)

    # dibujamos el gráfico
    plt.figure(figsize=(8,6))
    plt.plot(x, datos, marker='o', linestyle='-', color='blue')
    plt.title("Fitness medio por generacion")
    plt.xlabel("generacion")
    plt.ylabel("Fitness medio")
    plt.grid(True)
    plt.show()

def main():
    parser = argparse.ArgumentParser(description="Procesa csv con la informacion de cada generacion generada por los algoritmos geneticos.")
    parser.add_argument("-dir", "-d", type=str, help="Ruta al directorio con los archivos de entrada (ej: source/carpeta_datos)")
    args = parser.parse_args()

    dir_path = args.dir

    resumenes = lectura_directorio_ordenado(dir_path)
    fitness_medio = getFitnessMedio(resumenes)

    pinta_grafico_fitness(fitness_medio)
    pinta_grafico_fitness(getMejorFitness(resumenes))

if __name__ == "__main__":
    main()