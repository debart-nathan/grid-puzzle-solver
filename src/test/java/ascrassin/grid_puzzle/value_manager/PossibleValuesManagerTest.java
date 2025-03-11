package ascrassin.grid_puzzle.value_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ascrassin.grid_puzzle.constraint.TestableConstraint;
import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;

class PossibleValuesManagerTest {

    @Mock
    protected Cell cellMock1;

    @Mock
    protected Cell cellMock2;

    @Mock
    private TestableConstraint testableConstraintMock;

    @Mock
    private TestableConstraint testableConstraintMock2;

    protected PossibleValuesManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<Integer> possibleValues1 = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> possibleValues2 = new HashSet<>(Arrays.asList(2, 3, 4));

        when(cellMock1.getPossibleValues()).thenReturn(possibleValues1);
        when(cellMock2.getPossibleValues()).thenReturn(possibleValues2);

        when(testableConstraintMock.isRuleBroken()).thenReturn(false);
        when(testableConstraintMock.getSolvableCell()).thenReturn(null);
        when(testableConstraintMock.innerRulesPropagateCell(any(Cell.class), any(Integer.class))).thenReturn(false);
        when(testableConstraintMock.generateUpdatedInnerOpinions(any(Cell.class), any(Cell.class), any(Integer.class),
                any(Integer.class))).thenReturn(new HashMap<>());
        when(testableConstraintMock.generateInnerOpinions(any(Cell.class))).thenReturn(new HashMap<>());

