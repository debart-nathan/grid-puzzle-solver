package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.constraint.IConstraint;
import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the possible values for each cell in a grid puzzle,
 * considering the constraints applied to the puzzle.
 *
 * This class is responsible for tracking and updating the possible
 * values for each cell based on the constraints imposed by the puzzle.
 * It maintains two main data structures:
 * <ul>
 * <li>{@link Map} of constraint counts for each cell</li>
 * <li>{@link Map} of value counts for each cell</li>
 * </ul>
 *
 * The class provides methods to link/unlink constraints, allow/disallow
 * cell values, and retrieve valid values for cells. It also offers helper
 * methods for testing and debugging purposes.
 *
 * @author Your Name
 * @version 1.1
 */
public class PossibleValuesManager implements IValueManager {

    /**
     * Stores the count of constraints allowing each value for each cell.
     */
    protected final Map<Cell, Map<Integer, Integer>> valueCounts;

    /**
     * Pre-computed set of allowed values for each cell, optimized for fast lookup.
     */
    protected final Map<Cell, Set<Integer>> allowedValues;

    private final Map<Cell, List<IConstraint>> cellToConstraints;
    private final Map<IConstraint, List<Cell>> constraintToCells;

    /**
     * Constructs a new PossibleValuesManager instance.
     */
    public PossibleValuesManager() {
        super();
        this.valueCounts = new ConcurrentHashMap<>();
        this.allowedValues = new ConcurrentHashMap<>();
        this.cellToConstraints = new ConcurrentHashMap<>();
        this.constraintToCells = new ConcurrentHashMap<>();
    }

    @Override
    public void linkConstraint(Cell cell, IConstraint constraint, Map<Integer, Boolean> lastOpinion) {
        if (cellToConstraints.containsKey(cell)) {
            cellToConstraints.get(cell).add(constraint);
        } else {
            List<IConstraint> constraints = new ArrayList<>();
            constraints.add(constraint);
            cellToConstraints.put(cell, constraints);
        }

        if (constraintToCells.containsKey(constraint)) {
            constraintToCells.get(constraint).add(cell);
        } else {
            List<Cell> cells = new ArrayList<>();
            cells.add(cell);
            constraintToCells.put(constraint, cells);
        }

        // increment values counts for value allowed by constraint (lastOpinion false or
        // absent)
        for (Integer value : cell.getPossibleValues()) {
            if (lastOpinion == null || !lastOpinion.getOrDefault(value, false)) {
                Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
                int count = cellValueCounts.getOrDefault(value, 0);
                cellValueCounts.put(value, count + 1);

            }
        }
        updateAllowedValues(cell);
    }

    @Override
    public void unlinkConstraint(Cell cell, IConstraint constraint, Map<Integer, Boolean> lastOpinion) {
        if (constraint == null) {
            return;
        }

        List<IConstraint> constraints = cellToConstraints.getOrDefault(cell, Collections.emptyList());
        int index = constraints.indexOf(constraint);
        if (index != -1) {
            constraints.remove(index);

            List<Cell> cells = constraintToCells.getOrDefault(constraint, Collections.emptyList());
            cells.remove(cells.indexOf(cell));

            // decrement values counts for value allowed by constraint (lastOpinion false or
            // absent)
            for (Integer value : cell.getPossibleValues()) {
                if (lastOpinion == null || !lastOpinion.getOrDefault(value, false)) {
                    Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
                    int count = cellValueCounts.getOrDefault(value, 0);
                    if (count > 0) {
                        cellValueCounts.put(value, count - 1);
                    }
                }
            }

            updateAllowedValues(cell);
        }
    }

    /**
     * Gets the subset of constraints for a given cell.
     *
     * @param cell The cell to get constraints for.
     * @return A set of constraints linked to the cell.
     */
    public List<IConstraint> getConstraintsForCell(Cell cell) {
        return cellToConstraints.getOrDefault(cell, Collections.emptyList());
    }

