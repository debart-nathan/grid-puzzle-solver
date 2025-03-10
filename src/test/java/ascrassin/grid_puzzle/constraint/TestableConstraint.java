package ascrassin.grid_puzzle.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;

/**
 * DO NOT USE :THIS IS ONLY FOR TESTING PURPOSES ON CONSTRAINT
 */
public class TestableConstraint extends Constraint {
    protected TestableConstraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        super(gridSubset, pvm);
    }

    @Override
    public boolean wideReachRulesPossibleValues(Cell cell, Integer value) {
        return false;
    }

    @Override
    public boolean isRuleBroken() {
        return false;
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        return null;
    }

    @Override
    public boolean innerRulesPropagateCell(Cell cell, Integer oldValue) {
        return false;
    }

    @Override
    public Map<Integer, Boolean> generateUpdatedInnerOpinions(Cell targetCell, Cell changedCell, Integer oldValue,
            Integer newValue) {
        return new HashMap<>();
    }

    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        return new HashMap<>();
    }
}