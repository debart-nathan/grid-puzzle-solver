package ascrassin.grid_puzzle.puzzle_factory;

import java.util.ArrayList;
import java.util.List;
import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;
import ascrassin.grid_puzzle.constraint.*;

public class SudokuFactory {

    public static SudokuInfo createSudoku(PossibleValuesManager pvm) {
        Integer[][] puzzle = new Integer[9][9];
        System.out.println("puzzle:");
        return initializeGridAndConstraints(puzzle, pvm);
    }

    private static SudokuInfo initializeGridAndConstraints(Integer[][] puzzle, PossibleValuesManager pvm) {
        Grid grid = new Grid(puzzle, 1, 9);

        List<Constraint> allConstraints = new ArrayList<>();

        // Row constraints
        for (Integer i = 0; i < 9; i++) {
            List<Cell> rowCells = getRowCells(grid, i);
            Constraint rowConstraint = ConstraintFactory.createInstance(UniqueValueConstraint.class, rowCells, pvm);
            for (Cell cell : rowCells) {
                cell.addLinkedConstraint(rowConstraint);
                pvm.linkConstraint(cell);
            }
            allConstraints.add(rowConstraint);
        }

        // Column constraints
        for (Integer j = 0; j < 9; j++) {
            List<Cell> colCells = getColumnCells(grid, j);
            Constraint colConstraint = ConstraintFactory.createInstance(UniqueValueConstraint.class, colCells, pvm);
            for (Cell cell : colCells) {
                cell.addLinkedConstraint(colConstraint);
                pvm.linkConstraint(cell);
            }
            allConstraints.add(colConstraint);
        }

        // Box constraints
        for (Integer k = 0; k < 3; k++) {
            for (Integer l = 0; l < 3; l++) {
                List<Cell> boxCells = getBoxCells(grid, k, l);
                Constraint boxConstraint = ConstraintFactory.createInstance(UniqueValueConstraint.class, boxCells, pvm);
                for (Cell cell : boxCells) {
                    cell.addLinkedConstraint(boxConstraint);
                    pvm.linkConstraint(cell);
                }
                allConstraints.add(boxConstraint);
            }
        }

        return new SudokuInfo(grid, allConstraints);
    }

    private static List<Cell> getRowCells(Grid grid, Integer row) {
        List<Cell> cells = new ArrayList<>();
        for (Integer i = 0; i < 9; i++) {
            cells.add(grid.getCellAt(row, i));
        }
        return cells;
    }

    private static List<Cell> getColumnCells(Grid grid, Integer col) {
        List<Cell> cells = new ArrayList<>();
        for (Integer i = 0; i < 9; i++) {
            cells.add(grid.getCellAt(i, col));
        }
        return cells;
    }

    private static List<Cell> getBoxCells(Grid grid, Integer boxRow, Integer boxCol) {
        List<Cell> cells = new ArrayList<>();
        
        // Ensure boxRow and boxCol are within valid range
        boxRow = Math.max(0, Math.min(2, boxRow));
        boxCol = Math.max(0, Math.min(2, boxCol));

        for (Integer i = boxRow * 3; i < (boxRow + 1) * 3; i++) {
            for (Integer j = boxCol * 3; j < (boxCol + 1) * 3; j++) {
                cells.add(grid.getCellAt(i, j));
            }
        }
        return cells;
    }

    public static class SudokuInfo {
        private final Grid grid;
        private final List<Constraint> constraints;

        public SudokuInfo(Grid grid, List<Constraint> constraints) {
            this.grid = grid;
            this.constraints = constraints;
        }

        public Grid getGrid() {
            return grid;
        }

        public List<Constraint> getConstraints() {
            return constraints;
        }
    }
}