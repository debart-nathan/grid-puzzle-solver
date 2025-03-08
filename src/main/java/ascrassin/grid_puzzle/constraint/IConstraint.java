package ascrassin.grid_puzzle.constraint;

import java.util.Map;

import ascrassin.grid_puzzle.kernel.Cell;

public interface IConstraint {

    /**
     * Cleans up the Constraint. Removes it from cells, clears variables, remove
     * it's effect on possible value ...
     * 
     * should be called each time you don't need the constraint instance anymore
     */
    void cleanupConstraint();

    /**
     * Propagates the Constraint to the cells in the subset because a cell was
     * modified.
     * This method is abstract and must be implemented by subclasses.
     * 
     * @param cell     The cell whose value has changed.
     * @param oldValue The previous value of the cell.
     * @return true if new Opinion was created and propagated
     */
    boolean innerRulesPropagateCell(Cell cell, Integer oldValue);

/**
 * Generates updated opinions for a cell based on its new value, considering previously calculated opinions,
 * without setting or storing these opinions. Instead, it returns potential future states.
 * @param targetCell The cell whose opinions need to be updated.
 * @param changedCell  The cell with the changed value
 * @param oldValue The previous value of the cell.
 * @param newValue The new Value of the cell
 *
 * @return A map of cell values to boolean opinions representing potential future states.
 */
    public Map<Integer, Boolean> generateUpdatedOpinions(Cell targetCell, Cell changedCell, Integer oldValue, Integer newValue);

/**
 * Generates fresh opinions for a cell from scratch based on the current state of the puzzle,
 * without setting or storing these opinions.
 *
 * @param cell The cell whose opinions need to be recalculated.
 * @return A map of cell values to boolean opinions representing potential future states.
 */
    public Map<Integer, Boolean> generateOpinions(Cell cell);

    /**
     * Checks if the rule of the Constraint is broken.
     * 
     * @return true if the rule is broken, false otherwise.
     */
    boolean isRuleBroken();

    /**
     * Tries to find a cell and the value it should have within the grid subset,
     * ensuring the constraint is respected. If no cell can be found, it returns
     * null.
     * Implementation details are left for the concrete classes implementing this
     * interface.
     *
     * @return Map.Entry<Cell, Integer> representing the cell to be updated and the
     *         integer
     *         value to assign, or null if no suitable cell could be found.
     */
    Map.Entry<Cell, Integer> getSolvableCell();

    Map<Integer, Boolean> getLastOpinions(Cell cell);

    /**
     * Resets the effect of the Constraint and reloads it from scratch.
     * Cleans up the possible value count for each cell if it has previous opinion.
     * If not, it initializes the last opinions map for the cell.
     * Calls the propagateCell method for each cell in the grid subset.
     */
    void resetProp();

}