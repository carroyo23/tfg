Este proyecto usa el framework [Mario-AI-Framework](https://github.com/amidos2006/Mario-AI-Framework). A partir de este se han creado nuevos agentes y un algoritmo genético para poder crear agentes mediante aprendizaje por refuerzo.

Este proyecto se centra en en crear agentes y observar su rendimiento dentro de la versión del juego Super Mario Bros implementada por el framework.

Para probar el juego ejecuta [`PlayLevel.java`](https://github.com/carroyo23/tfg/blob/master/src/PlayLevel.java) esto ejecutará los 15 niveles originales usando el agente [`AlphaBetaGenetico`](https://github.com/carroyo23/tfg/blob/master/src/agents/alphaBetaGenetico/Agent.java) con los mejores parámetros encontrados.
Para cambiar el agente en el que probarlo simplemente cambia el agente por cualquiera de los que hay en el paquete agents en la siguiente linea:
```
MarioResult result = game.runGame(new agents.alphaBetaOptimized.Agent(), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
```

Para probar un agente en un solo nivel comenta el código actual y descomenta la línea:
```
//printResults(game.runGame(new agents.alphaBeta.Agent(), getLevel("./levels/original/lvl-2.txt"), 20, 0, true));
```

También puedes jugar cualquier nivel descomentando la linea:
```
//printResults(game.playGame(getLevel("./levels/original/lvl-2.txt"), 200, 0));
```

Se puede cambiar el nivel por cualquiera de los que hay implementados en la carpera levels en cualquiera de las subcarpetas simplemente cambiando la ruta.

Para entrenar los agentes AlphaBeta o MCTS mediante un algoritmo genético ejecuta [`AGEAlphaBeta.java`](https://github.com/carroyo23/tfg/blob/master/src/geneticos/AGEAlphaBeta.java) o [`AGEMCTS.java`](https://github.com/carroyo23/tfg/blob/master/src/geneticos/AGEMCTS.java) respectivamente.

<h3 id="copyrights">Copyrights</h3>

------
Este proyecto no está avalado por Nintendo y tiene como único fin la investigación. Mario es un personaje de Nintendosobre el cual no se posee ningún derecho. Nintendo es también el único propietario de todos los recursos gráficos del juego. Se espera que cualquier uso de este proyecto sea de caracter no comercil. Este proyecto se basa en el framework creado por [Ahmed Khalifa](https://scholar.google.com/citations?user=DRcyg5kAAAAJ&hl=en), basado a su vez en el Mario AI Framework creado por [Sergey Karakovskiy](https://scholar.google.se/citations?user=6cEAqn8AAAAJ&hl=en), [Noor Shaker](https://scholar.google.com/citations?user=OK9tw1AAAAAJ&hl=en), y [Julian Togelius](https://scholar.google.com/citations?user=lr4I9BwAAAAJ&hl=en), que se basaba en [Infinite Mario Bros](https://fantendo.fandom.com/wiki/Infinite_Mario_Bros.) por Markus Persson.