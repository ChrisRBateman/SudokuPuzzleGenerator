SudokuPuzzleGenerator
=====================

#### Java Sudoku Puzzle Generator

Generates a series of standard Sudoku puzzles and writes them to a file. Each puzzle has 
the following format:

```
..2.847...8...7.3.713.9.......5...8124.8....7..8...4.29..42...3....13.94.64.....8
592384716486157239713296845679542381245831967138769452951428673827613594364975128
```

The first line is the partial grid and the second line is the solution.
The solution is defined by the following rules:
 
 - The board is 9 by 9 grid of cells.
 - Each row and column contains the numbers 1 to 9. Each number from 1 to 9 appears only once.
 - The board is also divided into nine sub-grids. Each sub-grid is a 3 by 3 rectangle containing 
   the numbers 1 to 9. Each number in the sub-grid also appears only once.
   
This application is based on code from:

https://medium.com/@rossharrison/generating-sudoku-boards-pt-1-structures-algorithms-a1e62feeb32

and

https://github.com/rharriso/sudoku-gen-cpp

For general information about Sudoku see:

https://en.wikipedia.org/wiki/Sudoku
   
How to run:

Install JDK SE 8 or later http://www.oracle.com/technetwork/java/javase/downloads/index.html

Run from a command prompt to build:

```
$ javac SudokuGenerator.java
```
	
Then run (for example):

```
$ java SudokuGenerator 5 38 output.txt
```

The first parameter is the number of puzzles to generate.

The second parameter is the number of visible cells per puzzle.	

The third parameter is the file the application will write the results to.