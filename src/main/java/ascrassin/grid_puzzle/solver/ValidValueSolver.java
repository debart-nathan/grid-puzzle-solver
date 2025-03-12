package ascrassin.grid_puzzle.solver;

import ascrassin.grid_puzzle.constraint.IConstraint;
import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.kernel.Grid;
import ascrassin.grid_puzzle.value_manager.IValueManager;
import java.util.List;
import java.util.Set;

/**
 * RuleSolver class is responsible for solving the Sudoku puzzle using
 * backtracking algorithm.
 * it use constraint.isRuleBroken() to check if the rule is broken to sop the
 * progress early.
 * 
 */
public class ValidValueSolver implements ISolver {

    private Grid grid;
    private List<IConstraint> constraints;
    private boolean solved;
    private IValueManager valueManager;

    public ValidValueSolver(Grid grid, List<IConstraint> constraints, IValueManager valueManager) {
        this.grid = grid;
        this.constraints = constraints;
        this.valueManager = valueManager;
        this.solved = false;
    }

    public boolean solve() {

        if (grid.isSolved()) {
            System.out.println("Grid solved successfully");
            return true;
        }
        Cell cell = grid.getCellAt(0, 0);
        return rSolve(cell);

    }

    private boolean rSolve(Cell cell) {
        if (cell == null) {
            return true;
        }
        if (cell.isSolved()) {
            return tryNextCell(cell);

        }
        Set<Integer> vValues = valueManager.getValidValues(cell);
        if (vValues.isEmpty()) {
            return false;
        }
        for (Integer i : vValues) {

            cell.setValue(i);
            if (tryNextCell(cell)) {
                return true;
            }

        }
        cell.setValue(null);
        return false;
    }

    private boolean tryNextCell(Cell cell) {
        Integer[] coord = grid.findCellCoordinates(cell);
        Integer row_max = grid.getRowsCount();
        Integer col_max = grid.getColsCount();
        if (coord[0] == row_max - 1 && coord[1] == col_max - 1) {
            return true;
        }
        if (coord[1] == col_max - 1) {
            return rSolve(grid.getCellAt(coord[0] + 1, 0));
        } else {
            return rSolve(grid.getCellAt(coord[0], coord[1] + 1));
        }
    }

    public void resetSolver(Grid newGrid, List<IConstraint> newConstraints, IValueManager newValueManager) {
        this.grid = newGrid;
        this.constraints = newConstraints;
        this.valueManager = newValueManager;
        this.solved = false;
    }

}