        manager = new PossibleValuesManager();
    }

    @Nested
    class InitialStateTests {

        @Test
        void testInitialAllowedValues() {
            Map<Cell, Set<Integer>> allowedValues = manager.getAllowedValues();
            assertTrue(allowedValues.isEmpty());
        }

        @Test
        void testInitialValueCounts() {
            Map<Cell, Map<Integer, Integer>> valueCounts = manager.getValueCounts();
            assertTrue(valueCounts.isEmpty());
        }
    }

    // Nested class for updateAllowedValues method
    @Nested
    class UpdateAllowedValuesTests {

        @Test
        void testWithNoConstraints() {
            manager.initializeCell(cellMock1);

            manager.updateAllowedValues(cellMock1);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testWithOneConstraint() {
            manager.initializeCell(cellMock1);
            manager.forbidCellValue(cellMock1, 1, false);
            manager.linkConstraint(cellMock1,testableConstraintMock, null);

            manager.updateAllowedValues(cellMock1);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(2,3), allowedValues);
        }

        @Test
        void testWithMultipleConstraints() {
            manager.linkConstraint(cellMock1,testableConstraintMock, null);
            manager.linkConstraint(cellMock1,testableConstraintMock2, null);
            manager.forbidCellValue(cellMock1, 1, false);

            manager.updateAllowedValues(cellMock1);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(2,3), allowedValues);
        }

        @Test
        void testEdgeCaseAfterReset() {
            manager.linkConstraint(cellMock1,testableConstraintMock, null);
            manager.resetCell(cellMock1);

            manager.updateAllowedValues(cellMock1);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.updateAllowedValues(null));
        }
    }

    // Nested class for initializeCell method
    @Nested
    class InitializeCellTests {

        @Test
        void testInitializeCell() {
            manager.initializeCell(cellMock1);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertTrue(allowedValues.containsAll(Arrays.asList(1, 2, 3)));
        }

        @Test
        void testInitializeCellMultipleTimes() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock1);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertTrue(allowedValues.containsAll(Arrays.asList(1, 2, 3)));
        }

        @Test
        void testInitializeCellDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            assertEquals(0, manager.getConstraintCount(cellMock2));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Map<Integer, Integer> initialCounts2 = new HashMap<>();
            for (Integer value : cellMock2.getPossibleValues()) {
                initialCounts2.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock2), initialCounts2);
            Map<Cell, Set<Integer>> allowedValues = manager.getAllowedValues();
            Set<Integer> cellMockAllowedValues = allowedValues.get(cellMock1);
            Set<Integer> cellMock2AllowedValues = allowedValues.get(cellMock2);
            assertTrue(cellMockAllowedValues.containsAll(Arrays.asList(1, 2, 3)));
            assertTrue(cellMock2AllowedValues.containsAll(Arrays.asList(2, 3, 4)));
        }

        @Test
        void testInitializeCellNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.initializeCell(null));
        }
    }

    // Nested class for linkConstraint method
    @Nested
    class LinkConstraintTests {

        @Test
        void testLinkConstraint() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            assertEquals(1, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testLinkMultipleConstraints() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock1,testableConstraintMock2,null);
            assertEquals(2, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testLinkConstraintDifferentCells() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock2,testableConstraintMock2,null);
            assertEquals(1, manager.getConstraintCount(cellMock1));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }

        @Test
        void testLinkConstraintNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.linkConstraint(null,testableConstraintMock,null));
        }
    }

    // Nested class for unlinkConstraint method
    @Nested
    class UnlinkConstraintTests {

        @Test
        void testUnlinkConstraint() {
            manager.linkConstraint(cellMock1,testableConstraintMock, null);
            manager.unlinkConstraint(cellMock1,testableConstraintMock,null );
            assertEquals(0, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testUnlinkMultipleConstraints() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock1,testableConstraintMock2,null);
            manager.unlinkConstraint(cellMock1,testableConstraintMock,null);
            assertEquals(1, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testUnlinkConstraintZeroTimes() {
            assertEquals(0, manager.getConstraintCount(cellMock1));
            manager.unlinkConstraint(cellMock1,testableConstraintMock,null);
            assertEquals(0, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testUnlinkConstraintDifferentCells() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock2,testableConstraintMock,null);
            manager.unlinkConstraint(cellMock1,testableConstraintMock,null);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }

        @Test
        void testUnlinkConstraintNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.unlinkConstraint(null,testableConstraintMock,null));
        }
    }

    // Nested class for getConstraintCount method
    @Nested
    class GetConstraintCountTests {

        @Test
        void testGetConstraintCount() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            assertEquals(1, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testGetConstraintCountZero() {
            assertEquals(0, manager.getConstraintCount(cellMock1));
        }

        @Test
        void testGetConstraintCountDifferentCells() {
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock2,testableConstraintMock,null);
            assertEquals(1, manager.getConstraintCount(cellMock1));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }
    }

    // Nested class for allowCellValue method
    @Nested
    class AllowCellValueTests {

        @Test
        void testAllowCellValue() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock1);
            assertEquals(1, valueCounts.get(1).intValue());
        }

        @Test
        void testAllowCellValueMultipleTimes() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            manager.allowCellValue(cellMock1, 1, false);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock1);
            assertEquals(2, valueCounts.get(1).intValue());
        }

        @Test
        void testAllowCellValueDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock1, 1, false);
            manager.allowCellValue(cellMock2, 2, false);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock1);
            Map<Integer, Integer> cellMock2Counts = manager.getValueCounts(cellMock2);
            assertEquals(1, cellMockCounts.get(1).intValue());
            assertEquals(1, cellMock2Counts.get(2).intValue());
        }

        @Test
        void testAllowCellValueNullCell() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(null, 1, false));
        }

        @Test
        void testAllowCellValueNotManagedCell() {
            Cell unmanagedCell = mock(Cell.class);
            when(unmanagedCell.getPossibleValues()).thenReturn(Set.of(1, 2, 3));

            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(unmanagedCell, 1, false));
        }

        @Test
        void testAllowCellValueNotAllowedValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(cellMock1, 4, false));
        }
    }

    // Nested class for forbidCellValue method
    @Nested
    class ForbidCellValueTests {

        @Test
        void testForbidCellValue() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 1, false);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock1);
            assertEquals(0, valueCounts.get(1).intValue());
        }

        @Test
        void testForbidCellValueMultipleTimes() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 1, false);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock1);
            assertEquals(-1, valueCounts.get(1).intValue());
        }

        @Test
        void testForbidCellValueDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock1, 1, false);
            manager.allowCellValue(cellMock2, 2, false);
            manager.forbidCellValue(cellMock1, 1, false);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock1);
            Map<Integer, Integer> cellMock2Counts = manager.getValueCounts(cellMock2);
            assertEquals(0, cellMockCounts.get(1).intValue());
            assertEquals(1, cellMock2Counts.get(2).intValue());
        }

        @Test
        void testForbidCellValueNullCell() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(null, 1, false));
        }

        @Test
        void testForbidCellValueNotManagedCell() {
            Cell unmanagedCell = mock(Cell.class);
            when(unmanagedCell.getPossibleValues()).thenReturn(Set.of(1, 2, 3));

            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(unmanagedCell, 1, false));
        }

        @Test
        void testForbidCellValueNotAllowedValue() {
            manager.initializeCell(cellMock1);
            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(cellMock1, 4, false));
        }
    }

    // Nested class for getValueCounts method
    @Nested
    class GetValueCountsTests {

        @Test
        void testGetValueCounts() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock1);
            assertEquals(1, valueCounts.get(1).intValue());
        }

        @Test
        void testGetValueCountsDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock1, 1, false);
            manager.allowCellValue(cellMock2, 2, false);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock1);
            Map<Integer, Integer> cellMock2Counts = manager.getValueCounts(cellMock2);
            assertEquals(1, cellMockCounts.get(1).intValue());
            assertEquals(1, cellMock2Counts.get(2).intValue());
        }
    }

    // Nested class for getValidValues method
    @Nested
    class GetValidValuesTests {

        @Test
        void testGetValidValues() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 2, false);
            Set<Integer> validValues = manager.getValidValues(cellMock1);
            assertEquals(2, validValues.size());
            assertTrue(validValues.contains(1));
            assertTrue(validValues.contains(3));
        }

        @Test
        void testGetValidValuesNoConstraints() {
            manager.initializeCell(cellMock1);
            Set<Integer> validValues = manager.getValidValues(cellMock1);
            assertEquals(new HashSet<>(cellMock1.getPossibleValues()), validValues);
        }

        @Test
        void testGetValidValuesDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 2, false);
            manager.allowCellValue(cellMock2, 2, false);
            manager.forbidCellValue(cellMock2, 4, false);
            Set<Integer> cellMockValidValues = manager.getValidValues(cellMock1);
            Set<Integer> cellMock2ValidValues = manager.getValidValues(cellMock2);
            assertEquals(2, cellMockValidValues.size());
            assertTrue(cellMockValidValues.contains(1));
            assertTrue(cellMockValidValues.contains(3));

            assertEquals(2, cellMock2ValidValues.size());
            assertTrue(cellMock2ValidValues.contains(2));
            assertTrue(cellMockValidValues.contains(3));
        }
    }

    // Nested class for canSetValue method
    @Nested
    class CanSetValueTests {

        @Test
        void testCanSetValue() {
            manager.initializeCell(cellMock1);
            manager.allowCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 2, false);
            assertTrue(manager.canSetValue(cellMock1, 1));
            assertFalse(manager.canSetValue(cellMock1, 2));
        }

        @Test
        void testCanSetValueNoConstraints() {
            manager.initializeCell(cellMock1);
            assertTrue(manager.canSetValue(cellMock1, 1));
        }

        @Test
        void testCanSetValueDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock1, 3, false);
            manager.forbidCellValue(cellMock1, 2, false);
            manager.allowCellValue(cellMock2, 2, false);
            manager.forbidCellValue(cellMock2, 3, false);
            assertTrue(manager.canSetValue(cellMock1, 3));
            assertFalse(manager.canSetValue(cellMock1, 2));
            assertTrue(manager.canSetValue(cellMock2, 2));
            assertFalse(manager.canSetValue(cellMock2, 3));
        }
    }

    // Nested class for getSolvableCell method
    @Nested
    class GetSolvableCellTests {

        @Test
        void testGetSolvableCell() {
            manager.initializeCell(cellMock1);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.forbidCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 3, false);
            Map.Entry<Cell, Integer> solvableCell = manager.getSolvableCell();
            assertNotNull(solvableCell);
            assertEquals(cellMock1, solvableCell.getKey());
            assertEquals(2, solvableCell.getValue().intValue());
        }

        @Test
        void testGetSolvableCellNoSolution() {
            manager.initializeCell(cellMock1);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            assertNull(manager.getSolvableCell());
        }

        @Test
        void testGetSolvableCellMultipleCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.forbidCellValue(cellMock1, 1, false);
            manager.forbidCellValue(cellMock1, 3, false);
            manager.linkConstraint(cellMock2,testableConstraintMock,null);
            manager.forbidCellValue(cellMock2, 2, false);
            manager.forbidCellValue(cellMock2, 4, false);


            Map.Entry<Cell, Integer> solvableCell = manager.getSolvableCell();

            assertNotNull(solvableCell);

            // Check if the returned cell is either cellMock or cellMock2
            assertTrue(solvableCell.getKey().equals(cellMock1) || solvableCell.getKey().equals(cellMock2));

            // Additional checks to ensure we have 2 for cellMock and 3 for cellMock2
            if (solvableCell.getKey().equals(cellMock1)) {
                assertEquals(2, solvableCell.getValue().intValue());
            } else {
                assertEquals(3, solvableCell.getValue().intValue());
            }
        }

    }

    // Nested class for resetCell method
    @Nested
    class ResetCellTests {

        @Test
        void testResetCell() {
            manager.initializeCell(cellMock1);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.resetCell(cellMock1);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testResetCellMultipleTimes() {
            manager.initializeCell(cellMock1);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.resetCell(cellMock1);
            manager.resetCell(cellMock1);
            assertEquals(0, manager.getConstraintCount(cellMock1));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testResetCellDifferentCells() {
            manager.initializeCell(cellMock1);
            manager.initializeCell(cellMock2);
            manager.linkConstraint(cellMock1,testableConstraintMock,null);
            manager.linkConstraint(cellMock2,testableConstraintMock,null);
            manager.resetCell(cellMock2);
            assertEquals(1, manager.getConstraintCount(cellMock1));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock1.getPossibleValues()) {
                initialCounts.put(value, 1);
            }
            assertEquals(manager.getValueCounts(cellMock1), initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock1);
            assertEquals(Set.of(1,2,3), allowedValues);
            assertEquals(0, manager.getConstraintCount(cellMock2));
            Map<Integer, Integer> initialCounts2 = new HashMap<>();
            for (Integer value : cellMock2.getPossibleValues()) {
                initialCounts2.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock2), initialCounts2);
            Set<Integer> allowedValues2 = manager.getAllowedValues().get(cellMock2);
            assertEquals(Set.of(2, 3, 4), allowedValues2);
        }

        @Test
        void testResetCellNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.resetCell(null));
        }
    }

}
