import re
import argparse
from collections import defaultdict
from typing import Dict, List, Tuple
import os

import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import numpy as np

"""
el formato de la linea sera el siguiente:
Nivel 1 porcentaje: 1.0 Tiempo restante: 8330 Monedas: 3;Nivel 2 porcentaje: 1.0 Tiempo restante: 11090 Monedas: 5
"""
def get_info_line_nivel(line):
    lines = line.split(";")

    resumen_nivel = dict()

    for nivel in lines:
        if nivel:
            partes = nivel.split(":")
            num_nivel = int((partes[0].lstrip("Nivel ")).rstrip(" porcentaje"))
            porcentaje = float((partes[1].lstrip(" ")).rstrip(" Tiempo restante"))
            tiempo = float((partes[2].lstrip(" ")).rstrip(" Monedas"))
            monedas = int((partes[3].lstrip(" ")).rstrip())

            resumen_nivel[f'resumen_{num_nivel}'] = {
                'nivel': num_nivel,
                'porcentaje_pasado': porcentaje,
                'tiempo_restante': tiempo,
                'monedas_conseguidas': monedas
            }

    return resumen_nivel

def lectura_fichero(file_path):

    response = {'status': False, 'valor_principal': '', 'resumen_principal': dict()}

    with open (file_path, 'r') as f:
        # las 2 primeras lineas son de ********** por lo que las descarto
        next(f)
        next(f)

        # en la tercera linea esta el valor fijo para el fichero
        response['status'] = True
        response['valor_principal'] = float(next(f).split(':')[1].strip())

        valor_principal = response['valor_principal']

        nuevo_diccionario = False
        resumen_principal = dict()
        resumen_secundario = None
        resumen_nivel = dict()
        puntuacion_secundaria = -1
        linea_niveles = ""

        for line in f:
            fixed_line = line.strip()

            # creo una nueva entrada al diccionario
            if nuevo_diccionario or (fixed_line.startswith('Puntuacion por')):
                nuevo_diccionario = False
                puntuacion_secundaria = float(fixed_line.split(':')[1].strip())

                resumen_secundario = {'valor_secundario': puntuacion_secundaria}
                continue

            # se ha acabado el resumen de este valor
            if (fixed_line == "///////////////////////////////////////////////////////////"):
                # creo un nuevo diccionario
                nuevo_diccionario = True

                # guardo el que haya creado
                if resumen_secundario:
                    resumen_principal[f'resumen_secundario_{puntuacion_secundaria}'] = resumen_secundario
                    resumen_secundario = None
                    resumen_nivel = None
                    puntuacion_secundaria = -1
                continue

            if (fixed_line.startswith("Nivel ")):
                linea_niveles += fixed_line + ";"

            elif (fixed_line.startswith("Niveles")):
                resumen_nivel = get_info_line_nivel(linea_niveles)
                linea_niveles = ""
                resumen_secundario['niveles_pasados'] = int(fixed_line.split(':')[1].strip())
                resumen_secundario['resumen_nivel'] = resumen_nivel
                resumen_nivel = None

            elif (fixed_line.startswith("Porcentaje")):
                resumen_secundario['porcentaje_pasado'] = float(fixed_line.split(':')[1].strip())

            elif (fixed_line.startswith("Tiempo")):
                resumen_secundario['tiempo_restante'] = float(fixed_line.split(':')[1].strip())

            elif (fixed_line.startswith("Monedas")):
                resumen_secundario['monedas_conseguidas'] = float(fixed_line.split(':')[1].strip())

    resumen_global = {'valor_principal': valor_principal, 'resumen_principal': resumen_principal}

    return resumen_global

def lectura_directorio(dir_path):
    resumenes = []

    for file_name in os.listdir(dir_path):
        file_path = os.path.join(dir_path, file_name)

        if os.path.isfile(file_path):
            resumenes.append(lectura_fichero(file_path))

    return resumenes

def extraccion_estadistica(resumenes, nombre_estadistica):
    resultado = []

    for resumen in resumenes:

        valor_principal = resumen.get('valor_principal', 0)
        resumen_principal = resumen.get('resumen_principal', {})

        for clave_secundaria, resumen_secundario in resumen_principal.items():
            valor_secundario = resumen_secundario.get('valor_secundario', 0)
            estadistica = resumen_secundario.get(nombre_estadistica, 0)

            resultado.append([valor_principal, valor_secundario, estadistica])

    return resultado

# devolvera un array de arrays de la siguiente forma:
# [[valor_principal, valor_secundario, porcentaje], [valor_principal, valor_secundario, porcentaje]]
def extraccion_porcentaje_pasado(resumenes):

    resultado = extraccion_estadistica(resumenes, 'porcentaje_pasado')

    return resultado

