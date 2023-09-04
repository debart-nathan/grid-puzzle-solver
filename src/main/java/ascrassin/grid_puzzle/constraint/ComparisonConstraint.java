package ascrassin.grid_puzzle.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.*;

/**
 * Represents a comparison constraint on a subset of cells in a grid puzzle.
 * 
 * <p>
 * This class extends the abstract {@link Constraint} class, and leverages a
 * {@link PossibleValuesManager} to administer potential cell values, conforming
 * to a designated comparison operation (such as less than, greater than,
 * equals, etc.).
 * </p>
 *
 * <p>
 * A comparison constraint is purposed to sustain a specific comparison relation
 * between the values of cells that have adjacent indices in the provided grid
 * subset.
 * </p>
 * 
 * <p>
 * Override the {@code propagateCell} method inherited from Constraint. This
 * method
 * updates possible cell values based on a target cell's old and new value,
 * contributing
 * to uphold the comparison constraint.
 * </p>
 * 
 * <p>
 * Override the {@code isRuleBroken} method inherited from Constraint. this
 * method
 * check whether the comparison constraint is being broken for the defined
 * subset of cells.
 * </p>
 * 
 * <p>
 * Override the {@code getSolvableCell} method inherited from Constraint.
 * Always returns null as the ComparisonConstraint does not provide
 * an advanced solving method.
 * </p>
 */
public class ComparisonConstraint extends Constraint {
    private BiPredicate<Integer, Integer> comparator;
    private Map<Cell, Map<Integer, Boolean>> precValueOpinion;
    private Map<Cell, Map<Integer, Boolean>> sucValueOpinion;

    /**
     * Constructs a new ComparisonConstraint with the specified list of cells,
     * PossibleValuesManager, and comparison operation. The comparison operation is
     * represented as a BiPredicate object.
     *
     * @param cells      The list of cells in the grid subset that the constraint
     *                   applies to.
     * @param pvm        The PossibleValuesManager that controls the possible values
     *                   of the cells.
     * @param comparator The comparison operation defined by the constraint.
     */
    public ComparisonConstraint(List<Cell> gridSubset, PossibleValuesManager pvm,
            BiPredicate<Integer, Integer> comparator) {
        super(gridSubset, pvm);
        this.comparator = comparator;
        for (Cell cell : gridSubset) {
            Map<Integer, Boolean> cellOpinions = new HashMap<>();
            for (Integer value : cell.getPossibleValues()) {
                cellOpinions.put(value, false); // defaultOpinion is false for each possible value
            }
            sucValueOpinion.put(cell, cellOpinions);
            precValueOpinion.put(cell, cellOpinions);
        }
    }

    /**
     * Propagates the changes in possible values of a target cell to other cells
     * in the grid subset. The propagation is based on the comparison constraint.
     *
     * @param targetCell The cell in the grid subset whose possible values have
     *                   changed.
     * @param oldValue   The old value of the target cell before the change.
     */
    @Override
    public void propagateCell(Cell targetCell, Integer oldValue) {

        updatePrecAndSucOpinionCell(targetCell);

        for (int iCell = 0; iCell < gridSubset.size(); iCell++) {
            Cell cell = gridSubset.get(iCell);
            for (int iConstraintCell = 0; iConstraintCell < gridSubset.size(); iConstraintCell++) {
                Map<Integer, Boolean> newOpinionsForCell = generateNewOpinions(iCell, iConstraintCell);
                updateLastOpinion(cell, newOpinionsForCell);
            }
        }
    }

