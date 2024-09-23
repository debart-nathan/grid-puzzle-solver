package ascrassin.grid_puzzle.kernel;

import ascrassin.grid_puzzle.constraint.Constraint;
import ascrassin.grid_puzzle.value_manager.IValueManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solver {
    private Grid grid;
    private List<Constraint> constraints;
    private IValueManager valueManager;

    public Solver(Grid grid, List<Constraint> constraints, IValueManager valueManager) {
        this.grid = grid;
        this.constraints = constraints;
        this.valueManager = valueManager;
    }

    public boolean solve() {
        System.out.println("Starting solver");
        while (true) {
            if (grid.isSolved()) {
                System.out.println("Grid solved successfully");
                return true;
            }
    
            boolean progress = false;
    
            // Print constraint processing start
            System.out.println("Processing constraints");
    
            for (Constraint constraint : constraints) {
                Map.Entry<Cell, Integer> solvableCell = constraint.getSolvableCell();
                if (solvableCell != null) {
                    Cell cell = solvableCell.getKey();
                    Integer value = solvableCell.getValue();
                    cell.setValue(value);
                    System.out.printf("Updated cell %s: %d%n", cell, value);
                    progress = true;
                    break;
                }
            }
    
            if (!progress) {
                System.out.println("No progress made by constraints");
    
                // Print value assignment attempt
                System.out.println("Attempting to assign values to cells");
                
                for (Cell cell : grid.getAllCells()) {
                    Set<Integer> validValues = valueManager.getValidValues(cell);
                    if (validValues.size() == 1) {
                        Integer newValue = validValues.iterator().next();
                        if (!newValue.equals(cell.getValue())) {
                            cell.setValue(newValue);
                            System.out.printf("Assigned new value %d to cell %s%n", newValue, cell);
                            progress = true;
                            break;
                        }
                    }
                }
            }
    
            if (!progress && !grid.isSolved()) {
                System.out.println("No progress made and grid is not solved yet");
                return false;
            }
        }
    }

}