package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public class TransitifComparatorConstraint extends Constraint {
    private BiPredicate<Integer, Integer> comparator;

    public TransitifComparatorConstraint(List<Cell> gridSubset, PossibleValuesManager pvm,
            BiPredicate<Integer, Integer> comparator) {
        super(gridSubset, pvm);
        this.comparator = comparator;
    }


    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        Map<Integer, Boolean> newOpinions = new ConcurrentHashMap<>();
        Set<Integer> values = cell.getPossibleValues();
        List<Cell> cells = pvm.getCellsForConstraint(this);

        int index = cells.indexOf(cell);
        List<Integer> valuesBefore = cells.subList(0, index).stream().map(Cell::getValue).toList();
        List<Integer> valuesAfter = cells.subList(index + 1, cells.size()).stream().map(Cell::getValue).toList();

        values.parallelStream()
                .forEach(value -> {
                    boolean canBePlaced = true;
                    for (Integer before : valuesBefore) {
                        if (!comparator.test(before, value)) {
                            canBePlaced = false;
                            break;
                        }
                    }
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