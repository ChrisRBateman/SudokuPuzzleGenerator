import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SudokuGenerator generates multiple puzzle-solution pairs for a standard Sudoku game.
 * Based on the code from https://github.com/rharriso/sudoku-gen-cpp.
 */
public class SudokuGenerator {
	private static final int SIZE = 9;
	private static final int THIRD = SIZE / 3;
	
	private static final Set<Integer> VALID_VALUES = 
			new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
	
	private boolean allNeighbors = false;
	
	/**
	 * Coord defines the horizontal and vertical position of a SudokuCell.
	 */
	private class Coord implements Comparable<Coord> {
		int i = 0;
		int j = 0;
		
		Coord(int i, int j) {
			this.i = i;
			this.j = j;
		}
		
		@Override
		public int compareTo(Coord coord) {
			if (this.i == coord.i && this.j == coord.j) {
				return 0;
			}
			if (this.i < coord.i || (this.i == coord.i && this.j < coord.j)) {
				return -1;
			}
			return 1;
		}
	}
	
	class CoordComp implements Comparator<Coord>{
	    @Override
	    public int compare(Coord e1, Coord e2) {
	    		return e1.compareTo(e2);
	    }
	}
	
	/**
	 * SudokuCell stores information (position, neighbors and value) about each cell of board.
	 */
	private class SudokuCell {
		Coord pos = null;
		Set<Coord> neighbors = new TreeSet<>(new CoordComp()); 
		int value = 0;
		
		/**
		 * Set the position of cell.
		 * @param pos a Coord object 
		 */
		public void setPosition(Coord pos) {
			this.pos = pos;
			
			if (allNeighbors) {
				generateAllNeighbors();
			} else {
				generateOptimalNeighbors();
			}
		}
		
		/**
		 * Generate coordinates of all neighboring cells.
		 */
		private void generateAllNeighbors() {
			for (int n = 0; n < SIZE; ++n) {
				if (n != pos.i) {
					neighbors.add(new Coord(n, pos.j));
				}
				if (n!= pos.j) {
					neighbors.add(new Coord(pos.i, n));
				}
	        }
			
			int iFloor = (pos.i / THIRD) * THIRD;
	        int jFloor = (pos.j / THIRD) * THIRD;
	        
	        for (int n = iFloor; n < iFloor + THIRD ; ++n) {
	            for (int m = jFloor; m < jFloor + THIRD; ++m) {
	            		if (n != pos.i && m != pos.j) {
	            			neighbors.add(new Coord(n, m));
	            		}
	            }
	        }	
		}
		
		/**
		 * Generate optimal coordinates of neighboring cells.
		 */
		private void generateOptimalNeighbors() {
			for (int i = 0; i < pos.i; ++i) {
	            neighbors.add(new Coord(i, pos.j));
	        }
			
			for (int j = 0; j < pos.j; ++j) {
	            neighbors.add(new Coord(pos.i, j));
	        }
			
			int iFloor = (pos.i / THIRD) * THIRD;
	        int jFloor = (pos.j / THIRD) * THIRD;
	        
	        for (int i = iFloor; i <= pos.i ; ++i) {
	            for (int j = jFloor; (i < pos.i && j < jFloor + THIRD) || j < pos.j ; ++j) {
	                neighbors.add(new Coord(i, j));
	            }
	        }
		}
	}
	
	/**
	 * SudokuBoard creates the puzzle and solution values for Sudoku games.
	 */
	private class SudokuBoard {
		// Stores all modifications to the board cells.
		LinkedList<SudokuCell> allCells = new LinkedList<>();
		LinkedList<SudokuCell> cellsToFill = new LinkedList<>();
		
		String puzzleString = "";
		String solutionString = "";
		
		/**
		 * SudokuBoard constructor.
		 */
		SudokuBoard() {
			for(int i = 0; i < SIZE; i++) {
	            for (int j = 0; j < SIZE; j++) {
	                SudokuCell cell = new SudokuCell();
	                cell.setPosition(new Coord(i, j));
	                allCells.addLast(cell);
	                cellsToFill.addLast(cell);
	            }
	        }
		}
		
		/**
		 * Fill cells to create a valid Sudoku board.
		 */
		public void fillCells() {
	        if(!doFillCells(0)) {
	            System.out.println("Unable to fill board");
	        }
	    }
		
		/**
		 * Generate a puzzle with requested number of visible values.
		 * @param hints the number of visible values
		 */
		public void generatePuzzle(int hints) {
			ArrayList<Integer> indices = new ArrayList<>();
			for(int i = 0; i < SIZE; i++) {
	            for (int j = 0; j < SIZE; j++) {
	            		indices.add(i * 9 + j);
	            }
	        }
			Collections.shuffle(indices);
			
			solutionString = cellsToString(allCells);
			
			ArrayList<Integer> removedIndices = new ArrayList<>();
			int count = SIZE * SIZE - hints;
			cellsToFill.clear();
			for (Integer index : indices) {
				cellsToFill.addLast(at(index));
				if (doFillCells(0)) {
					removedIndices.add(index);
				}
				if (removedIndices.size() >= count) {
					break;
				}
			}
			
			for (Integer index : removedIndices) {
				SudokuCell cell = allCells.get(index);
				cell.value = 0;
			}
			puzzleString = cellsToString(allCells);
		}
		
