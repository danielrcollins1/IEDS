# IEDS

A program to automate Iterated Elimination of Dominated Strategies in game-theory analysis.

This program takes in (on the command line) one or two matrices from CSV files, representing the payoff matrices for the game in question.
If only one matrix given, then the game is assumed to be symmetric (second matrix is transpose of the first).
By default only strictly dominated strategies are eliminated; but switch -w can force elimination of weakly dominated strategies,
and switch -v will force elimination of very weakly dominated strategies. 

The author uses this to cut a large and redundant game matrix to a smaller size before sending to a bimatrix solver for Nash equilibria. 

For more explanation, see: https://en.wikipedia.org/wiki/Strategic_dominance