    /**
     * Gets the cells associated with a specific constraint.
     *
     * @param constraint The constraint to get cells for.
     * @return A set of cells linked to the constraint.
     */
    public List<Cell> getCellsForConstraint(IConstraint constraint) {
        return constraintToCells.getOrDefault(constraint, Collections.emptyList());
    }

    @Override
    public void propagateCellValueChange(Cell cell, Integer oldValue, Integer newValue) {
        List<IConstraint> constraints = this.getConstraintsForCell(cell);
        constraints.forEach(constraint -> constraint.innerRulesPropagateCell(cell, oldValue));

        constraints.forEach(constraint -> constraint.wideReachRulesPossibleValues(cell, newValue));
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
    public void allowCellValue(Cell cell, Integer value, Boolean wideReach) {
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
        cellValueCounts.put(value, cellValueCounts.getOrDefault(value, 0) + 1);

        // Only update allowed values if the count becomes greater than or equal to the
        // constraint count and the value was not already in the allowed set
        if (cellValueCounts.get(value) >= getConstraintCount(cell) &&
                !allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.add(value);
            allowedValues.put(cell, newAllowedValues);
            List<IConstraint> constraints = getConstraintsForCell(cell);
            for (IConstraint constraint : constraints) {
                constraint.wideReachRulesPossibleValues(cell, value);
            }
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
    @Override
    public void forbidCellValue(Cell cell, Integer value, Boolean wideReach) {
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
        cellValueCounts.put(value, currentValueCount - 1);

        // Only update allowed values if the count becomes less than the constraint
        // count and the value was in the allowed set
        if (cellValueCounts.get(value) < getConstraintCount(cell) &&
                allowedValues.computeIfAbsent(cell, k -> new HashSet<>()).contains(value)) {
            Set<Integer> newAllowedValues = new HashSet<>(allowedValues.get(cell));
            newAllowedValues.remove(value);
            allowedValues.put(cell, newAllowedValues);
            List<IConstraint> constraints = getConstraintsForCell(cell);
            for (IConstraint constraint : constraints) {
                constraint.wideReachRulesPossibleValues(cell, value);
            }
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
        return cellToConstraints.getOrDefault(cell, Collections.emptyList()).size();
    }

    protected Map<Integer, Integer> getValueCounts(Cell cell) {
        return valueCounts.computeIfAbsent(cell, k -> new HashMap<>());
    }

    protected void updateAllowedValues(Cell cell) {
        Set<Integer> newAllowedValues = new HashSet<>();

        Map<Integer, Integer> cellValueCounts = getValueCounts(cell);
        int constraintCount = getConstraintCount(cell);

        for (Integer value : cell.getPossibleValues()) {
            int count = cellValueCounts.getOrDefault(value, 0);
            if (count >= constraintCount) {
                newAllowedValues.add(value);
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
        // Remove the cell from the constraint maps
        cellToConstraints.remove(cell);
        constraintToCells.values().forEach(constraints -> constraints.remove(cell));

        // Reset cell data
        valueCounts.remove(cell);
        allowedValues.remove(cell);

        // Re-initialize the cell
        initializeCell(cell);
    }

    /**
     * Initializes a cell in the manager.
     *
     * @param cell The cell to initialize.
     */
    public void initializeCell(Cell cell) {
        if (!valueCounts.containsKey(cell)) {
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cell.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            valueCounts.put(cell, initialCounts);
        }

        allowedValues.computeIfAbsent(cell, k -> new HashSet<>());

        // Update allowed values based on possible values
        updateAllowedValues(cell);
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        for (Cell cell : cellToConstraints.keySet()) {
            if (cell.isSolved()) {
                continue;
            }
            Set<Integer> validValues = getValidValues(cell);
            if (validValues.size() == 1) {
                return new AbstractMap.SimpleEntry<>(cell, validValues.iterator().next());
            }
        }
        return null;
    }
}