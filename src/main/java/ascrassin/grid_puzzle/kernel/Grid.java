package ascrassin.grid_puzzle.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/**
 * Class that represents a Grid in a grid puzzle.
 * 
 * <p>This class contains the grid structure and provides methods to manage the cells in the grid.</p>
 */
public class Grid {
    protected List<List<Cell>> cellGrid;
    protected Integer rows;
    protected Integer cols;

    /**
     * Constructor to create an instance of Grid.
     * 
     * @param puzzle     a two-dimensional array of integers representing the puzzle.
     * @param minValue   the lowest value that can be set in a cell.
     * @param maxValue   the highest value that can be set in a cell.
     * @throws IllegalArgumentException if the puzzle is empty
     */
    public Grid(Integer[][] puzzle, Integer minValue, Integer maxValue) {
        if (puzzle.length == 0 || puzzle[0].length == 0) {
            throw new IllegalArgumentException("Puzzle cannot be empty");
        }
        this.rows = puzzle.length;
        this.cols = puzzle[0].length;
        this.cellGrid = new ArrayList<>();
        for (Integer i = 0; i < rows; i++) {
            List<Cell> row = new ArrayList<>();
            for (Integer j = 0; j < cols; j++) {
                Cell cell=new Cell(minValue, maxValue,puzzle[i][j]);
                row.add(cell);
            }
            cellGrid.add(row);
        }
    }

    /**
     * Returns the cell grid as a list of lists of cells.
     * @return the cell grid as a list of lists of cells.
     */
    public List<List<Cell>> getCellGrid() {
        return cellGrid;
    }

    /**
     * Returns a collection of all cells in the grid.
     * @return A collection of all cells in the grid.
     */
    public Collection<Cell> getAllCells() {
        List<Cell> allCells = new ArrayList<>();
        for (List<Cell> row : cellGrid) {
            allCells.addAll(row);
        }
        return allCells;
    }

    /**
     * Returns the cell at the given row and column indices.
     * @param row   the row index.
     * @param col   the column index.
     * @return the cell at the given row and column indices.
     * @throws IllegalArgumentException if the index is invalid
     */
    public Cell getCellAt(Integer row, Integer col) {
        if (!isValidIndex(row, rows) || !isValidIndex(col, cols)) {
            throw new IllegalArgumentException("Invalid index");
        }
        return cellGrid.get(row).get(col);
    }

    /**
     * Returns the value of the cell at the given row and column indices.
     * @param row   the row index.
     * @param col   the column index.
     * @return the value of the cell at the given row and column indices.
     * @throws IllegalArgumentException if the index is invalid
     */
    public Integer getCellValue(Integer row, Integer col) {
        if (!isValidIndex(row, rows) || !isValidIndex(col, cols)) {
            throw new IllegalArgumentException("Invalid index");
        }
        return cellGrid.get(row).get(col).getValue();
    }

    /**
     * Sets the value of the cell at the given row and column indices.
     * @param row   the row index.
     * @param col   the column index.
     * @param value the value to be set.
     * @throws IllegalArgumentException if the index is invalid
     */
    public void setCellValue(Integer row, Integer col, Integer value) {
        if (!isValidIndex(row, rows) || !isValidIndex(col, cols)) {
            throw new IllegalArgumentException("Invalid index");
        }
        cellGrid.get(row).get(col).setValue(value);
    }

    /**
     * Checks if the entire grid is solved.
     * A grid is considered solved if all cells have a non-null value.
     * @return True if the grid is solved, false otherwise.
     */
    public boolean isSolved() {
        for (Integer r = 0; r < rows; r++) {
            for (Integer c = 0; c < cols; c++) {
                if (cellGrid.get(r).get(c).getValue() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the number of rows in the grid.
     * @return The number of rows in the grid.
     */
    public Integer getRowsCount() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     * @return The number of columns in the grid.
     */
    public Integer getColsCount() {
        return cols;
    }

    protected boolean isValidIndex(Integer index, Integer size) {
        return index >= 0 && index < size;
    }
}