Este proyecto usa el framework [Mario-AI-Framework](https://github.com/amidos2006/Mario-AI-Framework). Para más información se puede consultar su repositorio en github.

Este proyecto se centra en en crear agentes y observar su rendimiento dentro de la versión del juego Super Mario Bros implementada por el framework.

Para probar el juego ejecuta PlayLevel.java ejecutará los 15 niveles originales usando el agente AlphaBetaGenetico con los mejores parámetros encontrados.
```
level_pass = 0;
        perc_pass = 0;
        monedas_conseguidas = 0;
        tiempo_restante = 0;
        
        // probar un agente en todos los niveles
        for (int i = 1; i <= 15; i++) {
        	MarioResult result = game.runGame(new agents.alphaBetaOptimized.Agent(), getLevel("./levels/original/lvl-" + i + ".txt"), 20, 0, true);
            printResults(result);
            level_pass += (result.getGameStatus() == GameStatus.WIN) ? 1 : 0;
            perc_pass += result.getCompletionPercentage();
            tiempo_restante += (result.getGameStatus() == GameStatus.WIN) ? result.getRemainingTime() : 0;
            monedas_conseguidas += result.getCurrentCoins();
        }
        System.out.println("Niveles pasados: " + level_pass);
        System.out.println("Porcentaje pasado: " + perc_pass);
        System.out.println("Tiempo restante: " + tiempo_restante);
        System.out.println("Monedas conseguidas: " + monedas_conseguidas);
        System.out.println("********************************************************************");
```
