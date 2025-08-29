import argparse
from collections import defaultdict
import os
import csv
import numpy as np
import matplotlib.pyplot as plt
import re
import pandas as pd

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
    mejor_porcentaje = 0
    porcentaje_medio = 0
    mejor_nivel_superado = -1

    with open(file_path, 'r') as f:
        lector_csv = csv.reader(f, delimiter=",")

        for line in lector_csv:
            resultados = [float(x) for x in (line[-2].split(";")) if x != '']
            #print(resultados)
            fitness = calculaFitness(resultados)
            porcentaje = resultados[1]

            if mejor_fitness <= fitness:
                mejor_fitness = fitness
                mejor_genoma = [float(x) for x in line[:-2]]
                mejor_porcentaje = porcentaje
                mejor_nivel_superado = resultados[0]
            
            fitness_medio += fitness
            porcentaje_medio += porcentaje
            num_individuos += 1
        
    fitness_medio /= num_individuos
    porcentaje_medio /= num_individuos
    resumen = [fitness_medio, mejor_genoma, mejor_fitness, porcentaje_medio, mejor_porcentaje, mejor_nivel_superado]

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
    
    fitness = 100.0*(resultados[0]/15.0) + 70.0*(resultados[1] / 15.0) + 30.0 * (resultados[2]/300000.0) + 5.0*(resultados[3]/1000.0)

    return fitness

def getPorcentajeMedio(resumenes):
    a_devolver = [(r[3] * 100.0) / 15.0 for r in resumenes]

    return a_devolver

def getMejorPorcentaje(resumenes):
    a_devolver = [(r[4] * 100.0) / 15.0 for r in resumenes]

    return a_devolver

def pinta_grafico_fitness(datos, titulo, nombre_ejeX, nombre_ejeY):
    # podemos crear un array de índices para el eje x
    x = np.arange(50, 10*len(datos)+50, 10)

    # dibujamos el gráfico
    plt.figure(figsize=(8,6))
    plt.plot(x, datos, marker='o', linestyle='-', color='purple')
    plt.title(titulo)
    plt.xlabel(nombre_ejeX)
    plt.ylabel(nombre_ejeY)
    plt.grid(True)

    # Aseguramos que el eje Y siempre incluya el 0
    plt.ylim(0, 200)

    plt.show()

def pinta_grafico_porcentaje(datos, titulo, nombre_ejeX, nombre_ejeY):
    # podemos crear un array de índices para el eje x
    x = np.arange(50, 10*len(datos)+50, 10)

    # dibujamos el gráfico
    plt.figure(figsize=(8,6))
    plt.plot(x, datos, marker='o', linestyle='-', color='purple')
    plt.title(titulo)
    plt.xlabel(nombre_ejeX)
    plt.ylabel(nombre_ejeY)
    plt.grid(True)

    # Aseguramos que el eje Y siempre incluya el 0
    plt.ylim(0, 100)

    plt.show()

def graf_doble(datos1, datos2, datos3, datos4, titulo, nombre_ejeX, nombre_ejeY, max_y):
    # podemos crear un array de índices para el eje x
    x = np.arange(50, 10*len(datos1)+50, 10)
    x2 = np.arange(50, 10*len(datos2)+50, 10)

    # dibujamos el gráfico
    plt.figure(figsize=(8,6))
    plt.plot(x, datos1, marker='o', linestyle='-', color='purple', label="AlphaBeta media generacion")
    plt.plot(x2, datos2, marker='o', linestyle='-', color='green', label="MCTS media generacion")
    plt.plot(x, datos3, marker='o', linestyle='-', color='red', label="AlphaBeta mejor individuo generacion")
    plt.plot(x2, datos4, marker='o', linestyle='-', color='black', label="MCTS mejor individuo generacion")
    plt.title(titulo)
    plt.xlabel(nombre_ejeX)
    plt.ylabel(nombre_ejeY)
    plt.grid(True)

    # Aseguramos que el eje Y siempre incluya el 0
    plt.ylim(0, max_y)
    plt.legend()

    plt.show()

def main():
    parser = argparse.ArgumentParser(description="Procesa csv con la informacion de cada generacion generada por los algoritmos geneticos.")
    parser.add_argument("-dir", "-d", type=str, help="Ruta al directorio con los archivos de entrada (ej: source/carpeta_datos)")
    parser.add_argument("-dir2", "-d2", type=str, help="Ruta al directorio con los archivos de entrada del segundo agente (ej: source/carpeta_datos)")
    args = parser.parse_args()

    dir_path = args.dir
    otro_dir_path = args.dir2

    resumenes = lectura_directorio_ordenado(dir_path)
    fitness_medio = getFitnessMedio(resumenes)

    #otros_resumenes = lectura_directorio_ordenado(otro_dir_path)

    resumen_fin = lectura_csv_calculada(os.path.join(dir_path, "poblacion_910.csv"))

    #print(resumen_fin)
    #print((resumen_fin[4] * 100.0) / 15.0)

    # Lee el CSV
    df = pd.read_csv(os.path.join(otro_dir_path, "resultados_finales.csv"))

    # Convierte a tabla en LaTeX
    latex_table = df.to_latex(index=False)  

    # Guarda en un .tex
    with open("tabla.tex", "w") as f:
        f.write(latex_table)

    print(latex_table)

    """
    pinta_grafico_fitness(fitness_medio, "Fitness medio de la generación", "Número de evaluaciones", "Fitness medio")
    pinta_grafico_fitness(getMejorFitness(resumenes), "Fitness del mejor individuo de la generación", "Número de evaluaciones", "Fitness mejor individuo")

    pinta_grafico_porcentaje(getPorcentajeMedio(resumenes), "Porcentaje superado medio de la generación", "Número de evaluaciones", "Porcentaje superado medio")
    pinta_grafico_porcentaje(getMejorPorcentaje(resumenes), "Porcentaje superado del mejor individuo de la generación", "Número de evaluaciones", "Porcentaje superado mejor individuo")
    """

    #graf_doble(getFitnessMedio(resumenes), getFitnessMedio(otros_resumenes), getMejorFitness(resumenes), getMejorFitness(otros_resumenes), "Comparacion fitness", "Número de evaluaciones", "Fitness", 200)

    #graf_doble(getPorcentajeMedio(resumenes), getPorcentajeMedio(otros_resumenes), getMejorPorcentaje(resumenes), getMejorPorcentaje(otros_resumenes), "Comparacion porcentaje", "Número de evaluaciones", "Porcentaje superado", 100)

if __name__ == "__main__":
    main()