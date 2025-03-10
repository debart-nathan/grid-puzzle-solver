package ascrassin.grid_puzzle.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class ConstraintTest {

    @Mock
    private PossibleValuesManager pvm;

    @Mock
    private Cell cell1, cell2;

    private List<Cell> cells;
    private Constraint constraint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<Integer> possibleValues1 = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> possibleValues2 = new HashSet<>(Arrays.asList(2, 3, 4));

        when(cell1.getPossibleValues()).thenReturn(possibleValues1);
        when(cell2.getPossibleValues()).thenReturn(possibleValues2);
        cells = new ArrayList<>();
        cells.add(cell1);
        cells.add(cell2);

        // Initialize constraint with mocked objects
        constraint = new TestableConstraint(cells, pvm);
        when(pvm.getCellsForConstraint(constraint)).thenReturn(cells);
    }



    @Nested
    class CleanupConstraintTests {
        @Test
        void testCleanupConstraintRemovesConstraintFromCells() {
            reset(pvm);
            when(pvm.getCellsForConstraint(constraint)).thenReturn(cells);
            constraint.cleanupConstraint();

            verify(pvm).unlinkConstraint(cell1,constraint,null);
            verify(pvm).unlinkConstraint(cell2,constraint,null);
        }

        @Test
        void testCleanupConstraintClearsVariables() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, true);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            constraint.cleanupConstraint();

            assertTrue(constraint.lastOpinions.isEmpty());
        }
    }

    @Nested
    class UpdateLastOpinionTests {
        @Test
        void testUpdateLastOpinionUpdatesPossibleValuesManager() {
            Map<Integer, Boolean> opinions = new HashMap<>();
            opinions.put(2, true);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, opinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(2, false);

            reset(pvm);            
            constraint.updateLastOpinion(cell1, newOpinions , false);

            verify(pvm).allowCellValue(cell1, 2 , false);
        }

        @Test
        void testUpdateLastOpinionNotUpdateUnchangedOpinions() {
            Map<Integer, Boolean> opinions = new HashMap<>();
            opinions.put(1, true);
            opinions.put(41, false);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, opinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> unchangedOpinions = new HashMap<>();
            unchangedOpinions.put(1, true);
            unchangedOpinions.put(41, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, unchangedOpinions, false);
            verify(pvm, never()).allowCellValue(any(Cell.class), anyInt() , eq(false));
            verify(pvm, never()).forbidCellValue(any(Cell.class), anyInt() , eq(false));

        }

        @Test
        void testUpdateLastOpinionChangesPositiveToNegative() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, true);
            initialOpinions.put(2, false); // Add another value

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(1, false);
            newOpinions.put(2, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, newOpinions, false);

            verify(pvm, never()).allowCellValue(eq(cell2), anyInt() , eq(false));
            verify(pvm, never()).forbidCellValue(any(), anyInt() , eq(false));
            verify(pvm, never()).allowCellValue(cell1, 2, false);
            verify(pvm).allowCellValue(cell1, 1, false);

        }

        @Test
        void testUpdateLastOpinionChangesNegativeToPositive() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, false);
            initialOpinions.put(2, true); // Add another value

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(1, true);
            newOpinions.put(2, true);

            reset(pvm);
            constraint.updateLastOpinion(cell1, newOpinions, false);

            verify(pvm, never()).forbidCellValue(eq(cell2), anyInt(), eq(false));
            verify(pvm, never()).allowCellValue(any(), anyInt(), eq(false));
            verify(pvm, never()).forbidCellValue(cell1, 2, false);

            verify(pvm).forbidCellValue(cell1, 1, false);
        }
    }

    
}