		/**
		 * Return the cells as a string.
		 * @param cells the list of cells
		 * @return cells as a string
		 */
		public String cellsToString(LinkedList<SudokuCell> cells) {
			StringBuilder sb = new StringBuilder();
			if (cells != null) {
				for (SudokuCell cell : cells) {
					if (cell.value == 0) {
						sb.append(".");
					}
					else {
						sb.append(cell.value);
					}
				}
			}
			return sb.toString();
		}
		
		/**
		 * Set up cells with test data.
		 * @param testData string of test data
		 */
		public void setTestData(String testData) {
			if (testData.length() == allCells.size()) {
				int i = 0;
				cellsToFill.clear();
				for (SudokuCell cell : allCells) {
					char c = testData.charAt(i++);
					int k = (c == '.') ? 0 : Character.getNumericValue(c);
					cell.value = k;
					
					if (k == 0) {
						cellsToFill.addLast(cell);
					}
				}
			}
		}
		
		/**
		 * Solve the test data.
		 */
		public void solveTestPuzzle() {
			doFillCells(0);
		}
		
		private boolean doFillCells(int index) {
			SudokuCell cell = cellsToFill.get(index);
			
			Set<Integer> neighborValues = new TreeSet<>();
			
			for(Coord neighbor : cell.neighbors) {
	            int value = at(neighbor).value;
	            neighborValues.add(value);
	        }
			
			ArrayList<Integer> options = new ArrayList<>(VALID_VALUES);
			options.removeAll(neighborValues);
			Collections.shuffle(options);
			
			for(Integer option : options) {
	            cell.value = option;

	            if (index == cellsToFill.size() - 1 || doFillCells(index + 1)) {
	                return true;
	            }
	        }

	        // out of options - backtrack
	        cell.value = 0;
			
			return false;
		}
		
		private int resolvePosition(Coord position) {
	        return position.i * SIZE + position.j;
	    }
		
		private SudokuCell at(int index) {
	        return allCells.get(index);
	    }
		
		private SudokuCell at(Coord position) {
	        int index = resolvePosition(position);
	        return at(index);
	    }
	}

	/**
	 * Application entry point.
	 * @param args array of command line parameters
	 */
	public static void main(String[] args) {
		SudokuGenerator app = new SudokuGenerator();		
		app.run(args);
	}
	
	/**
	 * Print usage information.
	 */
	public void usage() {
		System.out.println("java SudokuGenerator count clues file");
		System.out.println("    where");
		System.out.println("        count - is the number of puzzles in file");
		System.out.println("        clues - is the number of visible values per puzzle");
		System.out.println("        file  - is the name of the output file. This file is");
		System.out.println("                deleted if it already exists.");
	}
	
	/**
	 * Run application.
	 * @param args array of command line parameters
	 */
	public void run(String[] args) {
		if (args.length != 3) {
			usage();
			return;
		}
		
		try {
			int totalPuzzles = Integer.parseInt(args[0]);
			int visibleValues = Integer.parseInt(args[1]);
			
			totalPuzzles = Math.max(1, Math.min(totalPuzzles, 500));
			visibleValues = Math.max(29, Math.min(visibleValues, 50));
			
			Charset utf8 = StandardCharsets.UTF_8;
			Path path = Paths.get(args[2]);
			
			List<String> lines = new ArrayList<>();
			Files.deleteIfExists(path);
			
			allNeighbors = true;
			
			int dotCount = 0;
			
			while (totalPuzzles > 0) {
				SudokuBoard board = new SudokuBoard();
				board.fillCells();
				board.generatePuzzle(visibleValues);
				
				board.setTestData(board.puzzleString);
				board.solveTestPuzzle();
				String solvedString = board.cellsToString(board.allCells);
				
				if (solvedString.equals(board.solutionString)) {
					lines.add(board.puzzleString);
					lines.add(board.solutionString);
					totalPuzzles--;
					
					System.out.print(".");
					dotCount++;
					if (dotCount > 70) {
						dotCount = 0;
						System.out.println();
					}
				}	
			}
			if (lines.size() > 0) {
				Files.write(path, lines, utf8);
			}
			System.out.println();
			System.out.println("Done.");
		}
		catch (Exception e) {
			System.out.println("There's an error : [" + e.getMessage() + "]");
			usage();
			e.printStackTrace();
		}
	}	
}
