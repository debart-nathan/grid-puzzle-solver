package ascrassin.grid_puzzle.solver;

import ascrassin.grid_puzzle.kernel.Grid;

import java.util.List;

import ascrassin.grid_puzzle.constraint.IConstraint;
import ascrassin.grid_puzzle.value_manager.IValueManager;

public interface ISolver {
    
    /**
     * Attempts to solve the Sudoku puzzle.
     *
     * @return true if the puzzle was solved, false otherwise
     */
    boolean solve();

    /**
     * Resets the solver with a new grid, constraints, and value manager.
     *
     * @param newGrid the new grid to solve
     * @param newConstraints the new set of constraints
     * @param newValueManager the new value manager
     */
    void resetSolver(Grid newGrid, List<IConstraint> newConstraints, IValueManager newValueManager);

}