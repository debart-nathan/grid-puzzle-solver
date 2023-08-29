package ascrassin.grid_puzzle.kernel;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a grid in a grid puzzle.
 * 
 * <p>This class maintains the grid structure and provides methods to manage the cells in the grid.
 */
public class Grid {
    private List<List<Cell>> cellGrid;

    public Grid(int[][] puzzle, int minValue, int maxValue) {
        int rows = puzzle.length;
        int cols = puzzle[0].length;
        cellGrid = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Cell> row = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                row.add(new Cell(puzzle[i][j], minValue, maxValue));
            }
            cellGrid.add(row);
        }
    }

    public List<List<Cell>> getCellGrid() {
        return cellGrid;
    }

    public Cell getCellAt(int row, int col) {
        return cellGrid.get(row).get(col);
    }

    public int getCellValue(int row, int col) {
        return cellGrid.get(row).get(col).getValue();
    }

    public void setCellValue(int row, int col, int value) {
        cellGrid.get(row).get(col).setValue(value);
    }
}