package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;
import java.util.*;

/**
 * Represents a rule applied to a subset of cells in a grid puzzle.
 * This abstract class is part of a solver by rule propagation where the allowed
 * and forbidden values are managed by interfacing the
 * {@link PossibleValuesManager}
 * of the Grid.
 * It provides functionality to verify if the rule was broken.
 *
 * <p>
 * The Constraint class is responsible for:
 * </p>
 * <ul>
 * <li>Managing the allowed and forbidden values for a subset of cells</li>
 * <li>Tracking the opinions (allowed or forbidden status) of each cell
 * value</li>
 * <li>Updating the PossibleValuesManager based on changes in cell values</li>
 * <li>Cleaning up resources when no longer needed</li>
 * </ul>
 *
 * <p>
 * This class is designed to be subclassed by specific constraint
 * implementations,
 * such as ComparisonConstraint and UniqueValueConstraint.
 * </p>
 */
public abstract class Constraint implements IConstraint {
    protected PossibleValuesManager pvm;

    /**
     * The last opinion that the Constraint made on the cell without taking into
     * account the possibleValueManager (true for forbidden)
     */
    protected Map<Cell, Map<Integer, Boolean>> lastOpinions;

    /**
     * Constructs a new Constraint instance for the given subset of cells and
     * PossibleValuesManager.
     * 
     * @param gridSubset The list of cells this constraint applies to
     * @param pvm        The PossibleValuesManager to interface with
     */
    Constraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        this.lastOpinions = new HashMap<>();

        this.pvm = pvm;
        for (Cell cell : gridSubset) {
            Map<Integer, Boolean> lastOpinionsForCell = new HashMap<>();
            this.lastOpinions.put(cell, lastOpinionsForCell);
            pvm.linkConstraint(cell, this, lastOpinionsForCell);

            cell.addLinkedValueManager(pvm);

        }

        resetProp();
    }

    /**
     * Cleans up resources associated with this constraint.
     * Unlinks the constraint from all cells and clears internal data structures.
     */
    @Override
    public void cleanupConstraint() {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        for (Cell cell : cells) {
            pvm.unlinkConstraint(cell, this, lastOpinions.get(cell));
        }
        lastOpinions.clear();
    }

    @Override
    public Map<Integer, Boolean> getLastOpinions(Cell cell) {
        return lastOpinions.get(cell);
    }

    /**
     * Resets the propperties state for all cells in this constraint.
     * This is called when switching between different PossibleValuesManagers.
     */
    @Override
    public void resetProp() {
        if (this.pvm == null) {
            System.out.println("Warning: PossibleValuesManager is null");
            return;
        }

        List<Cell> cells = this.pvm.getCellsForConstraint(this);
        for (Cell cell : cells) {

            updateLastOpinion(cell, generateInnerOpinions(cell), false);
        }

    }

    /**
     * Updates the last known opinion for a cell regarding a particular value.
     * 
     * @param cell               The cell whose opinion is being updated
     * @param newOpinionsForCell The new opinions for the cell
     * @return The updated map of opinions for the cell
     */
    protected Map<Integer, Boolean> updateLastOpinion(Cell cell, Map<Integer, Boolean> newOpinionsForCell,
            Boolean wideReach) {
        Map<Integer, Boolean> lastOpinionsForCell = this.lastOpinions.computeIfAbsent(cell, k -> new HashMap<>());
        for (Map.Entry<Integer, Boolean> entry : newOpinionsForCell.entrySet()) {
            Integer value = entry.getKey();
            if (!cell.getPossibleValues().contains(value)) {
                continue;
            }
            Boolean newOpinion = Boolean.TRUE.equals(entry.getValue());
            Boolean oldOpinion = lastOpinionsForCell.computeIfAbsent(value, k -> false);
            if (!oldOpinion.equals(newOpinion)) {
                updatePossibleValuesManager(cell, value, newOpinion, wideReach);
                lastOpinionsForCell.put(value, newOpinion);

            }

        }
        this.lastOpinions.put(cell, lastOpinionsForCell);

        return this.lastOpinions.get(cell);
    }

    @Override
    public boolean innerRulesPropagateCell(Cell cell, Integer oldValue) {
        if (!pvm.getCellsForConstraint(this).contains(cell)) {
            return false;
        }

        if ((oldValue == null && cell.getValue() != null) ||
                (oldValue != null && !oldValue.equals(cell.getValue()))) {
            // Generate updated opinions for the cell
            for (Cell c : pvm.getCellsForConstraint(this)) {
                Map<Integer, Boolean> newOpinions = generateUpdatedInnerOpinions(c, cell, oldValue, cell.getValue());
                updateLastOpinion(c, newOpinions, false);
            }
            return true;
        }
        return false;
    }

    @Override
    public Map<Integer, Boolean> generateUpdatedInnerOpinions(Cell targetCell, Cell changedCell, Integer oldValue,
            Integer newValue) {
        if (targetCell == null || changedCell == null || this.lastOpinions == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        return generateInnerOpinions(targetCell);
    }

    @Override
    public boolean wideReachRulesPossibleValues(Cell cell, Integer affectedValue) {
        return false;
    }

    /**
     * Updates the PossibleValuesManager based on a change in a cell's opinion.
     * 
     * @param cell            The cell whose opinion changed
     * @param value           The value whose opinion changed
     * @param newValueOpinion The delta of the new opinion for the value
     * @return 1 if forbid 0 if allow -1 if no pvm
     */
    protected Integer updatePossibleValuesManager(Cell cell, Integer value, Boolean newValueOpinion,
            Boolean wideReach) {
        if (pvm == null) {
            return -1;
        }
        if (Boolean.TRUE.equals(newValueOpinion)) {
            pvm.forbidCellValue(cell, value, wideReach);
            return 1;
        } else {
            pvm.allowCellValue(cell, value, wideReach);
            return 0;
        }
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        for (Cell cell : pvm.getCellsForConstraint(this)) {

            Map<Integer, Boolean> cellOpinions = lastOpinions.get(cell);
            if (cellOpinions == null) {
                continue;
            }
            Integer uniqueFalseValue = null;

            for (Map.Entry<Integer, Boolean> valueOpinion : cellOpinions.entrySet()) {
                if (Boolean.FALSE.equals(valueOpinion.getValue())) {
                    if (uniqueFalseValue == null) {
                        uniqueFalseValue = valueOpinion.getKey();
                    } else {
                        uniqueFalseValue = null;
                        break;
                    }
                }

            }

            if (uniqueFalseValue != null) {
                return new AbstractMap.SimpleEntry<>(cell, uniqueFalseValue);
            }

        }

        return null;
    }

}