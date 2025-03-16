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

import java.util.*;

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
            Map<Integer,Boolean> lastOpinionsCell1 = constraint.lastOpinions.get(cell1);
            Map<Integer,Boolean> lastOpinionsCell2 = constraint.lastOpinions.get(cell2);
            constraint.cleanupConstraint();

            verify(pvm).unlinkConstraint(cell1, constraint, lastOpinionsCell1 );
            verify(pvm).unlinkConstraint(cell2, constraint, lastOpinionsCell2);
        }

        @Test
        void testCleanupConstraintClearsVariables() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, true);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, initialOpinions);

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

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, opinions);

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(2, false);

            reset(pvm);            
            constraint.updateLastOpinion(cell1, newOpinions, false);

            verify(pvm).allowCellValue(cell1, 2, false);
        }

        @Test
        void testUpdateLastOpinionNotUpdateUnchangedOpinions() {
            Map<Integer, Boolean> opinions = new HashMap<>();
            opinions.put(1, true);
            opinions.put(41, false);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, opinions);

            Map<Integer, Boolean> unchangedOpinions = new HashMap<>();
            unchangedOpinions.put(1, true);
            unchangedOpinions.put(41, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, unchangedOpinions, false);
            verify(pvm, never()).allowCellValue(any(Cell.class), anyInt(), eq(false));
            verify(pvm, never()).forbidCellValue(any(Cell.class), anyInt(), eq(false));
        }

        @Test
        void testUpdateLastOpinionChangesPositiveToNegative() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, true);
            initialOpinions.put(2, false); // Add another value

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, initialOpinions);

            Map<Integer, Boolean> newOpinions = new HashMap<>();
            newOpinions.put(1, false);
            newOpinions.put(2, false);

            reset(pvm);
            constraint.updateLastOpinion(cell1, newOpinions, false);

            verify(pvm, never()).allowCellValue(eq(cell2), anyInt(), eq(false));
            verify(pvm, never()).forbidCellValue(any(), anyInt(), eq(false));
            verify(pvm, never()).allowCellValue(cell1, 2, false);
            verify(pvm).allowCellValue(cell1, 1, false);
        }

        @Test
        void testUpdateLastOpinionChangesNegativeToPositive() {
            Map<Integer, Boolean> initialOpinions = new HashMap<>();
            initialOpinions.put(1, false);
            initialOpinions.put(2, true); // Add another value

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, initialOpinions);

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

    @Nested
    class GetSolvableCellTests {
        @Test
        void testGetSolvableCellReturnsCorrectCellAndValue() {
            Map<Integer, Boolean> cell1Opinions = new HashMap<>();
            cell1Opinions.put(1, false);
            cell1Opinions.put(2, true);
            cell1Opinions.put(3, true);

            Map<Integer, Boolean> cell2Opinions = new HashMap<>();
            cell2Opinions.put(2, false);
            cell2Opinions.put(3, true);
            cell2Opinions.put(4, true);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, cell1Opinions);
            constraint.lastOpinions.put(cell2, cell2Opinions);

            Map.Entry<Cell, Integer> solvableCell = constraint.getSolvableCell();
            assertNotNull(solvableCell);
            assertEquals(cell1, solvableCell.getKey());
            assertEquals(1, solvableCell.getValue());
        }

        @Test
        void testGetSolvableCellReturnsNullWhenNoSolvableCell() {
            Map<Integer, Boolean> cell1Opinions = new HashMap<>();
            cell1Opinions.put(1, true);
            cell1Opinions.put(2, true);

            Map<Integer, Boolean> cell2Opinions = new HashMap<>();
            cell2Opinions.put(2, true);
            cell2Opinions.put(3, true);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, cell1Opinions);
            constraint.lastOpinions.put(cell2, cell2Opinions);

            assertNull(constraint.getSolvableCell());
        }
    }

    @Nested
    class InnerRulesPropagateCellTests {

        @Test
        void testInnerRulesPropagateCellDoesNotUpdateOpinionsWhenValueDoesNotChange() {
            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(2);

            Map<Integer, Boolean> cell1Opinions = new HashMap<>();
            cell1Opinions.put(1, false);
            cell1Opinions.put(2, true);

            Map<Integer, Boolean> cell2Opinions = new HashMap<>();
            cell2Opinions.put(2, false);
            cell2Opinions.put(3, true);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, cell1Opinions);
            constraint.lastOpinions.put(cell2, cell2Opinions);

            assertFalse(constraint.innerRulesPropagateCell(cell1, 1));
            verify(pvm, never()).allowCellValue(any(Cell.class), anyInt(), eq(false));
        }
    }

    @Nested
    class GenerateUpdatedInnerOpinionsTests {
        @Test
        void testGenerateUpdatedInnerOpinionsReturnsCorrectOpinions() {
            Map<Integer, Boolean> opinions = new HashMap<>();

            when(cell1.getPossibleValues()).thenReturn(new HashSet<>(Arrays.asList(1, 2)));
            when(cell2.getPossibleValues()).thenReturn(new HashSet<>(Arrays.asList(2, 3)));

            Map<Integer, Boolean> updatedOpinions = constraint.generateUpdatedInnerOpinions(cell1, cell2, 2, 3);
            assertEquals(opinions, updatedOpinions);
        }

        @Test
        void testGenerateUpdatedInnerOpinionsThrowsExceptionWhenCellsAreNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                constraint.generateUpdatedInnerOpinions(null, cell2, 2, 3);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                constraint.generateUpdatedInnerOpinions(cell1, null, 2, 3);
            });
        }
    }

    @Nested
    class WideReachRulesPossibleValuesTests {
        @Test
        void testWideReachRulesPossibleValuesReturnsFalse() {
            assertFalse(constraint.wideReachRulesPossibleValues(cell1, 1));
        }
    }

    @Nested
    class UpdatePossibleValuesManagerTests {
        @Test
        void testUpdatePossibleValuesManagerForbidsValueWhenOpinionIsTrue() {
            assertEquals(1, constraint.updatePossibleValuesManager(cell1, 1, true, false));
            verify(pvm).forbidCellValue(cell1, 1, false);
        }

        @Test
        void testUpdatePossibleValuesManagerAllowsValueWhenOpinionIsFalse() {
            assertEquals(0, constraint.updatePossibleValuesManager(cell1, 1, false, false));
            verify(pvm).allowCellValue(cell1, 1, false);
        }

        @Test
        void testUpdatePossibleValuesManagerReturnsNegativeOneWhenPvmIsNull() {
            // Use reflection to set pvm to null for testing
            try {
                var pvmField = Constraint.class.getDeclaredField("pvm");
                pvmField.setAccessible(true);
                pvmField.set(constraint, null);
            } catch (Exception e) {
                fail("Failed to set pvm to null using reflection", e);
            }

            assertEquals(-1, constraint.updatePossibleValuesManager(cell1, 1, true, false));
        }
    }

    @Nested
    class ResetPropTests {

        @Test
        void testResetPropPrintsWarningWhenPvmIsNull() {
            // Use reflection to set pvm to null for testing
            try {
                var pvmField = Constraint.class.getDeclaredField("pvm");
                pvmField.setAccessible(true);
                pvmField.set(constraint, null);
            } catch (Exception e) {
                fail("Failed to set pvm to null using reflection", e);
            }

            constraint.resetProp();
            // You can use a logging framework or System.out to capture the warning message
        }
    }

    @Nested
    class GetLastOpinionsTests {
        @Test
        void testGetLastOpinionsReturnsOpinionsForCell() {
            Map<Integer, Boolean> opinions = new HashMap<>();
            opinions.put(1, false);
            opinions.put(2, true);

            // Add opinions to the lastOpinions map
            constraint.lastOpinions.put(cell1, opinions);

            assertEquals(opinions, constraint.getLastOpinions(cell1));
        }

        @Test
        void testGetLastOpinionsReturnsEmptyMapWhenCellHasNoOpinions() {
            assertEquals(Collections.emptyMap(), constraint.getLastOpinions(cell1));
        }
    }
}