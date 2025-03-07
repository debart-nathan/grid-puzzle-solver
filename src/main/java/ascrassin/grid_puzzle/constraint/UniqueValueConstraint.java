package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a unique value constraint in a grid puzzle solver.
 * This constraint ensures that each cell in the puzzle grid contains a unique
 * value.
 */
public class UniqueValueConstraint extends Constraint {

    public UniqueValueConstraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        super(gridSubset, pvm);
    }

    @Override
    public boolean propagateCell(Cell cell, Integer oldValue) {
        if (!gridSubset.contains(cell)) {
            // If the cell is not in gridSubset, skip propagation
            return false;
        }

        if ((oldValue == null && cell.getValue() != null) ||
                (oldValue != null && !oldValue.equals(cell.getValue()))) {
            // Generate updated opinions for the cell
            

            // Update last opinion for all cells in the subset
            for (Cell c : gridSubset) {
                Map<Integer, Boolean> newOpinions = generateUpdatedOpinions(c, cell, oldValue, cell.getValue());
                updateLastOpinion(c, newOpinions);
            }

            return true;
        }
        return false;
    }

    @Override
    public Map<Integer, Boolean> generateUpdatedOpinions(Cell targetCell, Cell changedCell, Integer oldValue, Integer newValue) {
        Set<Integer> possibleValues = targetCell.getPossibleValues();
    
        Map<Integer, Boolean> newOpinion = new HashMap<>(this.lastOpinions.get(targetCell));
        
        // Update opinion based on new value
        if (newValue != null) {
            newOpinion.put(newValue, true);
        }
        
        // Update opinion based on old value
        if (oldValue != null && possibleValues.contains(oldValue)) {
            boolean isPresent = gridSubset.stream()
                    .anyMatch(c -> c.getValue() != null && c.getValue().equals(oldValue));
            newOpinion.put(oldValue, isPresent);
        }
        
        return newOpinion;
    }

    @Override
    public Map<Integer, Boolean> generateOpinions(Cell cell) {
        Map<Integer, Boolean> newOpinions = new HashMap<>();
        Set<Integer> possibleValues = cell.getPossibleValues();

        for (Integer value : possibleValues) {

            boolean isPresent = gridSubset.stream()
                    .anyMatch(c -> c.getValue() != null && c.getValue().equals(value));

            newOpinions.put(value, isPresent);
        }

        return newOpinions;
    }

    @Override
    public boolean isRuleBroken() {
        return gridSubset.stream()
                .anyMatch(this::hasDuplicateValue);
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        Set<Cell> unsolvedCells = gridSubset.stream()
                .filter(cell -> !cell.isSolved())
                .collect(Collectors.toSet());

        if (unsolvedCells.isEmpty()) {
            return null;
        }

        Map<Cell, Set<Integer>> cellValidValues = new HashMap<>();
        Set<Integer> allUniqueValues = new HashSet<>();

        for (Cell c : unsolvedCells) {
            Set<Integer> validValues = pvm.getValidValues(c);
            cellValidValues.put(c, validValues);
            allUniqueValues.addAll(validValues);
        }

        if (allUniqueValues.size() != unsolvedCells.size()) {
            return null;
        }

        return findHiddenSingle(cellValidValues);
    }

    private Map.Entry<Cell, Integer> findHiddenSingle(Map<Cell, Set<Integer>> cellValidValues) {
        Map<Integer, Integer> valueCounts = new HashMap<>();
        for (Set<Integer> values : cellValidValues.values()) {
            for (Integer value : values) {
                valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
            }
        }

        Set<Integer> endemicValues = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : valueCounts.entrySet()) {
            if (entry.getValue() == 1) {
                endemicValues.add(entry.getKey());
            }
        }

        for (Map.Entry<Cell, Set<Integer>> entry : cellValidValues.entrySet()) {
            Cell cell = entry.getKey();
            Set<Integer> values = entry.getValue();
            Integer endemicValueCount = 0;
            for (Integer value : values) {
                if (endemicValues.contains(value)) {
                    endemicValueCount++;
                    if (endemicValueCount > 1) {
                        break;
                    }
                }
            }
            if (endemicValueCount == 1 && !endemicValues.isEmpty()) {
                for (Integer endemicValue : endemicValues) {
                    if (values.contains(endemicValue)) {
                        return new AbstractMap.SimpleEntry<>(cell, endemicValue);
                    }
                }
            }
        }
    
        return null;
    }

    private boolean hasDuplicateValue(Cell cell) {
        if (cell.getValue() == null) {
            return false;
        }

        Integer cellValue = cell.getValue();

        boolean hasDuplicate = false;
        for (Cell c : gridSubset) {
            if (!c.equals(cell)) {
                if (c.getValue() != null && c.getValue().equals(cellValue)) {
                    hasDuplicate = true;
                    break;
                }
            }
        }

        return hasDuplicate;
    }
}