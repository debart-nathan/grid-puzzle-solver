package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;
import java.util.*;

public abstract class Constraint implements IConstraint {
    protected final PossibleValuesManager pvm;
    protected final Map<Cell, Map<Integer, Boolean>> lastOpinions;

    /**
     * Constructs a new Constraint instance for the given subset of cells and
     * PossibleValuesManager.
     *
     * @param gridSubset The list of cells this constraint applies to.
     * @param pvm        The PossibleValuesManager to interface with.
     * @throws IllegalArgumentException if gridSubset or pvm is null.
     */
    Constraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        if (gridSubset == null || pvm == null) {
            throw new IllegalArgumentException("gridSubset and pvm cannot be null");
        }

        this.pvm = pvm;
        this.lastOpinions = new HashMap<>();

        for (Cell cell : gridSubset) {
            Map<Integer, Boolean> lastOpinionsForCell = new HashMap<>();
            this.lastOpinions.put(cell, lastOpinionsForCell);
            pvm.linkConstraint(cell, this, lastOpinionsForCell);
            cell.addLinkedValueManager(pvm);
        }

        resetProp();
    }

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
        return lastOpinions.getOrDefault(cell, Collections.emptyMap());
    }

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

    protected Map<Integer, Boolean> updateLastOpinion(Cell cell, Map<Integer, Boolean> newOpinionsForCell,
            boolean wideReach) {
        if (cell == null || newOpinionsForCell == null) {
            throw new IllegalArgumentException("Cell and opinions map cannot be null");
        }

        Map<Integer, Boolean> lastOpinionsForCell = this.lastOpinions.computeIfAbsent(cell, k -> new HashMap<>());
        for (Map.Entry<Integer, Boolean> entry : newOpinionsForCell.entrySet()) {
            Integer value = entry.getKey();
            if (!cell.getPossibleValues().contains(value)) {
                continue;
            }
            Boolean newOpinion = Boolean.TRUE.equals(entry.getValue());
            Boolean oldOpinion = lastOpinionsForCell.getOrDefault(value, false);
            if (!oldOpinion.equals(newOpinion)) {
                updatePossibleValuesManager(cell, value, newOpinion, wideReach);
                lastOpinionsForCell.put(value, newOpinion);
            }
        }
        return lastOpinionsForCell;
    }

    @Override
    public boolean innerRulesPropagateCell(Cell cell, Integer oldValue) {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        if (!cells.contains(cell)) {
            return false;
        }

        if ((oldValue == null && cell.getValue() != null) || (oldValue != null && !oldValue.equals(cell.getValue()))) {
            for (Cell c : cells) {
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
        if (targetCell == null || changedCell == null) {
            throw new IllegalArgumentException("Target cell and changed cell cannot be null");
        }
        return generateInnerOpinions(targetCell);
    }

    @Override
    public boolean wideReachRulesPossibleValues(Cell cell, Integer affectedValue) {
        return false;
    }

    protected int updatePossibleValuesManager(Cell cell, Integer value, Boolean newValueOpinion, boolean wideReach) {
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
