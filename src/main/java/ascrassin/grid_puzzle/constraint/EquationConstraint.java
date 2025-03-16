package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a constraint based on a mathematical equation or inequality.
 * The equation is passed as a string, and variables are represented as [Xi],
 * where i is the index of the cell in the grid subset.
 * This constraint generates all valid combinations of values that satisfy the
 * equation and forbids values that are not part of any valid combination.
 */
public class EquationConstraint extends Constraint {

    private final String equation; // The equation or inequality (e.g., "[X1] + [X2] == 10")

    /**
     * Constructs a new EquationConstraint instance.
     *
     * @param gridSubset The list of cells this constraint applies to.
     * @param pvm        The PossibleValuesManager to interface with.
     * @param equation   The equation or inequality as a string (e.g., "[X1] + [X2]
     *                   == 10").
     */
    public EquationConstraint(List<Cell> gridSubset, PossibleValuesManager pvm, String equation) {
        super(gridSubset, pvm);
        this.equation = equation;
    }

    @Override
    public boolean innerRulesPropagateCell(Cell cell, Integer oldValue) {
        if (!pvm.getCellsForConstraint(this).contains(cell)) {
            return false;
        }

        if ((oldValue == null && cell.getValue() != null) ||
                (oldValue != null && !oldValue.equals(cell.getValue()))) {

            Map<Cell, Map<Integer, Boolean>> newOpinions = generateFullInnerOpinions();
            for (Cell c : pvm.getCellsForConstraint(this)) {
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

        List<Cell> cells = this.pvm.getCellsForConstraint(this);
        Map<Cell, Map<Integer, Boolean>> newOpinions = generateFullInnerOpinions();
        for (Cell cell : cells) {
            updateLastOpinion(cell, newOpinions.get(cell), false);
        }
    }

    /**
     * Generates opinions for all cells in the grid subset based on the equation.
     *
     * @return A map of cells to their respective opinions (allowed or forbidden
     *         values).
     */
    public Map<Cell, Map<Integer, Boolean>> generateFullInnerOpinions() {
        return generateOpinions(cell -> cell.getPossibleValues());
    }

    /**
     * Checks if the rule is broken by evaluating all valid combinations of values.
     *
     * @return true if the rule is broken, false otherwise.
     */
    @Override
    public boolean isRuleBroken() {
        List<Cell> cells = pvm.getCellsForConstraint(this);
        List<Map<Cell, Integer>> validCombinations = generateCombinations(cells, cell -> pvm.getValidValues(cell));
        return validCombinations.isEmpty();
    }

    /**
     * Helper method to generate opinions or check rule validity based on a value
     * supplier.
     *
     * @param valueSupplier A function that provides the valid values for a cell.
     * @return A map of cells to their respective opinions (allowed or forbidden
     *         values).
     */
    private Map<Cell, Map<Integer, Boolean>> generateOpinions(Function<Cell, Set<Integer>> valueSupplier) {
        Map<Cell, Map<Integer, Boolean>> fullOpinions = new HashMap<>();

        List<Cell> cells = pvm.getCellsForConstraint(this);
        List<Map<Cell, Integer>> validCombinations = generateCombinations(cells, valueSupplier);

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

    /**
     * Generates all valid combinations of values that satisfy the equation.
     *
     * @param cells         The list of cells in the grid subset.
     * @param valueSupplier A function that provides the valid values for a cell.
     * @return A list of maps, where each map represents a valid combination of cell
     *         values.
     */
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

    /**
     * Recursively generates all valid combinations of values that satisfy the
     * equation.
     *
     * @param cells             The list of cells in the grid subset.
     * @param baseValues        A map of cells to their valid values.
     * @param index             The current index in the cells list.
     * @param currentCombo      The current combination of cell values being built.
     * @param validCombinations The list of all valid combinations.
     */
    private void generateCombinationsRecursive(List<Cell> cells, Map<Cell, Set<Integer>> baseValues, int index,
            Map<Cell, Integer> currentCombo, List<Map<Cell, Integer>> validCombinations) {
        if (index == cells.size()) {
            String substitutedEquation = substituteCombinationIntoEquation(cells, currentCombo);
            if (evaluateEquation(substitutedEquation)) {
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

    /**
     * Substitutes a combination of cell values into the equation.
     *
     * @param cells       The list of cells in the grid subset.
     * @param combination The combination of cell values to substitute.
     * @return The equation with cell values substituted.
     */
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

    /**
     * Evaluates the equation or inequality.
     *
     * @param equation The equation or inequality as a string.
     * @return true if the equation is satisfied, false otherwise.
     */
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

    @Override
    public Map<Integer, Boolean> generateInnerOpinions(Cell cell) {
        // Delegate to generateFullInnerOpinions and return the opinions for the
        // specific cell
        Map<Cell, Map<Integer, Boolean>> fullOpinions = generateFullInnerOpinions();
        return fullOpinions.getOrDefault(cell, new HashMap<>());
    }
}