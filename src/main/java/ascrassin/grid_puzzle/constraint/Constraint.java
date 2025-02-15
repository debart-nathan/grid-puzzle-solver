package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a rule applied to a subset of cells in a grid puzzle.
 * 
 * <p>
 * This abstract class is part of a solver by rule propagation where the allowed
 * and
 * forbidden values are managed by interfacing the {@link PossibleValuesManager}
 * of the Grid.
 * 
 * <p>
 * It provides functionality to verify if the rule was broken.
 */
public abstract class Constraint {

    /**
     * The PossibleValuesManager for this Constraint.
     * It manages the allowed and forbidden values for the cells in the grid.
     */
    protected PossibleValuesManager pvm;

    /**
     * The subset of cells in the grid that this Constraint applies to.
     */
    protected List<Cell> gridSubset;

    /**
     * The last opinion that the Constraint made on the cell without taking into
     * account the possibleValueManager (true for forbidden)
     */
    protected Map<Cell, Map<Integer, Boolean>> lastOpinions;

    /**
     * Creates a Constraint object for a subset of cells in the puzzle.
     * Initializes the constraint count for each cell in the grid subset.
     * Calls resetPropagation() for propagating the constraint.
     * 
     * @param gridSubset The subset of cells that this Constraint is applied to.
     * @param pvm        The PossibleValuesManager that manages the allowed and
     *                   forbidden values for the cells.
     */
    protected Constraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        this.pvm = pvm; // initialize possibleValuesManager with pvm
        this.gridSubset=gridSubset;
        this.lastOpinions = new LinkedHashMap<>();
        for (Cell cell : gridSubset) {
            pvm.linkConstraint(cell); // increment constraint count for each cell
            cell.addLinkedConstraint(this);
        }
    }

    /**
     * Cleans up the Constraint. Removes it from cells, clears variables, remove
     * it's effect on possible value ...
     * 
     * should be called each time you don't need the constraint instance anymore
     */
    public void cleanupConstraint() {
        // Loop over each cell in the Constraint's gridSubset
        for (Cell cell : gridSubset) {
            // Call PossibleValuesManager's decrementConstraintCount() for the cell
            pvm.unlinkConstraint(cell);

            // Decrement value counts for the cell
            cleanPossibleValueValueCount(cell);
        }

        // Clear the gridSubset and lastOpinions after the cleanup is done
        gridSubset.clear();
        lastOpinions.clear();

        // If possibleValuesManager is not used after calling cleanupConstraint, set it
        // to null
        pvm = null;
    }

    /**
     * Propagates the Constraint to the cells in the subset because a cell was
     * modified.
     * This method is abstract and must be implemented by subclasses.
     */
    public abstract void propagateCell(Cell cell, Integer oldValue);

    /**
     * An abstract method that checks if the rule of the Constraint is broken.
     * This method needs to be implemented by the classes that extend the Constraint
     * class.
     * 
     * @return true if the rule is broken, false otherwise.
     */
    public abstract boolean isRuleBroken();

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
    public abstract Map.Entry<Cell, Integer> getSolvableCell();

    /**
     * Sets a new PossibleValuesManager for this Constraint.
     * Decrement the constraint count and cleans the possible value count for each
     * cell if a previous PossibleValuesManager was set.
     * Increment the constraint count and updates the value count for each cell if
     * new PossibleValuesManager is provided.
     * 
     * @param newManager The new PossibleValuesManager to set.
     */
    public void setValuesManager(PossibleValuesManager pvm) {
        // Iterate over each cell in the grid subset
        for (Cell cell : gridSubset) {
            // If a PossibleValuesManager was previously set
            if (this.pvm != null) {
                // Decrement the constraint count for the cell
                this.pvm.unlinkConstraint(cell);
                // Clean the possible value count for the cell
                cleanPossibleValueValueCount(cell);
            }
            // If a new PossibleValuesManager is provided
            if (pvm != null) {
                // Increment the constraint count for the cell
                pvm.linkConstraint(cell);
                // Iterate over each entry in the last opinions of the cell
                for (Map.Entry<Integer, Boolean> entry : lastOpinions.get(cell).entrySet()) {
                    // If the last opinion was true
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        // Increment the value count for the cell
                        pvm.allowCellValue(cell, entry.getKey());
                    }
                }
            }
        }
        // Set the new PossibleValuesManager
        this.pvm = pvm;
    }

    /**
     * Resets the effect of the Constraint and reloads it from scratch.
     * Cleans up the possible value count for each cell if it has previous opinion.
     * If not, it initializes the last opinions map for the cell.
     * Calls the propagateCell method for each cell in the grid subset.
     */
    public void resetProp() {

        for (Cell cell : gridSubset) {
            if (lastOpinions.containsKey(cell)) {
                cleanPossibleValueValueCount(cell);
            } else {
                Map<Integer, Boolean> cellOpinions = new HashMap<>();
                for (Integer value : cell.getPossibleValues()) {
                    cellOpinions.put(value, false); // defaultOpinion is false for each possible value
                }
                lastOpinions.put(cell, cellOpinions);
            }
        }
        for (Cell cell : gridSubset) {
            propagateCell(cell, null);
        }

    }

    /**
     * Updates the last opinions for a given cell based on the new opinions.
     * If the old opinion for a value is null or different from the new opinion,
     * it updates the PossibleValuesManager and sets the new opinion as the last
     * opinion for the value.
     *
     * @param cell               The cell for which the last opinions are to be
     *                           updated.
     * @param newOpinionsForCell A map containing the new opinions for the cell.
     */
    protected void updateLastOpinion(Cell cell, Map<Integer, Boolean> newOpinionsForCell) {

        Map<Integer, Boolean> lastOpinionsForCell = this.lastOpinions.get(cell);
        for (Map.Entry<Integer, Boolean> entry : newOpinionsForCell.entrySet()) {
            Integer value = entry.getKey();
            Boolean newOpinion = entry.getValue();
            Boolean oldOpinion = lastOpinionsForCell.get(value);
            if (oldOpinion == null || !oldOpinion.equals(newOpinion)) {
                updatePossibleValuesManager(cell, value, newOpinion);
                lastOpinionsForCell.put(value, newOpinion);
            }
        }
    }

    /**
     * Updates the PossibleValuesManager based on the current state of the cells.
     */
    protected void updatePossibleValuesManager(Cell cell, Integer value, Boolean newOpinion) {
        if (Boolean.TRUE.equals(newOpinion)) {
            pvm.forbidCellValue(cell, value);
        } else {
            pvm.allowCellValue(cell, value);
        }
    }

    /**
     * Decrements the value count for each value with a positive opinion for the
     * given cell.
     * Called when the PossibleValuesManager is unlinked, to preserve the integrity
     * of its valueCount.
     * 
     * @param cell The cell for which the value count needs to be decremented.
     */
    protected void cleanPossibleValueValueCount(Cell cell) {
        Map<Integer, Boolean> cellOpinions = lastOpinions.get(cell);

        // Loop over each entry (value and opinion) in the cell's opinion map
        for (Map.Entry<Integer, Boolean> opinionEntry : cellOpinions.entrySet()) {
            // Extract the value and the opinion
            Integer value = opinionEntry.getKey();
            boolean opinion = opinionEntry.getValue();

            // Call PossibleValuesManager's decrementValueCount() for each value with a
            // positive opinion
            if (opinion) {
                    pvm.forbidCellValue(cell, value);
                    // Update the opinion in the new map
                    updatedOpinions.put(value, false);
            }

        }
    }

}