package ascrassin.grid_puzzle;

import java.util.List;
import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;
import ascrassin.grid_puzzle.puzzle_factory.*;
import ascrassin.grid_puzzle.puzzle_factory.SudokuFactory.SudokuInfo;
import ascrassin.grid_puzzle.constraint.*;


public class App {
    public static void main(String[] args) {
        PossibleValuesManager pvm = new PossibleValuesManager();

        SudokuInfo sudokuInfo = SudokuFactory.createSudoku(pvm);

        Grid sudoku = sudokuInfo.getGrid();
        List<Constraint> constraints = sudokuInfo.getConstraints();

        System.out.println("Sudoku before Init:");
        logSudoku(sudoku);
    
// Easy Sudoku puzzle
//sudoku.setCellValue(row, col, value);
int[][] easySudoku = {
    {5, 0, 0, 0, 0, 0, 0, 0, 0},
    {6, 0, 0, 0, 0, 0, 0, 0, 0},
    {2, 0, 0, 0, 0, 0, 0, 0, 0},
    {8, 0, 0, 0, 0, 0, 0, 0, 0},
    {4, 0, 0, 0, 0, 0, 0, 0, 0},
    {7, 0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 0, 0},
    {3, 0, 0, 0, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 0, 0, 0, 0, 0}
};

for (int i = 0; i < 9; i++) {
    for (int j = 0; j < 9; j++) {
        sudoku.setCellValue(i, j, easySudoku[i][j]);
    }
}


    
        // Log the Sudoku before solving
        System.out.println("Sudoku before solving:");
        logSudoku(sudoku);
    
        Solver solver = new Solver(sudoku, constraints, pvm);
        boolean isSolved = solver.solve();
    
        // Log the Sudoku after solving
        System.out.println("\nSudoku after solving:");
        logSudoku(sudoku);
    
        System.out.println("\nIs solved: " + isSolved);
    }
    
    private static void logSudoku(Grid sudoku) {
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