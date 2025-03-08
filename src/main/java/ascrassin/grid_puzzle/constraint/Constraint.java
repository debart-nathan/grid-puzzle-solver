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
            pvm.linkConstraint(cell, this);
            this.lastOpinions.putIfAbsent(cell, new HashMap<>());
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
            pvm.unlinkConstraint(cell, this);
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

            updateLastOpinion(cell, generateOpinions(cell));
        }

    }

    /**
     * Updates the last known opinion for a cell regarding a particular value.
     * 
     * @param cell               The cell whose opinion is being updated
     * @param newOpinionsForCell The new opinions for the cell
     * @return The updated map of opinions for the cell
     */
    protected Map<Integer, Boolean> updateLastOpinion(Cell cell, Map<Integer, Boolean> newOpinionsForCell) {
        Map<Integer, Boolean> lastOpinionsForCell = this.lastOpinions.computeIfAbsent(cell, k -> new HashMap<>());
        for (Map.Entry<Integer, Boolean> entry : newOpinionsForCell.entrySet()) {
            Integer value = entry.getKey();
            Boolean newOpinion = entry.getValue();
            Boolean oldOpinion = lastOpinionsForCell.get(value);
            if ((oldOpinion == null && newOpinion != null)
                    || (oldOpinion != null && !oldOpinion.equals(newOpinion))) {
                updatePossibleValuesManager(cell, value, newOpinion);
                lastOpinionsForCell.replace(value, newOpinion);

            }

        }
        this.lastOpinions.replace(cell, lastOpinionsForCell);

        return this.lastOpinions.get(cell);
    }

    /**
     * Updates the PossibleValuesManager based on a change in a cell's opinion.
     * 
     * @param cell            The cell whose opinion changed
     * @param value           The value whose opinion changed
     * @param newValueOpinion The delta of the new opinion for the value
     * @return 1 if forbid 0 if allow -1 if no pvm
     */
    protected Integer updatePossibleValuesManager(Cell cell, Integer value, Boolean newValueOpinion) {
        if (pvm == null) {
            return -1;
        }
        if (Boolean.TRUE.equals(newValueOpinion)) {
            pvm.forbidCellValue(cell, value);
            return 1;
        } else {
            pvm.allowCellValue(cell, value);
            return 0;
        }
    }

}