package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;

/**
 * Manages the possible values for each cell in a grid puzzle,
 * considering the constraints applied to the puzzle.
 */
public class PossibleValuesManager implements IValueManager {
    
    /**
     * Stores the count of constraints linked to each cell.
     */
    private Map<Cell, Integer> constraintCounts;
    
    /**
     * Stores the count of constraints allowing each value for each cell.
     */
    private Map<Cell, Map<Integer, Integer>> valueCounts;
    
    /**
     * Pre-computed set of allowed values for each cell, optimized for fast lookup.
     */
    private Map<Cell, Set<Integer>> allowedValues;

    public PossibleValuesManager() {
        this.constraintCounts = new HashMap<>();
        this.valueCounts = new HashMap<>();
        this.allowedValues = new HashMap<>();
    }

    @Override
    public void linkConstraint(Cell cell) {
        constraintCounts.put(cell, constraintCounts.getOrDefault(cell, 0) + 1);
        updateAllowedValues(cell);
    }

    @Override
    public void unlinkConstraint(Cell cell) {
        int count = constraintCounts.getOrDefault(cell, 0) - 1;
        if (count < 0) {
            throw new IllegalStateException("Constraint count cannot be negative");
        }
        constraintCounts.put(cell, count);
        updateAllowedValues(cell);
    }

    @Override
    public void allowCellValue(Cell cell, Integer value) {
        if (!cell.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Cannot allow value " + value + " for cell " + cell + ", it's not in the possible values");
        }
        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) + 1);
        updateAllowedValues(cell);
    }

    @Override
    public void forbidCellValue(Cell cell, Integer value) {
        if (!cell.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Cannot forbid value " + value + " for cell " + cell + ", it's not in the possible values");
        }
        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) - 1);
        updateAllowedValues(cell);
    }

    /**
     * Helper method to get the value counts for a cell.
     * Creates a new map if none exists for the cell.
     *
     * @param cell The cell for which to get value counts.
     * @return Map of value counts for the cell.
     */
    private Map<Integer, Integer> getValueCounts(Cell cell) {
        return valueCounts.computeIfAbsent(cell, c -> new HashMap<>());
    }

    /**
     * Updates the set of allowed values for a cell based on the current constraint counts and value counts.
     * A value is considered allowed if it's explicitly allowed or if there are no constraints.
     *
     * @param cell The cell for which to update allowed values.
     */
    private void updateAllowedValues(Cell cell) {
        Set<Integer> newAllowedValues = new HashSet<>( Collections.emptySet());
        int totalConstraints = constraintCounts.getOrDefault(cell, 0);
    
        if (totalConstraints == 0) {
            // If no constraints, all values are allowed
            newAllowedValues.addAll(new HashSet<>(allowedValues.getOrDefault(cell, Collections.emptySet())));
        } else {
            for (Integer value : getValueCounts(cell).keySet()) {
                if (getValueCounts(cell).get(value) >= totalConstraints) {
                    newAllowedValues.add(value);
                }
            }
        }
    
        allowedValues.put(cell, newAllowedValues);
    }

    @Override
    public Set<Integer> getValidValues(Cell cell) {
        // Get the intersection of possible values and allowed values
        Set<Integer> validValues = new HashSet<>(cell.getPossibleValues());
        validValues.retainAll(allowedValues.getOrDefault(cell, Collections.emptySet()));
        return validValues;
    }

    @Override
    public boolean canSetValue(Cell cell, Integer value) {
        // Check if the value is in the pre-computed set of allowed values
        return allowedValues.getOrDefault(cell, Collections.emptySet()).contains(value);
    }

    /**
     * Helper method for testing: Returns the constraint counts map.
     *
     * @return Map of constraint counts for cells.
     */
    public Map<Cell, Integer> getConstraintCounts() {
        return constraintCounts;
    }

    /**
     * Helper method for testing: Returns the value counts map.
     *
     * @return Map of value counts for cells.
     */
    public Map<Cell, Map<Integer, Integer>> getValueCounts() {
        return valueCounts;
    }

    /**
     * Helper method for testing: Returns the allowed values map.
     *
     * @return Map of allowed values for cells.
     */
    public Map<Cell, Set<Integer>> getAllowedValues() {
        return allowedValues;
    }

    /**
     * Resets all data for a specific cell.
     *
     * @param cell The cell to reset.
     */
    public void resetCell(Cell cell) {
        constraintCounts.remove(cell);
        valueCounts.remove(cell);
        allowedValues.remove(cell);
        updateAllowedValues(cell); // Ensure we initialize with empty sets
    }
}