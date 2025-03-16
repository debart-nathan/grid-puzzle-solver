package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UniqueValueConstraint extends Constraint {

    private final Map<List<Cell>, Map<Cell, Map<Integer, Boolean>>> ruleCache = new ConcurrentHashMap<>(); // Cache for
                                                                                                           // Hidden/Naked
                                                                                                           // N rules
    private final Map<List<Cell>, Map<Integer, List<List<Cell>>>> combinationCache = new ConcurrentHashMap<>(); // Cache
                                                                                                                // for
                                                                                                                // combinations

    public UniqueValueConstraint(List<Cell> gridSubset, PossibleValuesManager pvm) {
        super(gridSubset, pvm);
    }

    @Override
    public boolean wideReachRulesPossibleValues(Cell cell, Integer affectedValue) {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        if (!cells.contains(cell)) {
            return false;
        }

        // Filter out solved cells
        List<Cell> unsolvedCells = cells.stream()
                .filter(c -> !c.isSolved())
                .toList();

        // Early exit if no unsolved cells
        if (unsolvedCells.isEmpty()) {
            return false;
        }

        // Get all distinct valid values in the unsolved cells
        Set<Integer> allValidValues = unsolvedCells.stream()
                .flatMap(c -> pvm.getValidValues(c).stream())
                .collect(Collectors.toSet());

        // Early exit if the number of distinct valid values != number of unsolved cells
        if (allValidValues.size() != unsolvedCells.size()) {
            return false;
        }

        // Precompute valid values for all unsolved cells
        Map<Cell, Set<Integer>> validValuesMap = unsolvedCells.stream()
                .collect(Collectors.toMap(c -> c, pvm::getValidValues));

        // Apply Hidden N and Naked N Rules (use cache if available)
        Map<Cell, Map<Integer, Boolean>> newOpinions = ruleCache.computeIfAbsent(unsolvedCells, k -> {
            Map<Cell, Map<Integer, Boolean>> opinions = new HashMap<>();
            applyHiddenAndNakedNRules(opinions, unsolvedCells, validValuesMap);
            return opinions;
        });

        // Update last opinions
        for (Map.Entry<Cell, Map<Integer, Boolean>> entry : newOpinions.entrySet()) {
            updateLastOpinion(entry.getKey(), entry.getValue(), true);
        }

        return true;
    }

    private void applyHiddenAndNakedNRules(Map<Cell, Map<Integer, Boolean>> newOpinions,
            List<Cell> unsolvedCells,
            Map<Cell, Set<Integer>> validValuesMap) {
        // Determine the maximum combination size to consider
        int maxCombinationSize = Math.min(4, unsolvedCells.size() - 1);

        // Iterate over all possible combination sizes
        for (int combinationSize = 2; combinationSize <= maxCombinationSize; combinationSize++) {
            // Create a final copy of combinationSize for use in the lambda
            final int finalCombinationSize = combinationSize;

            // Generate all possible combinations of cells for the current size (use cache
            // if available)
            List<List<Cell>> combinations = combinationCache
                    .computeIfAbsent(unsolvedCells, k -> new HashMap<>())
                    .computeIfAbsent(finalCombinationSize,
                            k -> generateCombinations(unsolvedCells, finalCombinationSize));

            // Process each combination
            for (List<Cell> combination : combinations) {
                // Find all valid values for cells in the current combination
                Set<Integer> validValuesInCombination = combination.stream()
                        .flatMap(cell -> validValuesMap.getOrDefault(cell, Collections.emptySet()).stream())
                        .collect(Collectors.toSet());

                // Hidden N rule
                // Find values to remove (valid values of cells not in the combination)
                Set<Integer> valuesToRemove = unsolvedCells.stream()
                        .filter(cell -> !combination.contains(cell)) // Exclude cells in the combination
                        .flatMap(cell -> validValuesMap.getOrDefault(cell, Collections.emptySet()).stream())
                        .collect(Collectors.toSet());

                // Calculate remaining valid values after removing values from other cells
                Set<Integer> remainingValidValues = new HashSet<>(validValuesInCombination);
                remainingValidValues.removeAll(valuesToRemove);

                // If the remaining valid values match the combination size, apply the Hidden N
                // rule
                if (remainingValidValues.size() == combination.size()) {
                    // Forbid other values in the combination
                    combination.forEach(cell -> valuesToRemove.forEach(
                            value -> newOpinions.computeIfAbsent(cell, k -> new HashMap<>()).put(value, true)));
                }

                // Naked N rule
                // Check if the number of valid values matches the combination size
                if (validValuesInCombination.size() == combination.size()) {
                    // Forbid the values outside the combination
                    validValuesInCombination.forEach(value -> unsolvedCells.stream()
                            .filter(cell -> !combination.contains(cell)) // Exclude cells in the combination
                            .forEach(cell -> newOpinions.computeIfAbsent(cell, k -> new HashMap<>()).put(value, true)));
                }
            }
        }
    }

    /**
     * Helper method to generate combinations of cells.
     *
     * @param cells The list of cells to generate combinations from.
     * @param n     The size of each combination.
     * @return A list of all possible combinations of size `n`.
     */
    private List<List<Cell>> generateCombinations(List<Cell> cells, int n) {
        // Early return if the combination size is invalid
        if (n > cells.size() || n > 9) {
            return Collections.emptyList();
        }

        List<List<Cell>> result = new ArrayList<>();
        generateCellCombinations(result, cells, new ArrayList<>(), 0, n);
        return result;
    }

    /**
     * Recursive helper method to generate combinations of cells.
     *
     * @param result  The list to store all valid combinations.
     * @param cells   The list of cells to generate combinations from.
     * @param current The current combination being built.
     * @param start   The starting index for the next cell to add.
     * @param n       The size of the combination.
     */
    private void generateCellCombinations(List<List<Cell>> result, List<Cell> cells, List<Cell> current, int start,
            int n) {
        // If the current combination is complete, add it to the result
        if (current.size() == n) {
            result.add(new ArrayList<>(current)); // Create a new list to avoid mutation
            return;
        }

        // Iterate through the cells to build combinations
        for (int i = start; i < cells.size(); i++) {
            current.add(cells.get(i)); // Add the current cell to the combination
            generateCellCombinations(result, cells, current, i + 1, n); // Recurse for the next cell
            current.remove(current.size() - 1); // Backtrack: remove the last cell to try the next one
        }
    }

    @Override
    public Map<Integer, Boolean> generateUpdatedInnerOpinions(Cell targetCell, Cell changedCell, Integer oldValue,
            Integer newValue) {
        if (targetCell == null || changedCell == null) {
            throw new IllegalArgumentException("Target cell and changed cell cannot be null");
        }

        Set<Integer> possibleValues = targetCell.getPossibleValues();
        Map<Integer, Boolean> newOpinion = new HashMap<>(
                this.lastOpinions.getOrDefault(targetCell, Collections.emptyMap()));

        if (newValue != null && possibleValues.contains(newValue)) {
            newOpinion.put(newValue, true);
        }

        if (oldValue != null && possibleValues.contains(oldValue)) {
            boolean isPresent = pvm.getCellsForConstraint(this).stream()
                    .anyMatch(c -> Objects.equals(c.getValue(), oldValue));
            newOpinion.put(oldValue, isPresent);
        }

        return newOpinion;
    }

    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        Set<Integer> possibleValues = cell.getPossibleValues();
        Map<Integer, Boolean> newOpinions = new HashMap<>(possibleValues.size());

        possibleValues.forEach(value -> {
            boolean isPresent = pvm.getCellsForConstraint(this).stream()
                    .anyMatch(c -> Objects.equals(c.getValue(), value));
            newOpinions.put(value, isPresent);
        });

        return newOpinions;
    }

    @Override
    public boolean isRuleBroken() {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        Set<Integer> uniqueValues = new HashSet<>(cells.size());

        for (Cell cell : cells) {
            Integer cellValue = cell.getValue();
            if (cellValue != null && !uniqueValues.add(cellValue)) {
                return true; // Found a duplicate value
            }
        }

        return false; // No duplicates found
    }

    @Override
    public Map.Entry<Cell, Integer> getSolvableCell() {
        Set<Cell> unsolvedCells = pvm.getCellsForConstraint(this).stream()
                .filter(cell -> !cell.isSolved())
                .collect(Collectors.toSet());

        if (unsolvedCells.isEmpty()) {
            return null;
        }

        // Precompute valid values for all unsolved cells
        Map<Cell, Set<Integer>> cellValidValues = unsolvedCells.stream()
                .collect(Collectors.toMap(c -> c, pvm::getValidValues));

        // Get all distinct valid values across all unsolved cells
        Set<Integer> allValidValues = cellValidValues.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        // If the number of distinct valid values is greater than the number of unsolved
        // cells, return null
        if (allValidValues.size() > unsolvedCells.size()) {
            return null;
        }

        // Count how many times each value appears across all cells
        Map<Integer, Integer> valueCounts = new HashMap<>();
        for (Set<Integer> values : cellValidValues.values()) {
            for (Integer value : values) {
                valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
            }
        }

        // Identify values that appear only once (endemic values)
        Set<Integer> endemicValues = valueCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Find the cell that contains a single endemic value
        for (Map.Entry<Cell, Set<Integer>> entry : cellValidValues.entrySet()) {
            Cell cell = entry.getKey();
            Set<Integer> values = entry.getValue();

            for (Integer value : values) {
                if (endemicValues.contains(value)) {
                    // Return the cell and the endemic value
                    return new AbstractMap.SimpleEntry<>(cell, value);
                }
            }
        }

        // No hidden single found
        return null;
    }
}