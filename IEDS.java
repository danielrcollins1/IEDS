import java.io.*;

/******************************************************************************
*  Iterated Elimination of Dominated Strategies calculator.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-02-04
******************************************************************************/

public class IEDS {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Strictness level. */
	Strictness strictness;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	/** Payoff matrix for player 1. */
	int[][] matrix1;

	/** Payoff matrix for player 2. */
	int[][] matrix2;

	/** Number of rows in each matrix. */
	int numRows;

	/** Number of columns in each matrix. */
	int numCols;

	/** Placeholder for a blank entry. */
	public final static int BLANK = Integer.MIN_VALUE;

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	enum Strictness {STRICT, WEAK, VERYWEAK};

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	IEDS () {
		strictness = Strictness.STRICT;
		exitAfterArgs = false;
		matrix1 = null;
		matrix2 = null;
		numRows = 0;
		numCols = 0;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print usage.
	*/
	void printUsage () {
		System.out.println("Usage: IEDS matrix1 [matrix2] [options]");
		System.out.println("  Performs IEDS on two-player game in normal form.");
		System.out.println("  matrix1/2 is CSV file with payoff matrix for player1/2.");
		System.out.println("  If only matrix1 provided assumes game is symmetric");
		System.out.println("  (matrix2 will be set to the transpose of matrix1).");
		System.out.println("  By default only eliminates strictly dominated strategies.");
		System.out.println();
		System.out.println("Options include:");
		System.out.println("  -w weak dominance eliminated");
		System.out.println("  -v very weak dominance eliminated");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	void parseArgs (String[] args) {
		for (String s: args) {
			if (s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'v': strictness = Strictness.VERYWEAK; break;
					case 'w': strictness = Strictness.WEAK; break;
					default: exitAfterArgs = true; break;
				}
			}
			else {

				// Load the matrix
				int[][] loadMatrix = null;
				try {
					loadMatrix = loadMatrixFromFile(s);
				}
				catch (IOException e) {
					System.err.println("Could not load file: " + s);
					exitAfterArgs = true;
				}
				
				// Assign to next game matrix
				if (matrix1 == null) {
					matrix1 = loadMatrix;
				}
				else if (matrix2 == null) {
					matrix2 = loadMatrix;
				}
				else {
					exitAfterArgs = true;
				}
			}
		}
		processMatrices();
	}

	/**
	*  Load a matrix from a CSV file.
	*/
	int[][] loadMatrixFromFile (String filename) throws IOException {
		String[][] table = CSVReader.readFile(filename);
		int numRows = table.length;
		int numCols = table[0].length;
		int[][] matrix = new int[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				matrix[i][j] = Integer.parseInt(table[i][j]);			
			}
		}
		return matrix;
	}

	/**
	*  Process matrices after reading arguments. 
	*/
	void processMatrices () {

		// Check on matrix existence
		if (matrix1 == null) {
			System.err.println("No game matrix found.");
			exitAfterArgs = true;
			return;
		}

		// Create transpose if needed
		if (matrix2 == null) {
			matrix2 = transposeMatrix(matrix1);	
		}
		
		// Check compatible sizes
		if (matrix1.length != matrix2.length
				|| matrix1[0].length != matrix2[0].length) {
			System.err.println("Incompatible matrix sizes.");
			exitAfterArgs = true;
			return;
		}
		
		// Set joint rows and columns
		numRows = matrix1.length;
		numCols = matrix1[0].length;
	}

	/**
	*  Transpose a matrix (for symmetric game).
	*/
	int[][] transposeMatrix (int[][] matrix) {
		int newCols = matrix.length;
		int newRows = matrix[0].length;
		int[][] transpose = new int[newRows][newCols];
		for (int i = 0; i < newRows; i++) {
			for (int j = 0; j < newCols; j++) {
				transpose[i][j] = matrix[j][i];
			}
		}
		return transpose;
	}

	/**
	*  For player1, is row A dominated by B?
	*/
	boolean isRowDominated (int rowA, int rowB) {
		if (isRowBlank(rowA) || isRowBlank(rowB) || rowA == rowB)
			return false;
		boolean foundLesser = false, foundEqual = false, foundGreater = false;
		for (int j = 0; j < numCols; j++) {
			int diff = matrix1[rowB][j] - matrix1[rowA][j];
			if (diff < 0)
				foundLesser = true;
			else if (diff > 0)
				foundGreater = true;
			else
				foundEqual = true;
		}	
		switch (strictness) {
			case STRICT: return !foundEqual && !foundLesser;
			case WEAK: return foundGreater && !foundLesser;
			case VERYWEAK: return !foundLesser;
			default: return false;
		}
	}

