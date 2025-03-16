package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EquationConstraint extends Constraint {

    private final String equation; // The equation or inequality (e.g., "[X1] + [X2] == 10")
    private final Map<List<Integer>, Boolean> equationCache = new ConcurrentHashMap<>(); // Cache for equation
                                                                                         // evaluations
    private final Map<List<Cell>, List<Map<Cell, Integer>>> combinationCache = new ConcurrentHashMap<>(); // Cache for
                                                                                                          // valid
                                                                                                          // combinations

    public EquationConstraint(List<Cell> gridSubset, PossibleValuesManager pvm, String equation) {
        super(gridSubset, pvm);
        this.equation = Objects.requireNonNull(equation, "Equation cannot be null");
    }

    @Override
    public boolean innerRulesPropagateCell(Cell cell, Integer oldValue) {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        if (!cells.contains(cell)) {
            return false;
        }

        if ((oldValue == null && cell.getValue() != null) || (oldValue != null && !oldValue.equals(cell.getValue()))) {
            // Invalidate the cache when a cell's value changes
            combinationCache.clear();
            equationCache.clear();

            Map<Cell, Map<Integer, Boolean>> newOpinions = generateFullInnerOpinions();
            for (Cell c : cells) {
                updateLastOpinion(c, newOpinions.get(c), false);
            }
            return true;
        }
        return false;
    }

    @Override
    public void resetProp() {
        if (this.pvm == null) {
            System.out.println("Warning: PossibleValuesManager is null");
            return;
        }

        // Invalidate the cache when resetting
        combinationCache.clear();
        equationCache.clear();

        List<Cell> cells = this.pvm.getCellsForConstraint(this);
        Map<Cell, Map<Integer, Boolean>> newOpinions = generateFullInnerOpinions();
        for (Cell cell : cells) {
            updateLastOpinion(cell, newOpinions.get(cell), false);
        }
    }

    @Override
    public boolean isRuleBroken() {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        List<Map<Cell, Integer>> validCombinations = generateCombinations(cells, pvm::getValidValues);
        return validCombinations.isEmpty();
    }

    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        Map<Cell, Map<Integer, Boolean>> fullOpinions = generateFullInnerOpinions();
        return fullOpinions.getOrDefault(cell, new HashMap<>());
    }

    private Map<Cell, Map<Integer, Boolean>> generateFullInnerOpinions() {
        return generateOpinions(Cell::getPossibleValues);
    }

    private Map<Cell, Map<Integer, Boolean>> generateOpinions(Function<Cell, Set<Integer>> valueSupplier) {
        Map<Cell, Map<Integer, Boolean>> fullOpinions = new HashMap<>();
        List<Cell> cells = pvm.getCellsForConstraint(this);

        // Generate valid combinations (use cache if available)
        List<Map<Cell, Integer>> validCombinations = combinationCache.computeIfAbsent(cells,
                k -> generateCombinations(cells, valueSupplier));

        // Track which values are part of at least one valid combination
        Map<Cell, Set<Integer>> validValues = new HashMap<>();
        for (Cell cell : cells) {
            validValues.put(cell, new HashSet<>());
        }

        for (Map<Cell, Integer> combination : validCombinations) {
            for (Map.Entry<Cell, Integer> entry : combination.entrySet()) {
                Cell cell = entry.getKey();
                Integer value = entry.getValue();
                validValues.get(cell).add(value);
            }
        }

        // Generate opinions for each cell
        for (Cell cell : cells) {
            Map<Integer, Boolean> cellOpinions = new HashMap<>();
            Set<Integer> possibleValues = valueSupplier.apply(cell);

            for (Integer value : possibleValues) {
                cellOpinions.put(value, !validValues.get(cell).contains(value)); // Forbid the value if not contained,
                                                                                 // otherwise allow
            }
            fullOpinions.put(cell, cellOpinions);
        }

        return fullOpinions;
    }

    private List<Map<Cell, Integer>> generateCombinations(List<Cell> cells,
            Function<Cell, Set<Integer>> valueSupplier) {
        List<Map<Cell, Integer>> validCombinations = new ArrayList<>();
        Map<Cell, Set<Integer>> baseValues = new HashMap<>();

        for (Cell cell : cells) {
            if (!cell.isSolved()) {
                baseValues.put(cell, valueSupplier.apply(cell));
            }
        }

        generateCombinationsRecursive(cells, baseValues, 0, new HashMap<>(), validCombinations);
        return validCombinations;
    }

    private void generateCombinationsRecursive(List<Cell> cells, Map<Cell, Set<Integer>> baseValues, int index,
            Map<Cell, Integer> currentCombo, List<Map<Cell, Integer>> validCombinations) {
        if (index == cells.size()) {
            // Check if the combination is already cached
            List<Integer> comboValues = new ArrayList<>(currentCombo.values());
            boolean isValid = equationCache.computeIfAbsent(comboValues, k -> evaluateCombination(cells, currentCombo));
            if (isValid) {
                validCombinations.add(new HashMap<>(currentCombo));
            }
            return;
        }

        Cell cell = cells.get(index);
        if (cell.isSolved()) {
            currentCombo.put(cell, cell.getValue());
            generateCombinationsRecursive(cells, baseValues, index + 1, currentCombo, validCombinations);
            currentCombo.remove(cell);
        } else {
            Set<Integer> validValues = baseValues.get(cell);
            for (Integer value : validValues) {
                currentCombo.put(cell, value);
                generateCombinationsRecursive(cells, baseValues, index + 1, currentCombo, validCombinations);
                currentCombo.remove(cell);
            }
        }
    }

    private boolean evaluateCombination(List<Cell> cells, Map<Cell, Integer> combination) {
        String substitutedEquation = substituteCombinationIntoEquation(cells, combination);
        return evaluateEquation(substitutedEquation);
    }

    private String substituteCombinationIntoEquation(List<Cell> cells, Map<Cell, Integer> combination) {
        String substitutedEquation = equation;

        // Replace [Xi] with the corresponding cell values
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            String variable = "[X" + (i + 1) + "]";
            substitutedEquation = substitutedEquation.replace(variable, combination.get(cell).toString());
        }

        return substitutedEquation;
    }

    private boolean evaluateEquation(String equation) {
        // Split the equation into left-hand side (LHS) and right-hand side (RHS)
        String[] parts = equation.split("[><=]+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid equation: " + equation);
        }

        String lhs = parts[0].trim();
        String rhs = parts[1].trim();

        // Evaluate the LHS and RHS using exp4j
        Expression lhsExpression = new ExpressionBuilder(lhs).build();
        Expression rhsExpression = new ExpressionBuilder(rhs).build();

        double lhsResult = lhsExpression.evaluate();
        double rhsResult = rhsExpression.evaluate();

        // Compare based on the operator
        if (equation.contains("==")) {
            return lhsResult == rhsResult;
        } else if (equation.contains("!=")) {
            return lhsResult != rhsResult;
        } else if (equation.contains(">=")) {
            return lhsResult >= rhsResult;
        } else if (equation.contains("<=")) {
            return lhsResult <= rhsResult;
        } else if (equation.contains(">")) {
            return lhsResult > rhsResult;
        } else if (equation.contains("<")) {
            return lhsResult < rhsResult;
        } else {
            throw new IllegalArgumentException("Unsupported operator in equation: " + equation);
        }
    }
}