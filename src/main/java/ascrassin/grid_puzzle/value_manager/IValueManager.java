package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.Set;

public interface IValueManager {
    void linkConstraint(Cell cell);
    void unlinkConstraint(Cell cell);
    void allowCellValue(Cell cell, int value);
    void forbidCellValue(Cell cell, int value);
    Set<Integer> getValidValues(Cell cell);
    boolean canSetValue(Cell cell, int value);
}