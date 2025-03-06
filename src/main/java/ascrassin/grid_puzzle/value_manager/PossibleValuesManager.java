package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the possible values for each cell in a grid puzzle,
 * considering the constraints applied to the puzzle.
 *
 * This class is responsible for tracking and updating the possible
 * values for each cell based on the constraints imposed by the puzzle.
 * It maintains three main data structures:
 * <ul>
 * <li>{@link Map} of constraint counts for each cell</li>
 * <li>{@link Map} of value counts for each cell</li>
 * <li>{@link Map} of allowed values for each cell</li>
 * </ul>
 *
 * The class provides methods to link/unlink constraints, allow/disallow
 * cell values, and retrieve valid values for cells. It also offers helper
 * methods for testing and debugging purposes.
 *
 * @author Your Name
 * @version 1.0
 */
public class PossibleValuesManager implements IValueManager {

    /**
     * Stores the count of constraints linked to each cell.
     */
    protected final Map<Cell, Integer> constraintCounts;

    /**
     * Stores the count of constraints allowing each value for each cell.
     */
    protected final Map<Cell, Map<Integer, Integer>> valueCounts;

    /**
     * Pre-computed set of allowed values for each cell, optimized for fast lookup.
     */
    protected final Map<Cell, Set<Integer>> allowedValues;

    /**
     * Constructs a new PossibleValuesManager instance.
     */
    public PossibleValuesManager() {
        this.constraintCounts = new ConcurrentHashMap<>();
        this.valueCounts = new ConcurrentHashMap<>();
        this.allowedValues = new ConcurrentHashMap<>();
    }

    /**
     * Links a constraint to the specified cell.
     *
     * @param cell The cell to link the constraint to.
     */
    @Override
    public void linkConstraint(Cell cell) {
        initializeCell(cell);
        constraintCounts.replace(cell, constraintCounts.get(cell) + 1);
        updateAllowedValues(cell);
    }

    /**
     * Unlinks a constraint from the specified cell.
     *
     * @param cell The cell to unlink the constraint from.
     */
    @Override
    public void unlinkConstraint(Cell cell) {
        int count = constraintCounts.getOrDefault(cell, 0) - 1;
        if (count < 0) {
            throw new IllegalStateException("Constraint count cannot be negative");
        }
        constraintCounts.replace(cell, count);
        updateAllowedValues(cell);
    }

    /**
     * Allows a specific value for a cell.
     *
     * @param cell  The cell to allow the value for.
     * @param value The value to allow.
     * @throws IllegalArgumentException if the cell is null or not managed by this
     *                                  ValueManager.
     * @throws IllegalArgumentException if the value is not in the cell's possible
     *                                  values.
     */
    @Override
    public void allowCellValue(Cell cell, Integer value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null");
        }

        if (!allowedValues.containsKey(cell)) {
            throw new IllegalArgumentException("Cell is not managed by this ValueManager");
        }

        Set<Integer> cellPossibleValues = cell.getPossibleValues();
        if (value == null) {
            // Return without effect when value is null
            return;
        }

        if (!cellPossibleValues.contains(value)) {
            throw new IllegalArgumentException(
                    "Cannot allow value " + value + " for cell " + cell + ", it's not in the possible values");
        }

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        cellValueCounts.replace(value, cellValueCounts.getOrDefault(value, 0) + 1);

