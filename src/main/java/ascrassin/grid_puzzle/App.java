package ascrassin.grid_puzzle;

import java.util.List;
import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;
import ascrassin.grid_puzzle.puzzle_factory.*;
import ascrassin.grid_puzzle.puzzle_factory.SudokuFactory.SudokuInfo;
import ascrassin.grid_puzzle.solver.HumanSolver;
import ascrassin.grid_puzzle.solver.ISolver;
import ascrassin.grid_puzzle.solver.RuleSolver;
import ascrassin.grid_puzzle.solver.ValidValueSolver;
import ascrassin.grid_puzzle.constraint.*;

public class App {
    public static void main(String[] args) {
        PossibleValuesManager pvm = new PossibleValuesManager();

        SudokuInfo sudokuInfo = SudokuFactory.createSudoku(pvm);

        Grid sudoku = sudokuInfo.getGrid();
        List<IConstraint> constraints = sudokuInfo.getConstraints();

        System.out.println("Sudoku before Init:");
        logSudoku(sudoku);

        // Easy Sudoku puzzle
        int[][] hardSudoku = {
            {4, 0, 0, 3, 7, 2, 1, 9, 6},
            {0, 0, 2, 0, 0, 0, 8, 7, 0},
            {9, 7, 0, 0, 0, 0, 4, 0, 0},
            {5, 0, 3, 0, 0, 1, 7, 6, 0},
            {0, 9, 0, 0, 3, 7, 5, 0, 4},
            {2, 0, 7, 0, 0, 0, 3, 0, 0},
            {6, 0, 0, 0, 0, 3, 9, 0, 7},
            {0, 0, 9, 7, 0, 0, 2, 4, 0},
            {7, 2, 0, 9, 5, 0, 6, 0, 0}
        };
        

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku.setCellValue(i, j, hardSudoku[i][j]);
            }
        }

        // Log the Sudoku before solving
        System.out.println("Sudoku before solving:");
        logSudoku(sudoku);

        ISolver solver = new RuleSolver(sudoku, constraints, pvm);
        boolean isSolved = solver.solve();

        // Log the Sudoku after solving
        System.out.println("\nSudoku after solving:");
        logSudoku(sudoku);

        System.out.println("\nIs solved: " + isSolved);
    }

    protected static void logSudoku(Grid sudoku) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Object value = sudoku.getCellValue(i, j);
                String formattedValue = value instanceof Integer ? value.toString() : "_";
                System.out.printf("%2s ", formattedValue);
                if ((j + 1) % 3 == 0 && j < 8) {
                    System.out.print("| ");
                }
            }
            System.out.println();
            if ((i + 1) % 3 == 0 && i < 8) {
                System.out.println("---------+---------+---------");
            }
        }
    }
}