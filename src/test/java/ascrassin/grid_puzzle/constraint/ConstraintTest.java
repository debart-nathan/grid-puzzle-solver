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
import java.util.HashMap;
import java.util.Map;


class ConstraintTest {

    @Mock
    protected PossibleValuesManager pvm;

    @Mock
    protected Cell cell1, cell2;

    protected List<Cell> cells;
    protected TestableConstraint constraint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cells = new ArrayList<>();
        cells.add(cell1);
        cells.add(cell2);

        // Initialize constraint with mocked objects
        constraint = new TestableConstraint(cells, pvm);
    }

    @Nested
    class SetValueManagerTests {
        private PossibleValuesManager oldPVM;
        private PossibleValuesManager newPVM;
        
        @BeforeEach
        void setUp() {
            
            // Additional setup specific to these tests
            oldPVM = mock(PossibleValuesManager.class);
            newPVM = mock(PossibleValuesManager.class);
            
            // Set initial ValuesManager
            constraint.setValuesManager(oldPVM);
        }
        
        @Test
        void testSetValuesManagerWithNewManager() {
            // Arrange
            reset(oldPVM);
            reset(newPVM);
            // Act
            constraint.setValuesManager(newPVM);
            
            // Assert

            verify(oldPVM).unlinkConstraint(cell1);
            verify(oldPVM).unlinkConstraint(cell2);
            verify(newPVM).linkConstraint(cell1);
            verify(newPVM).linkConstraint(cell2);
        }
        
        @Test
        void testSetValuesManagerWithoutNewManager() {
            // Arrange
            reset(oldPVM);
            // Act
            constraint.setValuesManager(null);
            
            // Assert
            verify(oldPVM).unlinkConstraint(cell1);
            verify(oldPVM).unlinkConstraint(cell2);

        }
        
    }

    @Nested
    class ResetPropTests {
        @Test
        void testResetPropClearsLastOpinions() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, true);
            initialOpinions.put(2, false);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            constraint.resetProp();

            assertTrue(constraint.lastOpinions.isEmpty());
        }
    }

    @Nested
    class CleanupConstraintTests {
        @Test
        void testCleanupConstraintRemovesConstraintFromCells() {
            reset(pvm);
            constraint.cleanupConstraint();

            verify(pvm).unlinkConstraint(cell1);
            verify(pvm).unlinkConstraint(cell2);
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
            opinions.put(42, true);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, opinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(24, false);

            reset(pvm);            
            constraint.updateLastOpinion(cell1, newOpinions);

            verify(pvm).allowCellValue(cell1, 24);
        }

        @Test
        void testUpdateLastOpinionNotUpdateUnchangedOpinions() {
            Map<Integer, Boolean> opinions = new HashMap<>();
            opinions.put(42, true);
            opinions.put(41, false);

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, opinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> unchangedOpinions = new HashMap<>();
            unchangedOpinions.put(42, true);
            unchangedOpinions.put(41, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, unchangedOpinions);
            verify(pvm, never()).allowCellValue(any(Cell.class), anyInt());
            verify(pvm, never()).forbidCellValue(any(Cell.class), anyInt());

        }

        @Test
        void testUpdateLastOpinionChangesPositiveToNegative() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(42, true);
            initialOpinions.put(24, false); // Add another value

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(42, false);
            newOpinions.put(24, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, newOpinions);

            verify(pvm, never()).allowCellValue(eq(cell2), anyInt());
            verify(pvm, never()).forbidCellValue(any(), anyInt());
            verify(pvm, never()).allowCellValue(cell1, 24);

            verify(pvm).allowCellValue(cell1, 42);

        }

        @Test
        void testUpdateLastOpinionChangesNegativeToPositive() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(42, false);
            initialOpinions.put(24, true); // Add another value

            Map<Cell, Map<Integer, Boolean>> lastOpinions = new HashMap<>();
            lastOpinions.put(cell1, initialOpinions);
            constraint.lastOpinions = lastOpinions;

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(42, true);
            newOpinions.put(24, true);

            reset(pvm);
            constraint.updateLastOpinion(cell1, newOpinions);

            verify(pvm, never()).forbidCellValue(eq(cell2), anyInt());
            verify(pvm, never()).allowCellValue(any(), anyInt());
            verify(pvm, never()).forbidCellValue(cell1, 24);

            verify(pvm).forbidCellValue(cell1, 42);
        }
    }

    
}