    /**
     * Checks if the comparison constraint is violated for the grid subset. It
     * considers the current values of the cells in the subset
     * and the comparison operation defined by the instance of the constraint.
     *
     * @return {@code true} if the comparison constraint is broken, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isRuleBroken() {
        for (int i = 0; i < this.gridSubset.size() - 1; i++) {
            Cell currentCell = this.gridSubset.get(i);
            Cell nextCell = this.gridSubset.get(i + 1);
            if (!comparator.test(currentCell.getValue(), nextCell.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method does not attempt to find a cell within the grid subset.
     * It always returns null as this class does not have a built-in algorithm
     * to find a cell respecting the comparison constraint.
     *
     * @return null always, as no suitable cell can be found by this method.
     */
    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        return null;
    }

    /**
     * Adjusts the potential values of other cells in the subset based on the
     * current cell's possible values.
     * This method considers the cell's position within the constraint and applies
     * the comparison operation accordingly.
     * 
     * @param cell The cell that serves as the reference point for updating the
     *             potential values of other cells in the subset.
     */
    private void updatePrecAndSucOpinionCell(Cell targetCell) {

        // Get the current value of the target cell
        Integer newValue = targetCell.getValue();

        // Maps to store the opinions of predecessor and successor cells for each
        // possible value in regard to the new value
        Map<Integer, Boolean> newPrecValueOpinion = new HashMap<>();
        Map<Integer, Boolean> newSucValueOpinion = new HashMap<>();

        // If the current value of the target cell is null
        if (newValue == null) {
            // In this case, we restrict no value in other cell
            for (Integer value : targetCell.getPossibleValues()) {
                newPrecValueOpinion.put(value, false);
                newSucValueOpinion.put(value, false);
            }
        } else {
            // If the cell's value is not null, we can use the comparator to test each
            // possible value
            // We store the result of each test in the corresponding map
            for (Integer value : targetCell.getPossibleValues()) {
                newPrecValueOpinion.put(value, !comparator.test(value, newValue));
                newSucValueOpinion.put(value, !comparator.test(newValue, value));
            }
        }
    }

    /**
     * Generates new opinions for a given cell based on a constraint cell.
     *
     * @param iCell           The index of the cell for which to generate new
     *                        opinions.
     * @param iConstraintCell The index of the constraint cell.
     * @return A map of new opinions for the cell.
     */
    private Map<Integer, Boolean> generateNewOpinions(int iCell, int iConstraintCell) {
        // Initialize a new map to store the new opinions for the cell
        Map<Integer, Boolean> newOpinionsForCell = new HashMap<>();
        // Get the constraint cell from the grid subset
        Cell constraintCell = gridSubset.get(iConstraintCell);

        // If the cell precedes the constraint cell, generate new opinions for the
        // preceding cell
        if (iCell < iConstraintCell) {
            generateNewOpinionsForPrecedingCell(constraintCell, newOpinionsForCell);
        }
        // If the cell succeeds the constraint cell, generate new opinions for the
        // succeeding cell
        else if (iCell > iConstraintCell) {
            generateNewOpinionsForSucceedingCell(constraintCell, newOpinionsForCell);
        }

        return newOpinionsForCell;
    }

    /**
     * Generates new opinions for a preceding cell based on a constraint cell.
     *
     * @param iCell              The index of the cell for which to generate new
     *                           opinions.
     * @param constraintCell     The constraint cell.
     * @param newOpinionsForCell The current opinions for the cell.
     * @return A map of new opinions for the cell.
     */
    private Map<Integer, Boolean> generateNewOpinionsForPrecedingCell(Cell constraintCell,
            Map<Integer, Boolean> newOpinionsForCell) {
        // Get the opinions on the predecessors of the constraint cell
        Map<Integer, Boolean> precValueOpinionInner = precValueOpinion.get(constraintCell);

        // If there are opinions on the predecessors of the constraint cell
        if (precValueOpinionInner != null) {
            // Iterate over each opinion entry
            for (Map.Entry<Integer, Boolean> entry : precValueOpinionInner.entrySet()) {
                // Merge the opinion with the existing opinions for the cell using logical OR
                mergeOpinions(entry, newOpinionsForCell);
            }
        }

        return newOpinionsForCell;
    }

    /**
     * Generates new opinions for a succeeding cell based on a constraint cell.
     *
     * @param iCell              The index of the cell for which to generate new
     *                           opinions.
     * @param constraintCell     The constraint cell.
     * @param newOpinionsForCell The current opinions for the cell.
     * @return A map of new opinions for the cell.
     */
    private Map<Integer, Boolean> generateNewOpinionsForSucceedingCell(Cell constraintCell,
            Map<Integer, Boolean> newOpinionsForCell) {
        // Get the opinions on the successors of the constraint cell
        Map<Integer, Boolean> sucValueOpinionInner = sucValueOpinion.get(constraintCell);

        // If there are opinions on the successors of the constraint cell
        if (sucValueOpinionInner != null) {
            // Iterate over each opinion entry
            for (Map.Entry<Integer, Boolean> entry : sucValueOpinionInner.entrySet()) {
                // Merge the opinion with the existing opinions for the cell using logical OR
                mergeOpinions(entry, newOpinionsForCell);
            }
        }

        return newOpinionsForCell;
    }

    /**
     * Merges an opinion entry with the existing opinions for the cell using logical
     * OR.
     *
     * @param entry              The opinion entry to merge.
     * @param newOpinionsForCell The current opinions for the cell.
     * @return The updated opinions for the cell.
     */
    private Map<Integer, Boolean> mergeOpinions(Map.Entry<Integer, Boolean> entry,
            Map<Integer, Boolean> newOpinionsForCell) {
        // Merge the opinion with the existing opinions for the cell using logical OR
        newOpinionsForCell.merge(
                entry.getKey(),
                entry.getValue(),
                (v1, v2) -> Boolean.logicalOr(
                        v1 == null ? false : v1,
                        v2 == null ? false : v2));
        return newOpinionsForCell;
    }

}