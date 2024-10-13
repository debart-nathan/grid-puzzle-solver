package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the possible values for each cell in a grid puzzle,
 * considering the constraints applied to the puzzle.
 */
public class PossibleValuesManager implements IValueManager {

    /**
     * Stores the count of constraints linked to each cell.
     */
    private final Map<Cell, Integer> constraintCounts;

    /**
     * Stores the count of constraints allowing each value for each cell.
     */
    private final Map<Cell, Map<Integer, Integer>> valueCounts;

    /**
     * Pre-computed set of allowed values for each cell, optimized for fast lookup.
     */
    private final Map<Cell, Set<Integer>> allowedValues;

    public PossibleValuesManager() {
        this.constraintCounts = new ConcurrentHashMap<>();
        this.valueCounts = new ConcurrentHashMap<>();
        this.allowedValues = new ConcurrentHashMap<>();
    }

    @Override
    public void linkConstraint(Cell cell) {
        initializeCell(cell);
        constraintCounts.put(cell, constraintCounts.get(cell) + 1);
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
        if (value != null && !cell.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Cannot allow value " + value + " for cell " + cell + ", it's not in the possible values");
        }

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) + 1);

        // Only update allowed values if the count becomes greater than or equal to the constraint count
        // and the value was not already in the allowed set
        if (cellValueCounts.get(value) >= getConstraintCount(cell) &&
            !allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.add(value);
            allowedValues.put(cell, newAllowedValues);
        }
    }

    @Override
    public void forbidCellValue(Cell cell, Integer value) {
        if (value != null && !cell.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Cannot forbid value " + value + " for cell " + cell + ", it's not in the possible values");
        }

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) - 1);

        // Only update allowed values if the count becomes less than the constraint count
        // and the value was in the allowed set
        if (cellValueCounts.get(value) < getConstraintCount(cell) &&
            allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.remove(value);
            allowedValues.put(cell, newAllowedValues);
        }
    }

    private int getConstraintCount(Cell cell) {
        return constraintCounts.getOrDefault(cell, 0);
    }

    private Map<Integer, Integer> getValueCounts(Cell cell) {
        return valueCounts.computeIfAbsent(cell, k -> new HashMap<>());
    }

    private void updateAllowedValues(Cell cell) {
        Set<Integer> newAllowedValues = new HashSet<>();
        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        int constraintCount = getConstraintCount(cell);
    
        for (Map.Entry<Integer, Integer> entry : cellValueCounts.entrySet()) {
            if (entry.getValue() >= constraintCount && cell.getPossibleValues().contains(entry.getKey())) {
                newAllowedValues.add(entry.getKey());
            }
        }
    
        allowedValues.put(cell, newAllowedValues);
    }

    @Override
    public Set<Integer> getValidValues(Cell cell) {
        Set<Integer> validValues = new HashSet<>(cell.getPossibleValues());
        validValues.retainAll(Collections.unmodifiableSet(allowedValues.getOrDefault(cell, Collections.emptySet())));
        return Collections.unmodifiableSet(validValues);
    }

    @Override
    public boolean canSetValue(Cell cell, Integer value) {
        return allowedValues.getOrDefault(cell, Collections.emptySet()).contains(value);
    }

    /**
     * Helper method for testing: Returns the constraint counts map.
     *
     * @return Map of constraint counts for cells.
     */
    public Optional<Map<Cell, Integer>> getConstraintCounts() {
        return Optional.ofNullable(constraintCounts);
    }

    /**
     * Helper method for testing: Returns the value counts map.
     *
     * @return Map of value counts for cells.
     */
    public Optional<Map<Cell, Map<Integer, Integer>>> getValueCounts() {
        return Optional.ofNullable(valueCounts);
    }

    /**
     * Helper method for testing: Returns the allowed values map.
     *
     * @return Map of allowed values for cells.
     */
    public Optional<Map<Cell, Set<Integer>>> getAllowedValues() {
        return Optional.ofNullable(allowedValues);
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
        initializeCell(cell);
        
    }

    /**
     * Initializes a cell in the manager.
     *
     * @param cell The cell to initialize.
     */
    public void initializeCell(Cell cell) {
        constraintCounts.putIfAbsent(cell, 0);
        valueCounts.putIfAbsent(cell, new HashMap<>());
        updateAllowedValues(cell);
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        for (Cell cell : constraintCounts.keySet()) {
            Set<Integer> validValues = getValidValues(cell);
            if (validValues.size() == 1) {
                return new AbstractMap.SimpleEntry<>(cell, validValues.iterator().next());
            }
        }
        return null;
    }
}