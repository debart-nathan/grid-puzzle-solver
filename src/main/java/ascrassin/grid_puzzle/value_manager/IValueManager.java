package ascrassin.grid_puzzle.value_manager;

import ascrassin.grid_puzzle.kernel.Cell;
import java.util.Set;

public interface IValueManager {
    void linkConstraint(Cell cell);
    void unlinkConstraint(Cell cell);
    void allowCellValue(Cell cell, Integer value);
    void forbidCellValue(Cell cell, Integer value);
    Set<Integer> getValidValues(Cell cell);
    boolean canSetValue(Cell cell, Integer value);
}
