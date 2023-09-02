package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;

import java.util.*;

/**
 * Represents a unique value constraint on a subset of cells in a grid puzzle.
 * 
 * <p>
 * This class extends from the abstract {@link Constraint} class, and takes advantage 
 * of a {@link PossibleValuesManager} to control possible cell values ensuring uniqueness.
 * </p>
 *
 * <p>
 * A unique value constraint needs to maintain the property that all cells in the 
 * provided grid subset have unique values.
 * </p>
 * 
 * <p>
 * It overrides the {@code propagateCell} method inherited from Constraint. This method 
 * updates possible cell values based on a target cell's old and new value, contributing 
 * to uphold the unique value constraint.
 * </p>
 * 
 * <p>
 * The {@code isRuleBroken} method is also overridden, providing functionality to check 
 * if the unique value constraint is violated for the grid subset, through considering 
 * the current values of the cells in the subset.
 * </p>
 * 
 * <p>
 * Further, it provides a {@code solveCell} method. This method puts forth a process 
 * to solve a cell within the grid subset by finding a unique value that can be assigned to it.
 * </p>
 */
public class UniqueValueConstraint extends Constraint {

    public UniqueValueConstraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        super(gridSubset, pvm);
    }

    /**
     * Updates possible cell values based on a target cell's old and new value.
     * 
     * @param targetCell the cell to propagate
     * @param oldValue   the old value of the cell
     */
    @Override
    public void propagateCell(Cell targetCell, Integer oldValue) {
        // Check if the target cell is part of the grid subset
        if (!gridSubset.contains(targetCell)) {
            return;
        }
        Integer newValue = targetCell.getValue();

        // Check if all old/new values in the grid subset are different from the given
        // old/new value
        boolean allOldValuesDifferent = areAllValuesDifferent(targetCell, oldValue);
        boolean allNewValuesDifferent = areAllValuesDifferent(targetCell, newValue);

        // Update the last opinion and value count based on these checks
        updateLOptionAndVCount(targetCell, oldValue, newValue, allOldValuesDifferent,
                allNewValuesDifferent);
    }

    /**
     * Attempts to solve a cell in the grid subset by finding a unique value that
     * can be assigned to it.
     * 
     * @return true if a cell was successfully solved, false otherwise
     */
    @Override
    public boolean solveCell() {
        List<Integer> possibleCellValues = new ArrayList<>(lastOpinions.values().iterator().next().keySet());
        int countEmptyCells = 0;
        Map<Integer, Cell> cellsWithUniqueValues = new HashMap<>();
        Map<Cell, Set<Integer>> cellValidValues = new HashMap<>();

        for (Cell cell : gridSubset) {
            if (cell.getValue() == null) {
                cellValidValues.put(cell, pValuesManager.getValidValues(cell));
            }
        }

        countEmptyCells = findUniqueValCell(cellsWithUniqueValues, countEmptyCells, cellValidValues);

        if (possibleCellValues.size() > countEmptyCells) {
            return false;
        }

        return setUniqueValCell(cellsWithUniqueValues);
    }

    /**
     * Checks if the unique value constraint is broken for the grid subset.
     * The constraint is broken if there are two cells in the grid subset with the
     * same non-null value.
     * 
     * @return true if the unique value constraint is broken, false otherwise
     */
    @Override
    public boolean isRuleBroken() {
        // Set to store the values of the cells we've seen
        Set<Integer> seenValues = new HashSet<>();

        // Iterate over each cell in the grid subset
        for (Cell cell : this.gridSubset) {
            Integer value = cell.getValue();

            // If the cell's value is not null and we've seen it before, the rule is broken
            if (value != null && seenValues.contains(value)) {
                return true;
            }

            // Add the cell's value to the set of seen values
            seenValues.add(value);
        }

        // If we've gone through all the cells and haven't found any duplicates, the
        // rule is not broken
        return false;
    }

    /**
     * Checks if all values in a subset of the grid are different after a target
     * cell is assigned a specific value.
     *
     * @param gridSubset The subset of the grid to check.
     * @param targetCell The cell in the grid that is assigned a new value.
     * @param value      The value assigned to the target cell.
     * @return true if all values in the grid subset are different after the target
     *         cell is assigned the value, false otherwise.
     */
    private boolean areAllValuesDifferent(Cell targetCell, Integer value) {
        for (Cell cell : gridSubset) {
            if (cell != targetCell && Objects.equals(cell.getValue(), value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates last opinions and value count for cells in grid subset.
     *
     * If old value is unique and exists:
     * - Sets last opinion of this value to false
     * - Increments value count
     *
     * @param targetCell            Cell with changed value.
     * @param oldValue              Previous value of target cell.
     * @param newValue              New value of target cell.
     * @param allOldValuesDifferent Flag for unique old values.
     * @param allNewValuesDifferent Flag for unique new values.
     */
    private void updateLOptionAndVCount(Cell targetCell, Integer oldValue,
            Integer newValue, boolean allOldValuesDifferent, boolean allNewValuesDifferent) {
        for (Cell cell : gridSubset) {
            if (cell != targetCell) {
                // If the old value is not null and all old values are different, update the
                // last opinion and increment the value count
                if (oldValue != null && allOldValuesDifferent) {
                    this.lastOpinions.get(cell).put(oldValue, false);
                    pValuesManager.incrementValueCount(cell, oldValue);
                }
                // If the new value is not null and all new values are different, update the
                // last opinion and decrement the value count
                if (newValue != null && allNewValuesDifferent) {
                    this.lastOpinions.get(cell).put(newValue, true);
                    pValuesManager.decrementValueCount(cell, newValue);
                }
            }
        }
    }

    /**
     * Updates the map of cells with unique values.
     *
     * @param pVals      List of possible cell values.
     * @param uniqueVals Map of cells with unique values.
     * @param cell       Current cell in the grid subset.
     */
    private void updateUniqueVals(Set<Integer> validValues, Map<Integer, Cell> uniqueVals, Cell cell) {
        for (Integer val : validValues) {
            // Update uniqueVals map
            if (uniqueVals.containsKey(val)) {
                uniqueVals.put(val, null);
            } else {
                uniqueVals.put(val, cell);
            }
        }
    }

    /**
     * Finds cells in the grid subset that can allow a unique value.
     *
     * @param pVals          List of possible cell values.
     * @param uniqueVals     Map of cells with unique values.
     * @param emptyCellCount Count of empty cells.
     * @return Updated count of empty cells.
     */
    private int findUniqueValCell(Map<Integer, Cell> uniqueVals, int emptyCellCount, Map<Cell, Set<Integer>> cellValidValues) {
        for (Cell cell : gridSubset) {
            if (cell.getValue() != null) {
                continue;
            }
            emptyCellCount++;
            Set<Integer> validValues = cellValidValues.get(cell);
            updateUniqueVals(validValues, uniqueVals, cell);
        }
        return emptyCellCount;
    }

    /**
     * Attempts to set unique values for each cell in the provided map.
     *
     * Iterates over each entry in the map. For each entry:
     * - The value is set for the cell
     * - If the value is successfully set, the method returns true
     *
     * @param cellsWithUniqueValues A map of cells to the unique values they should
     *                              be set to
     * @return true if a cell value was successfully set, false otherwise
     */
    private boolean setUniqueValCell(Map<Integer, Cell> cellsWithUniqueValues) {
        for (Map.Entry<Integer, Cell> entry : cellsWithUniqueValues.entrySet()) {
            Cell targetCell = entry.getValue();
            Integer targetValue = entry.getKey();
            if (targetCell != null && targetCell.setValue(targetValue)) {
                return true; // The cell was successfully solved
            }
        }
        // If no cell could be solved, return false
        return false;
    }

}