        // Only update allowed values if the count becomes greater than or equal to the
        // constraint count
        // and the value was not already in the allowed set
        if (cellValueCounts.get(value) >= getConstraintCount(cell) &&
                !allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.add(value);
            allowedValues.replace(cell, newAllowedValues);
        }
    }

    /**
     * Disallows a specific value for a cell.
     *
     * @param cell  The cell to disallow the value for.
     * @param value The value to disallow.
     * @throws IllegalArgumentException if the cell is null or not managed by this
     *                                  ValueManager.
     * @throws IllegalArgumentException if the value is not in the cell's possible
     *                                  values.
     */
    /**
     * Disallows a specific value for a cell.
     *
     * @param cell  The cell to disallow the value for.
     * @param value The value to disallow.
     * @throws IllegalArgumentException if the cell is null or not managed by this
     *                                  ValueManager.
     * @throws IllegalArgumentException if the value is not in the cell's possible
     *                                  values.
     */
    @Override
    public void forbidCellValue(Cell cell, Integer value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null");
        }

        if (!allowedValues.containsKey(cell)) {
            throw new IllegalArgumentException("Cell is not managed by this ValueManager");
        }

        if (value == null) {
            // Return without effect when value is null
            return;
        }

        if (!cell.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException(
                    "Cannot forbid value " + value + " for cell " + cell + ", it's not in the possible values");
        }

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        int currentValueCount = cellValueCounts.getOrDefault(value, 0);
        cellValueCounts.replace(value, currentValueCount - 1);

        // Only update allowed values if the count becomes less than the constraint
        // count and the value was in the allowed set
        if (cellValueCounts.get(value) < getConstraintCount(cell) &&
                allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.remove(value);
            allowedValues.replace(cell, newAllowedValues);
        }
    }

    /**
     * Gets the number of constraints linked to a cell.
     *
     * @param cell The cell to check.
     * @return The number of constraints linked to the cell.
     * @throws NullPointerException if the cell is null.
     */
    public int getConstraintCount(Cell cell) {
        return constraintCounts.getOrDefault(cell, 0);
    }

    /**
     * Gets the counts of values for a cell.
     *
     * @param cell The cell to get counts for.
     * @return A {@link Map} containing value counts for the cell.
     */
    protected Map<Integer, Integer> getValueCounts(Cell cell) {
        return valueCounts.computeIfAbsent(cell, k -> new HashMap<>());
    }

    /**
     * Updates the allowed values for a cell based on its constraint count and value
     * counts.
     *
     * @param cell The cell to update allowed values for.
     */
    protected void updateAllowedValues(Cell cell) {
        Set<Integer> newAllowedValues = new HashSet<>();

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        int constraintCount = getConstraintCount(cell);

        for (Integer value : cell.getPossibleValues()) {
            int count = cellValueCounts.getOrDefault(value, 0); // Treat null as 0
            if (count >= constraintCount) {
                newAllowedValues.add(value);
            }
        }


        allowedValues.put(cell, newAllowedValues);
    }

    /**
     * Gets the valid values for a cell, considering both possible values and
     * allowed values.
     *
     * @param cell The cell to get valid values for.
     * @return An unmodifiable {@link Set} of valid values for the cell.
     */
    @Override
    public Set<Integer> getValidValues(Cell cell) {
        Set<Integer> validValues = new HashSet<>(cell.getPossibleValues());
        validValues.retainAll(Collections.unmodifiableSet(allowedValues.getOrDefault(cell, Collections.emptySet())));
        return Collections.unmodifiableSet(validValues);
    }

    /**
     * Checks if a value can be set for a cell.
     *
     * @param cell  The cell to check.
     * @param value The value to check.
     * @return True if the value can be set, false otherwise.
     */
    /**
     * Checks if a value can be set for a cell.
     *
     * @param cell  The cell to check.
     * @param value The value to check.
     * @return True if the value can be set, false otherwise.
     */
    @Override
    public boolean canSetValue(Cell cell, Integer value) {
        return allowedValues.getOrDefault(cell, Collections.emptySet()).contains(value);
    }

    /**
     * Helper method for testing: Returns the constraint counts map.
     *
     * @return Map of constraint counts for cells.
     */
    public Map<Cell, Integer> getConstraintCounts() {
        return this.constraintCounts;
    }

    /**
     * Helper method for testing: Returns the value counts map.
     *
     * @return Map of value counts for cells.
     */
    public Map<Cell, Map<Integer, Integer>> getValueCounts() {
        return this.valueCounts;
    }

    /**
     * Helper method for testing: Returns the allowed values map.
     *
     * @return Map of allowed values for cells.
     */
    public Map<Cell, Set<Integer>> getAllowedValues() {
        return this.allowedValues;
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
        if (!valueCounts.containsKey(cell)) {
            // Initialize valueCounts with all possible values set to 0
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cell.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            valueCounts.put(cell, initialCounts);
        }
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