# nota: aqui solo hay que tener en cuenta los de los niveles superados
def extraccion_tiempo_restante(resumenes):
    resultado = []
    
    for resumen in resumenes:

        valor_principal = resumen.get('valor_principal', 0)
        resumen_principal = resumen.get('resumen_principal', {})

        for clave_secundaria, resumen_secundario in resumen_principal.items():
            valor_secundario = resumen_secundario.get('valor_secundario', 0)
            resumen_nivel = resumen_secundario.get('resumen_nivel', {})
                
            tiempo_total = 0
            for nivel_data in resumen_nivel.values():
                if nivel_data.get('porcentaje_pasado', 0) == 1.0:
                    tiempo_total += nivel_data.get('tiempo_restante', 0)

            resultado.append([valor_principal, valor_secundario, tiempo_total])

    return resultado

def extraccion_monedas_conseguidas(resumenes):
    return extraccion_estadistica(resumenes, 'monedas_conseguidas')

# nota: esto sera una mezcla entre el tiempo restante, las monedas conseguidas
# y porcentaje superado
def extraccion_puntuacion_conseguida(resumenes):
    monedas_conseguidas = extraccion_monedas_conseguidas(resumenes)
    tiempo_restante = extraccion_tiempo_restante(resumenes)

    puntuacion = []
    for m, t in zip(monedas_conseguidas, tiempo_restante):
        valor_principal = m[0]
        valor_secundario = m[1]
        suma = m[2] + t[2]
        puntuacion.append([valor_principal, valor_secundario, suma])

    return puntuacion

def extraccion_fitness(resumenes):
    monedas_conseguidas = np.array(extraccion_monedas_conseguidas(resumenes))
    tiempo_restante = np.array(extraccion_tiempo_restante(resumenes))
    progreso = np.array(extraccion_porcentaje_pasado(resumenes))
    niveles_superados = np.array(extraccion_estadistica(resumenes, 'niveles_pasados'))

    fitness = np.array(monedas_conseguidas)
    fitness[:,2] = 30.0 * (tiempo_restante[:,2]/300000.0) + 100.0*(niveles_superados[:,2]/15.0) + 70.0*(progreso[:,2] / 15.0) + 5.0*(monedas_conseguidas[:,2]/1000.0)
    
    return fitness

def pinta_grafico(datos, nombre_valor_principal, nombre_valor_secundario, nombre_resultado, nombre_grafico, etiqueta):
    # Separar en 3 listas
    x = [d[0] for d in datos]  # valor_principal
    y = [d[1] for d in datos]  # valor_secundario
    z = [d[2] for d in datos]  # porcentaje

    # Crear figura y ejes 3D
    fig = plt.figure(figsize=(8,6))
    ax = fig.add_subplot(111, projection='3d')

    # Graficar puntos
    sc = ax.scatter(x, y, z, c=z, cmap='viridis', s=50)

    # Etiquetas de ejes
    ax.set_xlabel(nombre_valor_principal)
    ax.set_ylabel(nombre_valor_secundario)
    ax.set_zlabel(nombre_resultado)
    ax.set_title(nombre_grafico)

    # Barra de color
    fig.colorbar(sc, ax=ax, label=etiqueta)

    plt.tight_layout()
    plt.show()

def pinta_grafico_malla(datos):
    # Convertimos a arrays de numpy
    datos = np.array(datos)

    # Extraemos columnas
    valores_principales = np.unique(datos[:, 0])  # X
    valores_secundarios = np.unique(datos[:, 1])  # Y

    # Creamos grillas de X, Y
    X, Y = np.meshgrid(valores_principales, valores_secundarios)

    # Creamos la matriz Z correspondiente (porcentaje pasado)
    Z = np.zeros_like(X)

    for i in range(len(valores_secundarios)):
        for j in range(len(valores_principales)):
            # Buscamos el valor Z correspondiente a esa combinación
            match = datos[(datos[:, 0] == X[i, j]) & (datos[:, 1] == Y[i, j])]
            Z[i, j] = match[0, 2] if len(match) > 0 else np.nan  # maneja datos faltantes

    # Graficar
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    # Superficie
    surf = ax.plot_surface(X, Y, Z, cmap='coolwarm', edgecolor='k')

    # Etiquetas
    ax.set_xlabel('Valor Principal')
    ax.set_ylabel('Valor Secundario')
    ax.set_zlabel('Porcentaje Pasado')
    ax.set_title('Superficie 3D de Porcentaje Pasado')

    # Barra de color
    fig.colorbar(surf, ax=ax, shrink=0.5, aspect=10, label='Porcentaje')

    plt.tight_layout()
    plt.show()

