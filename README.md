Este proyecto usa el framework [Mario-AI-Framework](https://github.com/amidos2006/Mario-AI-Framework). Para más información se puede consultar su repositorio en github.

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

Para entrenar los agentes AlphaBeta o MCTS mediante un algoritmo genético ejecuta AGEAlphaBeta.java o AGEMCTS respectivamente. Estos ficheros se encuentran dentro del paquete geneticos.