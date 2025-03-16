package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public class TransitifComparatorConstraint extends Constraint {

    private final BiPredicate<Integer, Integer> comparator;
    private final Map<Cell, Map<Integer, Boolean>> opinionCache = new ConcurrentHashMap<>(); // Cache for opinions

    public TransitifComparatorConstraint(List<Cell> gridSubset, PossibleValuesManager pvm,
            BiPredicate<Integer, Integer> comparator) {
        super(gridSubset, pvm);
        this.comparator = Objects.requireNonNull(comparator, "Comparator cannot be null");
    }

    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        return opinionCache.computeIfAbsent(cell, k -> computeInnerOpinions(cell));
    }

    private Map<Integer, Boolean> computeInnerOpinions(Cell cell) {
        Map<Integer, Boolean> newOpinions = new ConcurrentHashMap<>();
        Set<Integer> values = cell.getPossibleValues();
        List<Cell> cells = pvm.getCellsForConstraint(this);

        int index = cells.indexOf(cell);
        if (index == -1) {
            throw new IllegalStateException("Cell not found in the constraint's subset");
        }

        // Precompute values before and after the current cell
        List<Integer> valuesBefore = cells.subList(0, index).stream()
                .map(Cell::getValue)
                .filter(Objects::nonNull)
                .toList();
        List<Integer> valuesAfter = cells.subList(index + 1, cells.size()).stream()
                .map(Cell::getValue)
                .filter(Objects::nonNull)
                .toList();

        // Parallelize the evaluation of possible values
        values.parallelStream().forEach(value -> {
            boolean canBePlaced = true;

            // Check against values before the current cell
            for (Integer before : valuesBefore) {
                if (!comparator.test(before, value)) {
                    canBePlaced = false;
                    break;
                }
            }

            // Check against values after the current cell
            if (canBePlaced) {
                for (Integer after : valuesAfter) {
                    if (!comparator.test(value, after)) {
                        canBePlaced = false;
                        break;
                    }
                }
            }

            newOpinions.put(value, !canBePlaced);
        });

        return newOpinions;
    }

    @Override
    public boolean isRuleBroken() {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        List<Integer> values = cells.stream()
                .map(Cell::getValue)
                .filter(Objects::nonNull) // Ignore null values
                .toList();

        // Check if any pair violates the predicate
        for (int i = 0; i < values.size(); i++) {
            for (int j = i + 1; j < values.size(); j++) {
                if (!comparator.test(values.get(i), values.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }
}