#los datos aleatorios la segunda variable seran las repeticiones por lo que solo me interesa la primera
def pinta_grafico_repeticiones(datos):
    # Agrupar porcentajes por valor_principal
    grupos = defaultdict(list)
    for valor_principal, _, porcentaje in datos:
        grupos[valor_principal].append(porcentaje)

    # Calcular medias
    x = []
    y = []
    for valor in sorted(grupos):
        x.append(valor)
        y.append(np.mean(grupos[valor]))

    # Graficar
    plt.figure(figsize=(10, 6))
    plt.plot(x, y, marker='o', linestyle='-', color='purple')
    plt.xlabel('Cp')
    plt.ylabel('Media fitness conseguido')
    plt.title('Media de fitness conseguido por Cp')
    plt.grid(True)
    plt.tight_layout()
    plt.ylim(0)
    plt.show()

def pinta_tabla(datos, nombre_fil, nombre_col, nombre_val, nombre_tabla):

    # Obtenemos los valores únicos de filas (v1) y columnas (v2)
    rows = np.unique(datos[:,0])
    cols = np.unique(datos[:,1])

    # Creamos una matriz vacía para la tabla
    table = np.full((len(rows), len(cols)), np.nan)

    # Rellenamos la matriz con los valores de v3
    for v1, v2, v3 in datos:
        i = np.where(rows == v1)[0][0]
        j = np.where(cols == v2)[0][0]
        table[i, j] = v3

    # Graficamos el heatmap
    fig, ax = plt.subplots(figsize=(8,6))
    im = ax.imshow(table, cmap="viridis")

    # Etiquetas de filas y columnas
    ax.set_xticks(np.arange(len(cols)))
    ax.set_yticks(np.arange(len(rows)))
    ax.set_xticklabels(cols)
    ax.set_yticklabels(rows)

    # Rotamos etiquetas de columnas para mejor legibilidad
    plt.setp(ax.get_xticklabels(), rotation=45, ha="right", rotation_mode="anchor")

    # Mostramos los valores dentro de cada celda
    for i in range(len(rows)):
        for j in range(len(cols)):
            val = table[i, j]
            if not np.isnan(val):
                ax.text(j, i, f"{val:.2f}", ha="center", va="center", color="black")

    # Barra de color
    fig.colorbar(im, label=nombre_val)

    plt.xlabel(nombre_col)
    plt.ylabel(nombre_fil)
    plt.title(nombre_tabla)
    plt.gca().invert_yaxis()
    plt.show()

def main():
    parser = argparse.ArgumentParser(description="Procesa ficheros de puntuación con combinaciones de parámetros.")
    parser.add_argument("-dir", "-d", type=str, help="Ruta al directorio con los archivos de entrada (ej: source/carpeta_datos)")
    args = parser.parse_args()

    resumenes = lectura_directorio(args.dir)
    #print(resumenes)
    lista_cantidad_superada = extraccion_porcentaje_pasado(resumenes)
    lista_monedas = extraccion_monedas_conseguidas(resumenes)
    lista_tiempo = extraccion_tiempo_restante(resumenes)
    lista_puntuacion = extraccion_puntuacion_conseguida(resumenes)
    lista_fitness = np.array(extraccion_fitness(resumenes))
    #print(lista_monedas)

    lista_porcentajes = np.array(lista_cantidad_superada)
    lista_porcentajes[:,2] = lista_porcentajes[:,2] * 100.0 / 15.0

    #print(lista_porcentajes)
    print("Porcentaje: ", np.mean(lista_porcentajes[:,2]))
    print("Fitness: ", np.mean(lista_fitness[:,2]))
    print("Niveles: ", np.mean(np.array(extraccion_estadistica(resumenes, 'niveles_pasados'))[:,2]))
    print("********************************************")
    #print(lista_fitness)
    #pinta_grafico_repeticiones(lista_porcentajes) # para mcts
    #pinta_grafico_repeticiones(lista_fitness)

    
    #pinta_grafico(lista_puntuacion)
    #pinta_grafico_malla(lista_puntuacion)

    #pinta_grafico(lista_tiempo)
    #pinta_grafico_malla(lista_tiempo)
    
    #pinta_grafico(lista_porcentajes, 'Valor Horizontal', 'valor Vertical', 'Porcentaje superado', 'Visualizacion en 3D del porcentaje superado', 'Porcentaje superado')
    #pinta_grafico_malla(lista_porcentajes)

    #pinta_grafico(lista_fitness, 'Valor Horizontal', 'valor Vertical', 'fitness', 'Visualizacion en 3D del fitness', 'fitness')
    #pinta_grafico_malla(lista_fitness)

    #pinta_tabla(lista_porcentajes, "Valor Kill", "Valor Monedas", "Porcentaje superado", "Heatmap del porcentaje superado")
    #pinta_tabla(lista_fitness, "Valor Kill", "Valor Monedas", "fitness conseguido", "Heatmap del fitness conseguido")

    #pinta_grafico(lista_monedas)
    #pinta_grafico_malla(lista_monedas)
    
    


if __name__ == "__main__":
    main()

