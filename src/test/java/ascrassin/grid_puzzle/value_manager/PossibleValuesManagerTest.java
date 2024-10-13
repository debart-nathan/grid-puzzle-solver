package ascrassin.grid_puzzle.value_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ascrassin.grid_puzzle.kernel.Cell;
import java.util.*;

public class PossibleValuesManagerTest {

    @Mock
    private Cell cellMock;

    private PossibleValuesManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cellMock.getPossibleValues()).thenReturn(new ArrayList<>(Arrays.asList(1, 2, 3)));
        manager = new PossibleValuesManager();
        addCellToManager();
    }

    private void addCellToManager() {
        manager.linkConstraint(cellMock);
    }

    @Nested
    public class InitialStateTests {

        @Test
        void testInitialConstraintCount() {
            assertEquals(1, manager.getConstraintCounts().orElseThrow().get(cellMock));
        }

        @Test
        void testInitialAllowedValues() {
            Set<Integer> allowedValues = manager.getAllowedValues().orElseThrow().get(cellMock);
            assertTrue(allowedValues.isEmpty());
        }

        @Test
        void testInitialValueCounts() {
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertTrue(valueCounts.isEmpty());
        }
    }

    @Nested
    public class AllowCellValueTests {

        @Test
        void testAllowCellValue_AddsValue() {
            manager.allowCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(1, valueCounts.getOrDefault(1, 0));
        }

        @Test
        void testAllowCellValue_UpdatesAllowedValues() {
            manager.allowCellValue(cellMock, 1);
            Set<Integer> allowedValues = manager.getAllowedValues().orElseThrow().get(cellMock);
            assertTrue(allowedValues.contains(1));
        }

        @Test
        void testAllowCellValue_MultipleTimes() {
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(2, valueCounts.getOrDefault(1, 0));
        }
    }

    @Nested
    public class ForbidCellValueTests {

        @Test
        void testForbidCellValue_Initial() {
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(-1, valueCounts.getOrDefault(1, 0));
        }

        @Test
        void testForbidCellValue_AllowthenForbid() {
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(0, valueCounts.getOrDefault(1, 0));
        }

        @Test
        void testForbidCellValue_AllowthenForbidOther() {
            manager.allowCellValue(cellMock, 2);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(-1, valueCounts.getOrDefault(1, 0));
            assertEquals(1, valueCounts.getOrDefault(2, 0));
        }

        @Test
        void testForbidCellValue_UpdatesAllowedValues() {
            manager.forbidCellValue(cellMock, 1);
            Set<Integer> allowedValues = manager.getAllowedValues().orElseThrow().get(cellMock);
            assertFalse(allowedValues.contains(1));
        }

        @Test
        void testForbidCellValue_MultipleTimes() {
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(-1, valueCounts.getOrDefault(1, 0));
        }
    }

    @Nested
    public class GetValidValuesTests {

        @Test
        void testGetValidValues_NoOperations() {
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertEquals(new HashSet<>(), validValues);
        }

        @Test
        void testGetValidValues_AllowSingleValue() {
            manager.allowCellValue(cellMock, 1);
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertTrue(validValues.contains(1));
            assertEquals(1, validValues.size());
        }

        @Test
        void testGetValidValues_AllowMultiplesValue() {
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertTrue(validValues.contains(1));
            assertTrue(validValues.contains(2));
            assertFalse(validValues.contains(3));
            assertEquals(2, validValues.size());
        }

        @Test
        void testGetValidValues_ForbidSingleValue() {
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
            manager.forbidCellValue(cellMock, 1);
            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertEquals(new HashSet<>(Arrays.asList(2)), validValues);
        }
    }

    @Nested
    public class CanSetValueTests {

        @Test
        void testCanSetValue_AllowedValue() {
            manager.allowCellValue(cellMock, 1);
            assertTrue(manager.canSetValue(cellMock, 1));
        }

        @Test
        void testCanSetValue_ForbiddenValue() {
            manager.forbidCellValue(cellMock, 2);
            assertFalse(manager.canSetValue(cellMock, 2));
        }

        @Test
        void testCanSetValue_AllowedThenForbidden() {
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            assertFalse(manager.canSetValue(cellMock, 1));
        }

        @Test
        void testCanSetValue_ForbiddenThenAllowed() {
            var oldResult = manager.canSetValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 1);
            assertEquals(oldResult, manager.canSetValue(cellMock, 1));
        }
    }

    @Test
    void testResetCell() {
        manager.allowCellValue(cellMock, 1);
        manager.forbidCellValue(cellMock, 2);
        manager.resetCell(cellMock);

        // Check if counts are reset
        assertEquals(0, manager.getConstraintCounts().orElseThrow().getOrDefault(cellMock, 0));
        assertEquals(0, manager.getValueCounts().orElseThrow().getOrDefault(cellMock, new HashMap<>()).size());

        // Check if allowed values are reset
        Set<Integer> allowedValues = manager.getAllowedValues().orElseThrow().getOrDefault(cellMock, new HashSet<>());
        assertTrue(allowedValues.isEmpty());
    }

    @Nested
    public class EdgeCaseTests {

        @Test
        void testManyAllowAndForbidOperations() {
            for (int i = 0; i < 10; i++) {
                manager.allowCellValue(cellMock, 1);
                manager.forbidCellValue(cellMock, 1);
            }
            Map<Integer, Integer> valueCounts = manager.getValueCounts().orElseThrow().get(cellMock);
            assertEquals(0, valueCounts.getOrDefault(1, 0));
        }

        @Test
        void testConsecutiveResetOperations() {
            manager.allowCellValue(cellMock, 1);
            manager.resetCell(cellMock);
            manager.resetCell(cellMock);
            Set<Integer> allowedValues = manager.getAllowedValues().orElseThrow().get(cellMock);
            assertTrue(allowedValues.isEmpty());
        }
    }

    @Nested
    public class InvalidValueTests {

        @Test
        void testAllowCellValue_OutsidePossibleValues() {
            assertThrows(IllegalArgumentException.class, () -> manager.allowCellValue(cellMock, 4));
        }

        @Test
        void testGetValidValues_OutsidePossibleValues() {
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
            manager.forbidCellValue(cellMock, 1);

            Set<Integer> validValues = manager.getValidValues(cellMock);
            assertTrue(validValues.contains(2));
            assertFalse(validValues.contains(4)); // Value outside possible values
        }

        @Test
        void testCanSetValue_OutsidePossibleValues() {
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);

            assertFalse(manager.canSetValue(cellMock, 4)); // Value outside possible values
        }
    }
    @Nested
    public class GetSolvableCell{
        @Test
        void testGetSolvableCell_ReturnsNull() {
            // Arrange
            manager.allowCellValue(cellMock, 1);
            manager.allowCellValue(cellMock, 2);
    
            // Act
            Map.Entry<Cell, Integer> result = manager.getSolvableCell();
    
            // Assert
            assertNull(result);
        }
    
        @Test
        void testGetSolvableCell_ReturnsCellWithOneValidValue() {
            // Arrange
            manager.allowCellValue(cellMock, 1);
            manager.forbidCellValue(cellMock, 2);
    
            // Act
            Map.Entry<Cell, Integer> result = manager.getSolvableCell();
    
            // Assert
            assertNotNull(result);
            assertEquals(cellMock, result.getKey());
            assertEquals(1, result.getValue());
        }

    }
}