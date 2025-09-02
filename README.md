<p align="center">
<a href="#use">How To Use</a> &mdash; <a href="#papers">Related Papers</a> &mdash; <a href="#missing">Missing Features</a> &mdash; <a href="#copyrights">Copyrights</a>
</p>
<p align="center">
<img width="300" height="300" alt="Robin Baumgarten A* agent" src="https://raw.githubusercontent.com/amidos2006/Mario-AI-Framework/master/img/frameworkAD.gif">
</p>
<p align="center">
  <b>Current Framework Version: 0.8.0</b>
</p>

The Mario AI framework is a framework for using AI methods with a version of Super Mario Bros.

This is an updated version for the Mario AI Framework. As the first version was released in 2009, this is the tenth anniversary edition, integrating features from all previous versions and adding several new features. This new code includes a better interface for playing the game with planning algorithms (the planning track of the competition), generating levels (the level generation track), and possibly will support the learning track in the future . The framework comes with multiple different planning agents, level generators and thousands of levels including generated levels from diffeent generators as well as the original Mario levels. Also, the framework is compatible with [Video Game Level Corpus (VGLC)](https://github.com/TheVGLC/TheVGLC) processed notations.

If you want to access the old framework, feel free to check out the old websites for the previous competitions ([2015](https://sites.google.com/site/platformersai/platformer-ai-competition) - [2012](https://sites.google.com/site/noormario/home?pli=1) - [2011](https://sites.google.com/a/marioai.com/www/home) - [2009](http://julian.togelius.com/mariocompetition2009/)).

<h3 id="use">How To Use</h3>

------
#### Planning Track
Download the repo and run the [`PlayLevel.java`](https://github.com/amidos2006/Mario-AI-Framework/blob/master/src/PlayLevel.java) file. It will run [`robinBaumgarten`](https://github.com/amidos2006/Mario-AI-Framework/tree/master/src/agents/robinBaumgarten) A* agent on the [first Mario level](https://github.com/amidos2006/Mario-AI-Framework/blob/master/levels/original/lvl-1.txt) from the original Super Mario Bros. The game will run for 20 seconds (in-game time) and with Mario starting as small Mario and visuals appearing. To change the agent just change the package name of the agent in the following code
```
printResults(game.runGame(new agents.robinBaumgarten.Agent(), getLevel("levels/original/lvl-1.txt"), 20, 0, true));
```
to any of the package names that are found in [`src/agents/`](https://github.com/amidos2006/Mario-AI-Framework/tree/master/src/agents) folder, feel free to use any in your work. If you want to play a level yourself uncomment the following code in [`PlayLevel.java`](https://github.com/amidos2006/Mario-AI-Framework/blob/master/src/PlayLevel.java) file
```
//printResults(game.playGame(getLevel("levels/original/lvl-1.txt"), 200, 0));
```
and comment the agent running line from before. This code will run the framework to play the [first mario level](https://github.com/amidos2006/Mario-AI-Framework/blob/master/levels/original/lvl-1.txt) of the original Super Mario Bros with 200 tick on the game clock and with Mario starting as small mario. Feel free to change the `0` to `1` to start as Large Mario or `2` to start as Fire Mario.

------

<h3 id="copyrights">Copyrights</h3>

------
This framework is not endorsed by Nintendo and is only intended for research purposes. Mario is a Nintendo character which the authors don't own any rights to. Nintendo is also the sole owner of all the graphical assets in the game. Any use of this framework is expected to be on a non-commercial basis. This framework was created by [Ahmed Khalifa](https://scholar.google.com/citations?user=DRcyg5kAAAAJ&hl=en), based on the original Mario AI Framework by [Sergey Karakovskiy](https://scholar.google.se/citations?user=6cEAqn8AAAAJ&hl=en), [Noor Shaker](https://scholar.google.com/citations?user=OK9tw1AAAAAJ&hl=en), and [Julian Togelius](https://scholar.google.com/citations?user=lr4I9BwAAAAJ&hl=en), which in turn was based on [Infinite Mario Bros](https://fantendo.fandom.com/wiki/Infinite_Mario_Bros.) by Markus Persson.