	/**
	*  For player2, is col A dominated by B?
	*/
	boolean isColDominated (int colA, int colB) {
		if (isColBlank(colA) || isColBlank(colB) || colA == colB)
			return false;
		boolean foundLesser = false, foundEqual = false, foundGreater = false;
		for (int i = 0; i < numRows; i++) {
			int diff = matrix2[i][colB] - matrix2[i][colA];
			if (diff < 0)
				foundLesser = true;
			else if (diff > 0)
				foundGreater = true;
			else
				foundEqual = true;
		}	
		switch (strictness) {
			case STRICT: return !foundEqual && !foundLesser;
			case WEAK: return foundGreater && !foundLesser;
			case VERYWEAK: return !foundLesser;
			default: return false;
		}
	}

	/**
	*  Eliminate a row in both game matrices.
	*/
	void eliminateRow (int row) {
		for (int j = 0; j < numCols; j++) {
			matrix1[row][j] = BLANK;
			matrix2[row][j] = BLANK;
		}	
	}

	/**
	*  Eliminate a column in both game matrices.
	*/
	void eliminateCol (int col) {
		for (int i = 0; i < numRows; i++) {
			matrix1[i][col] = BLANK;
			matrix2[i][col] = BLANK;
		}	
	}

	/**
	*  Is this row eliminated?
	*/
	boolean isRowBlank (int row) {
		for (int j = 0; j < numCols; j++) {
			if (matrix1[row][j] != BLANK)
				return false;		
		}
		return true;
	}

	/**
	*  Is this column eliminated?
	*/
	boolean isColBlank (int col) {
		for (int i = 0; i < numRows; i++) {
			if (matrix1[i][col] != BLANK)
				return false;		
		}
		return true;
	}

	/**
	*  Eliminate everything we can.
	*/
	void eliminateAll () {
		boolean anyCut = false;
		int iterationCount = 0;
		do {
			// Start iteration
			anyCut = false;		
			iterationCount++;
			System.out.println("Iteration #" + iterationCount + ":");
		
			// Check rows
			for (int row = 0; row < numRows; row++) {
				for (int opp = 0; opp < numRows; opp++) {
					if (isRowDominated(row, opp)) {
						System.out.println("Row " + (row+1) + " dominated by row " + (opp+1));
						eliminateRow(row);
						anyCut = true;
					}
				}
			}

			// Check columns
			for (int col = 0; col < numCols; col++) {
				for (int opp = 0; opp < numCols; opp++) {
					if (isColDominated(col, opp)) {
						System.out.println("Col " + (col+1) + " dominated by col " + (opp+1));
						eliminateCol(col);
						anyCut = true;
					}
				}
			}
			
			// Check any cut this iteration
			if (!anyCut) {
				System.out.println("No eliminations.");
			}
			System.out.println();
		} while (anyCut);
	}

	/**
	*  Print all of a matrix.
	*/
	void printMatrix (int[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	*  Print matrix with crossouts where eliminated.
	*/
	void printMatrixCrossouts (int[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.out.print(matrix[i][j] == BLANK ?
					"-\t" : matrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	*  Print matrix with eliminations removed.
	*/
	void printMatrixReduced (int[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		for (int i = 0; i < rows; i++) {
			if (isRowBlank(i)) continue;
			for (int j = 0; j < cols; j++) {
				if (matrix[i][j] != BLANK)
					System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	*  Report post-elimination matrix size.
	*/
	void reportPostEliminationSize () {

		// Count remaining rows
		int newRowSize = 0;
		for (int row = 0; row < numRows; row++) {
			if (!isRowBlank(row)) newRowSize++;
		}
		
		// Count remaining columns
		int newColSize = 0;
		for (int col = 0; col < numCols; col++) {
			if (!isColBlank(col)) newColSize++;
		}
		
		// Report new size
		System.out.println("Post-elimination matrix size: " 
			+ newRowSize + "x" + newColSize + "\n");
	}

	/**
	*  Do eliminations and report results.
	*/
	void eliminateAndReport () {
		eliminateAll();
		reportPostEliminationSize();
		
		// Results with crossouts
		System.out.println("Player 1 Matrix With Crossouts:");
		printMatrixCrossouts(matrix1);
		System.out.println("Player 2 Matrix With Crossouts:");
		printMatrixCrossouts(matrix2);
		
		// Results with removals
		System.out.println("Player 1 Matrix After Removals:");
		printMatrixReduced(matrix1);
		System.out.println("Player 2 Matrix After Removals:");
		printMatrixReduced(matrix2);
	}

	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		IEDS eliminator = new IEDS();
		eliminator.parseArgs(args);
		if (eliminator.exitAfterArgs) {
			eliminator.printUsage();
		}
		else {
			eliminator.eliminateAndReport();
		}
	}
}
