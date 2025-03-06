package ascrassin.grid_puzzle.value_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class PossibleValuesManagerTest {

    @Mock
    protected Cell cellMock;

    @Mock
    protected Cell cellMock2;

    protected PossibleValuesManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cellMock.getPossibleValues()).thenReturn(new HashSet<>(Arrays.asList(1, 2, 3)));
        when(cellMock2.getPossibleValues()).thenReturn(new HashSet<>(Arrays.asList(2, 3, 4)));
        manager = new PossibleValuesManager();
    }

    @Nested
    class InitialStateTests {

        @Test
        void testInitialConstraintCount() {
            assertEquals(new ConcurrentHashMap<>(), manager.getConstraintCounts());
        }

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
            manager.initializeCell(cellMock);

            manager.updateAllowedValues(cellMock);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testWithOneConstraint() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.linkConstraint(cellMock);

            manager.updateAllowedValues(cellMock);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(1), allowedValues);
        }

        @Test
        void testWithMultipleConstraints() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);

            manager.updateAllowedValues(cellMock);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(1), allowedValues);
        }

        @Test
        void testEdgeCaseAfterReset() {
            manager.linkConstraint(cellMock);
            manager.resetCell(cellMock);

            manager.updateAllowedValues(cellMock);

            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
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
            manager.initializeCell(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertTrue(allowedValues.containsAll(Arrays.asList(1, 2, 3)));
        }

        @Test
        void testInitializeCellMultipleTimes() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertTrue(allowedValues.containsAll(Arrays.asList(1, 2, 3)));
        }

        @Test
        void testInitializeCellDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            assertEquals(0, manager.getConstraintCount(cellMock));
            assertEquals(0, manager.getConstraintCount(cellMock2));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Map<Integer, Integer> initialCounts2 = new HashMap<>();
            for (Integer value : cellMock2.getPossibleValues()) {
                initialCounts2.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock2),initialCounts2);
            Map<Cell, Set<Integer>> allowedValues = manager.getAllowedValues();
            Set<Integer> cellMockAllowedValues = allowedValues.get(cellMock);
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
            manager.linkConstraint(cellMock);
            assertEquals(1, manager.getConstraintCount(cellMock));
        }

        @Test
        void testLinkMultipleConstraints() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock);
            assertEquals(2, manager.getConstraintCount(cellMock));
        }

        @Test
        void testLinkConstraintDifferentCells() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock2);
            assertEquals(1, manager.getConstraintCount(cellMock));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }

        @Test
        void testLinkConstraintNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.linkConstraint(null));
        }
    }

    // Nested class for unlinkConstraint method
    @Nested
    class UnlinkConstraintTests {

        @Test
        void testUnlinkConstraint() {
            manager.linkConstraint(cellMock);
            manager.unlinkConstraint(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
        }

        @Test
        void testUnlinkMultipleConstraints() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock);
            manager.unlinkConstraint(cellMock);
            assertEquals(1, manager.getConstraintCount(cellMock));
        }

        @Test
        void testUnlinkConstraintZeroTimes() {
            assertEquals(0, manager.getConstraintCount(cellMock));
            assertThrows(IllegalStateException.class, () -> manager.unlinkConstraint(cellMock));
            assertEquals(0, manager.getConstraintCount(cellMock));
        }

        @Test
        void testUnlinkConstraintDifferentCells() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock2);
            manager.unlinkConstraint(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }

        @Test
        void testUnlinkConstraintNullCell() {
            assertThrows(NullPointerException.class,
                    () -> manager.unlinkConstraint(null));
        }
    }

    // Nested class for getConstraintCount method
    @Nested
    class GetConstraintCountTests {

        @Test
        void testGetConstraintCount() {
            manager.linkConstraint(cellMock);
            assertEquals(1, manager.getConstraintCount(cellMock));
        }

        @Test
        void testGetConstraintCountZero() {
            assertEquals(0, manager.getConstraintCount(cellMock));
        }

        @Test
        void testGetConstraintCountDifferentCells() {
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock2);
            assertEquals(1, manager.getConstraintCount(cellMock));
            assertEquals(1, manager.getConstraintCount(cellMock2));
        }
    }

    // Nested class for allowCellValue method
    @Nested
    class AllowCellValueTests {

        @Test
        void testAllowCellValue() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock);
            assertEquals(1, valueCounts.get(1).intValue());
        }

        @Test
        void testAllowCellValueMultipleTimes() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock);
            assertEquals(2, valueCounts.get(1).intValue());
        }

        @Test
        void testAllowCellValueDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock2, 2);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock);
            Map<Integer, Integer> cellMock2Counts = manager.getValueCounts(cellMock2);
            assertEquals(1, cellMockCounts.get(1).intValue());
            assertEquals(1, cellMock2Counts.get(2).intValue());
        }
        

        @Test
        void testAllowCellValueNullCell() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(null, 1));
        }

        @Test
        void testAllowCellValueNotManagedCell() {
            Cell unmanagedCell = mock(Cell.class);
            when(unmanagedCell.getPossibleValues()).thenReturn(Set.of(1, 2, 3));

            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(unmanagedCell, 1));
        }


        @Test
        void testAllowCellValueNotAllowedValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.allowCellValue(cellMock, 4));
        }
    }

    // Nested class for forbidCellValue method
    @Nested
    class ForbidCellValueTests {

        @Test
        void testForbidCellValue() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock);
            assertEquals(0, valueCounts.get(1).intValue());
        }

        @Test
        void testForbidCellValueMultipleTimes() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock);
            assertEquals(-1, valueCounts.get(1).intValue());
        }

        @Test
        void testForbidCellValueDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock2, 2);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock);
            Map<Integer, Integer> cellMock2Counts = manager.getValueCounts(cellMock2);
            assertEquals(0, cellMockCounts.get(1).intValue());
            assertEquals(1, cellMock2Counts.get(2).intValue());
        }

                @Test
        void testForbidCellValueNullCell() {
            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(null, 1));
        }

        @Test
        void testForbidCellValueNotManagedCell() {
            Cell unmanagedCell = mock(Cell.class);
            when(unmanagedCell.getPossibleValues()).thenReturn(Set.of(1, 2, 3));

            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(unmanagedCell, 1));
        }

        @Test
        void testForbidCellValueNotAllowedValue() {
            manager.initializeCell(cellMock);
            assertThrows(IllegalArgumentException.class,
                    () -> manager.forbidCellValue(cellMock, 4));
        }
    }

    // Nested class for getValueCounts method
    @Nested
    class GetValueCountsTests {

        @Test
        void testGetValueCounts() {
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts(cellMock);
            assertEquals(1, valueCounts.get(1).intValue());
        }


        @Test
        void testGetValueCountsDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock2, 2);
            Map<Integer, Integer> cellMockCounts = manager.getValueCounts(cellMock);
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
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 2);
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertEquals(2, validValues.size());
            assertTrue(validValues.contains(1));
            assertTrue(validValues.contains(3));
        }

        @Test
        void testGetValidValuesNoConstraints() {
            manager.initializeCell(cellMock);
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertEquals(new HashSet<>(cellMock.getPossibleValues()), validValues);
        }

        @Test
        void testGetValidValuesDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 2);
            manager.allowCellValue(cellMock2, 2);
            manager.forbidCellValue(cellMock2, 4);
            Set<Integer> cellMockValidValues = manager.getValidValues(cellMock);
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
            manager.initializeCell(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 2);
            assertTrue(manager.canSetValue(cellMock, 1));
            assertFalse(manager.canSetValue(cellMock, 2));
        }

        @Test
        void testCanSetValueNoConstraints() {
            manager.initializeCell(cellMock);
            assertTrue(manager.canSetValue(cellMock, 1));
        }

        @Test
        void testCanSetValueDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.allowCellValue(cellMock, 3);
            manager.forbidCellValue(cellMock, 2);
            manager.allowCellValue(cellMock2, 2);
            manager.forbidCellValue(cellMock2, 3);
            assertTrue(manager.canSetValue(cellMock, 3));
            assertFalse(manager.canSetValue(cellMock, 2));
            assertTrue(manager.canSetValue(cellMock2, 2));
            assertFalse(manager.canSetValue(cellMock2, 3));
        }
    }

    // Nested class for getSolvableCell method
    @Nested
    class GetSolvableCellTests {

        @Test
        void testGetSolvableCell() {
            manager.initializeCell(cellMock);
            manager.linkConstraint(cellMock);
            manager.forbidCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
            Map.Entry<Cell, Integer> solvableCell = manager.getSolvableCell();
            assertNotNull(solvableCell);
            assertEquals(cellMock, solvableCell.getKey());
            assertEquals(2, solvableCell.getValue().intValue());
        }

        @Test
        void testGetSolvableCellNoSolution() {
            manager.initializeCell(cellMock);
            manager.linkConstraint(cellMock);
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
            assertNull(manager.getSolvableCell());
        }

        @Test
        void testGetSolvableCellMultipleCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.linkConstraint(cellMock);
            manager.forbidCellValue(cellMock, 3);
            manager.allowCellValue(cellMock, 2);
            manager.linkConstraint(cellMock2);
            manager.forbidCellValue(cellMock2, 2);
            manager.allowCellValue(cellMock2, 3);
            
            Map.Entry<Cell, Integer> solvableCell = manager.getSolvableCell();
            
            assertNotNull(solvableCell);
            
            // Check if the returned cell is either cellMock or cellMock2
            assertTrue(solvableCell.getKey().equals(cellMock) || solvableCell.getKey().equals(cellMock2));
            
            // Additional checks to ensure we have 2 for cellMock and 3 for cellMock2
            if (solvableCell.getKey().equals(cellMock)) {
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
            manager.initializeCell(cellMock);
            manager.linkConstraint(cellMock);
            manager.resetCell(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testResetCellMultipleTimes() {
            manager.initializeCell(cellMock);
            manager.linkConstraint(cellMock);
            manager.resetCell(cellMock);
            manager.resetCell(cellMock);
            assertEquals(0, manager.getConstraintCount(cellMock));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(1, 2, 3), allowedValues);
        }

        @Test
        void testResetCellDifferentCells() {
            manager.initializeCell(cellMock);
            manager.initializeCell(cellMock2);
            manager.linkConstraint(cellMock);
            manager.linkConstraint(cellMock2);
            manager.resetCell(cellMock2);
            assertEquals(1, manager.getConstraintCount(cellMock));
            Map<Integer, Integer> initialCounts = new HashMap<>();
            for (Integer value : cellMock.getPossibleValues()) {
                initialCounts.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock),initialCounts);
            Set<Integer> allowedValues = manager.getAllowedValues().get(cellMock);
            assertEquals(Set.of(), allowedValues);
            assertEquals(0, manager.getConstraintCount(cellMock2));
            Map<Integer, Integer> initialCounts2 = new HashMap<>();
            for (Integer value : cellMock2.getPossibleValues()) {
                initialCounts2.put(value, 0);
            }
            assertEquals(manager.getValueCounts(cellMock2),initialCounts2);
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
