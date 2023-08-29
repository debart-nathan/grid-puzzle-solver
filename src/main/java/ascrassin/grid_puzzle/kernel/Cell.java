package ascrassin.grid_puzzle.kernel;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a cell in a grid puzzle.
 * 
 * <p>This class maintains the value of the cell, a flag indicating if the cell is solved, 
 * a flag indicating if the cell is a default cell, and a list of possible values for the cell.
 * 
 * <p>It provides methods to get and set the cell value, check if the cell is solved or default, 
 * and manage the possible values for the cell.
 */
public class Cell {
    private Integer value;
    private boolean isSolved;
    private boolean isDefault;
    private List<Integer> possibleValues;

    public Cell(int minValue, int maxValue, Integer value) {
        this.value = value;
        this.isSolved = value != null;
        this.isDefault = value != null;
        this.possibleValues = new ArrayList<>();
        if (!isSolved) {
            for (int i = minValue; i <= maxValue; i++) {
                possibleValues.add(i);
            }
        }
    }

    public Integer getValue() {
        return value;
    }

    public boolean setValue(Integer value) {
        if (!isDefault) {
            if (possibleValues.contains(value) || value == null) {
                this.value = value;
                this.isSolved = value != null;
                return true;
            }
            return false;

        }
        return false;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public List<Integer> getPossibleValues() {
        return possibleValues;
    }
}