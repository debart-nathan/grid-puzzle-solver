package ascrassin.grid_puzzle.solver;

import ascrassin.grid_puzzle.constraint.IConstraint;
import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.kernel.Grid;
import ascrassin.grid_puzzle.value_manager.IValueManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * HumanSolver class is responsible for solving the Sudoku puzzle using human-like strategies.
 * It tries to solve the puzzle by applying constraints and value managers.
 * 
 */
public class HumanSolver implements ISolver {

    private Grid grid;
    private List<IConstraint> constraints;
    private IValueManager valueManager;
    private boolean solved;

    public HumanSolver(Grid grid, List<IConstraint> constraints, IValueManager valueManager) {
        this.grid = grid;
        this.constraints = constraints;
        this.valueManager = valueManager;
        this.solved = false;
    }

    public boolean solve() {
        while (!solved) {
            if (grid.isSolved()) {
                System.out.println("Grid solved successfully");
                return true;
            }

            boolean progress = false;

            // Try Situation 1 (Unique Value Constraint)
            System.out.println("Try Constraints");
            if (solveConstraints()) {
                progress = true;
            }

            // If no progress from Situation 1, try Situation 2 (Single Value)
            if (!progress && !grid.isSolved()) {
                System.out.println("Try single valid value");
                if (solveVM()) {
                    progress = true;
                }
            }
            if (!progress && !grid.isSolved()) {
                return false;
            }
        }
        return true;
    }

    private boolean solveConstraints() {
        boolean progress = false;
        for (IConstraint constraint : constraints) {
            Map.Entry<Cell, Integer> solvableCell = constraint.getSolvableCell();
            if (solvableCell != null) {
                Cell cell = solvableCell.getKey();
                Integer newValue = solvableCell.getValue();
                Integer[] coord = grid.findCellCoordinates(cell);
                grid.setCellValue(coord[0], coord[1], newValue);
                System.out.printf("Updated cell %s: %d%n", Arrays.toString(coord), newValue);
                progress = true;
                break;
            }
        }
        return progress;
    }

    private boolean solveVM() {
        boolean progress = false;

        Map.Entry<Cell, Integer> solvableCell = valueManager.getSolvableCell();
        if (solvableCell != null) {
            Cell cell = solvableCell.getKey();
            Integer newValue = solvableCell.getValue();
            Integer[] coord = grid.findCellCoordinates(cell);
            grid.setCellValue(coord[0], coord[1], newValue);
            System.out.printf("Updated cell %s: %d%n", Arrays.toString(coord), newValue);
            progress = true;
        }
        return progress;
    }

    public void resetSolver(Grid newGrid, List<IConstraint> newConstraints, IValueManager newValueManager) {
        this.grid = newGrid;
        this.constraints = newConstraints;
        this.valueManager = newValueManager;
        this.solved = false;
    }
}