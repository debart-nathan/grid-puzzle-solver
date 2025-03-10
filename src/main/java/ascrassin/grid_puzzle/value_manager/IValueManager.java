package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.constraint.IConstraint;
import ascrassin.grid_puzzle.kernel.Cell;

import java.util.Map;
import java.util.Set;

public interface IValueManager {
    void linkConstraint(Cell cell, IConstraint constraint, Map<Integer,Boolean> lastOpinion);
    void unlinkConstraint(Cell cell, IConstraint constraint,Map<Integer,Boolean> lastOpinion);
    void allowCellValue(Cell cell, Integer value, Boolean wideReach);
    void forbidCellValue(Cell cell, Integer value, Boolean wideReach);
    Set<Integer> getValidValues(Cell cell);
    boolean canSetValue(Cell cell, Integer value);
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
    Map.Entry<Cell, Integer> getSolvableCell();

    /**
     * 
     * @param changedCell
     * @param oldValue
     * @param newValue
     */
    void propagateCellValueChange(Cell changedCell, Integer oldValue, Integer newValue);
}
