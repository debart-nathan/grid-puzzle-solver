package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages the possible values for each cell in a grid puzzle.
 * 
 * <p>
 * This class provides functionality to manage and track the possible values for
 * each cell.
 * It maintains a map to store the constraint count for each cell and a map to
 * store the number
 * of constraints that allow each value for each cell.
 */
public class PossibleValuesManager implements IValueManager {

    // A map to store the constraint count for each cell
    private Map<Cell, Integer> constraintCounts;

    // A map to store the number of constraints that allow each value for each cell
    private Map<Cell, Map<Integer, Integer>> valueCounts;

    /**
     * Constructor for the PossibleValuesManager class.
     * It initializes the constraintCounts and valueCounts maps.
     */
    public PossibleValuesManager() {
        this.constraintCounts = new HashMap<>();
        this.valueCounts = new HashMap<>();
    }

    /**
     * Method to increment the constraint count for a cell.
     * 
     * @param cell The cell for which the constraint count is to be incremented.
     */
    public void linkConstraint(Cell cell) {
        // Increment the constraint count for the given cell
        this.constraintCounts.put(cell, this.constraintCounts.getOrDefault(cell, 0) + 1);
    }

    /**
     * Method to decrement the constraint count for a cell.
     * 
     * @param cell The cell for which the constraint count is to be decremented.
     */
    public void unlinkConstraint(Cell cell) {
        // Decrement the constraint count for the given cell
        this.constraintCounts.put(cell, this.constraintCounts.getOrDefault(cell, 0) - 1);
    }

    /**
     * Method to increment the count for a value for a cell.
     * 
     * @param cell  The cell for which the value count is to be incremented.
     * @param value The value for which the count is to be incremented.
     */
    public void allowCellValue(Cell cell, int value) {
        // Get the value counts for the given cell
        Map<Integer, Integer> cellValueCounts;
        if (this.valueCounts.containsKey(cell)) {
            cellValueCounts = this.valueCounts.get(cell);
        } else {
            cellValueCounts = new HashMap<>();
            this.valueCounts.put(cell, cellValueCounts);
        }
        // Increment the count for the given value
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) + 1);
    }

    /**
     * Method to decrement the count for a value for a cell.
     * 
     * @param cell  The cell for which the value count is to be decremented.
     * @param value The value for which the count is to be decremented.
     */
    public void forbidCellValue(Cell cell, int value) {
        // Get the value counts for the given cell
        Map<Integer, Integer> cellValueCounts = this.valueCounts.get(cell);
        if (cellValueCounts != null) {
            // Decrement the count for the given value
            cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) - 1);
        }
    }

    /**
     * Returns a set of valid values for a given cell.
     * A value is considered valid if it can be set for the cell based on the
     * constraint counts and value counts.
     *
     * @param cell The cell for which to get the valid values.
     * @return A HashSet containing all valid values for the cell.
     */
    public Set<Integer> getValidValues(Cell cell) {
        // Create a HashSet to store the valid values. We use a HashSet because it
        // allows constant time complexity for the contains operation.
        HashSet<Integer> validValues = new HashSet<>();

        // Get the value counts for the given cell. If the cell is not in the
        // valueCounts map, we use an empty HashMap.
        Map<Integer, Integer> cellValueCounts = this.valueCounts.getOrDefault(cell, new HashMap<Integer, Integer>());

        // Iterate over all values in the cellValueCounts map.
        for (Integer value : cellValueCounts.keySet()) {
            // Check if the value can be set for the cell by calling the canSetValue method.
            // If it can be set, add it to the validValues HashSet.
            if (canSetValue(cell, value)) {
                validValues.add(value);
            }
        }

        // Return the validValues HashSet. This contains all values that can be set for
        // the cell.
        return validValues;
    }

    /**
     * Method to check if a value can be set for a cell, based on the constraint
     * count and the count for that value for the cell.
     * 
     * @param cell  The cell for which the check is to be performed.
     * @param value The value to be checked.
     * @return true if the value can be set for the cell, false otherwise.
     */
    public boolean canSetValue(Cell cell, int value) {
        // Get the value counts for the given cell
        Map<Integer, Integer> cellValueCounts;
        if (this.valueCounts.containsKey(cell)) {
            cellValueCounts = this.valueCounts.get(cell);
        } else {
            cellValueCounts = new HashMap<>();
        }
        // Check if the value can be set for the cell
        return Objects.equals(this.constraintCounts.getOrDefault(cell, 0),
                cellValueCounts.getOrDefault(value, 0));
    }
}