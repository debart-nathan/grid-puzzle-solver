package ascrassin.grid_puzzle.constraint;

import ascrassin.grid_puzzle.kernel.Cell;
import ascrassin.grid_puzzle.value_manager.PossibleValuesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UniqueValueConstraintTest {

    @Mock
    protected PossibleValuesManager pvm;

    @Mock
    protected Cell cell1;
    @Mock
    protected Cell cell2;
    @Mock
    protected Cell cell3;

    @Mock
    protected Cell cellOutOfSubset;

    protected IConstraint uvc;

    List<Cell> gridSubset;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // Mock setup
        when(pvm.getValidValues(any())).thenReturn(new HashSet<>());

        // Create UniqueValueConstraint using the factory
        gridSubset = new ArrayList<Cell>();
        gridSubset.add(cell1);
        gridSubset.add(cell2);
        gridSubset.add(cell3);

        Set<Integer> possibleValues1 = new HashSet<>(Arrays.asList(1, 2, 4));
        Set<Integer> possibleValues2 = new HashSet<>(Arrays.asList(2, 3, 4));
        Set<Integer> possibleValues3 = new HashSet<>(Arrays.asList(2, 3, 4));

        when(cell1.getPossibleValues()).thenReturn(possibleValues1);
        when(cell2.getPossibleValues()).thenReturn(possibleValues2);
        when(cell3.getPossibleValues()).thenReturn(possibleValues3);

        Set<Integer> validValues1 = new HashSet<>();
        validValues1.add(1);
        validValues1.add(2);
        validValues1.add(4);

        Set<Integer> validValues2 = new HashSet<>();
        validValues2.add(2);
        validValues2.add(3);
        validValues2.add(4);

        Set<Integer> validValues3 = new HashSet<>();
        validValues3.add(2);
        validValues3.add(3);
        validValues3.add(4);

        when(pvm.getValidValues(cell1)).thenReturn(validValues1);
        when(pvm.getValidValues(cell2)).thenReturn(validValues2);
        when(pvm.getValidValues(cell3)).thenReturn(validValues3);

        uvc = new UniqueValueConstraint( gridSubset, pvm);

        when(pvm.getCellsForConstraint(uvc)).thenReturn(gridSubset);
    }

    @Nested
    class IsRuleBrokenTests {

        @Test
        void testNoDuplicatesReturnsFalse() {
            // Arrange
            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(null);
            when(cell3.getValue()).thenReturn(3);

            // Act & Assert
            assertFalse(uvc.isRuleBroken());
        }

        @Test
        void testDuplicateValuesReturnsTrue() {
            // Arrange
            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(1); // Duplicate value
            when(cell3.getValue()).thenReturn(null);


            // Act & Assert
            assertTrue(uvc.isRuleBroken());
        }
    }

    @Nested
    class GetSolvableCellTests {

        @Test
        void testValidValuesSuperiorCellCountReturnNull() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(2);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(2);
            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNull(result);
        }

        void testValidValuesInferiorCellCountReturnNull() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);
            validValues1.add(4);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(2);
            validValues2.add(3);
            validValues2.add(4);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(2);
            validValues3.add(3);
            validValues3.add(4);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNull(result);
        }

        @Test
        void testMultipleValidValuesNoUniqueReturnsNull() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(2);
            validValues2.add(3);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(1);
            validValues3.add(3);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNull(result);
        }

        @Test
        void testMultipleUniqueSharedValidValuesReturnNull() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(3);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(3);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);

            when(cell1.isSolved()).thenReturn(false);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNull(result);
        }

        @Test
        void testMultipleValidValuesButSolvedreturnNull() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(2);
            validValues2.add(3);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(2);
            validValues3.add(3);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            when(cell1.isSolved()).thenReturn(true);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNull(result);
        }

        @Test
        void testMultipleValidValuesReturnCell() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(2);
            validValues2.add(3);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(2);
            validValues3.add(3);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertNotNull(result);
            assertEquals(cell1, result.getKey());
            assertEquals(1, result.getValue());
        }

        @Test
        void testMultipleValidValuesMultiplesEligibleReturnCell() {
            // Arrange
            Set<Integer> validValues1 = new HashSet<>();
            validValues1.add(1);
            validValues1.add(2);

            Set<Integer> validValues2 = new HashSet<>();
            validValues2.add(3);

            Set<Integer> validValues3 = new HashSet<>();
            validValues3.add(2);
            validValues3.add(3);

            when(pvm.getValidValues(cell1)).thenReturn(validValues1);
            when(pvm.getValidValues(cell2)).thenReturn(validValues2);
            when(pvm.getValidValues(cell3)).thenReturn(validValues3);
            uvc.resetProp();

            // Act
            Map.Entry<Cell, Integer> result = uvc.getSolvableCell();

            // Assert
            assertTrue(result != null &&
                    (result.getKey().equals(cell1) && result.getValue() == 1 ||
                            result.getKey().equals(cell3) && result.getValue() == 2));
        }
    }

    @Nested
    class GenerateOpinionsTests {

        @Test
        void testInitialCallShouldReturnFalseMap() {

            Map<Integer, Boolean> expectedOpinions = new HashMap<>();
            when(cell1.getValue()).thenReturn(null);
            when(cell2.getValue()).thenReturn(null);
            when(cell3.getValue()).thenReturn(3);
            expectedOpinions.put(1, false);
            expectedOpinions.put(2, false);
            expectedOpinions.put(4, false);

            Map<Integer, Boolean> actualOpinions = uvc.generateOpinions(cell1);

            assertEquals(expectedOpinions, actualOpinions);
        }

        @Test
        void testGenerateOpinions() {
            // Arrange
            Map<Integer, Boolean> expectedOpinions = new HashMap<>();
            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(null);
            when(cell3.getValue()).thenReturn(null);
            expectedOpinions.put(1, true);
            expectedOpinions.put(2, false);
            expectedOpinions.put(4, false);

            // Act
            Map<Integer, Boolean> actualOpinions = uvc.generateOpinions(cell1);

            // Assert
            assertEquals(expectedOpinions, actualOpinions);
        }

        @Test
        void testGenerateOpinionsMultiplesCellValues() {
            // Arrange
            Map<Integer, Boolean> expectedOpinions = new HashMap<>();

            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(2);
            when(cell3.getValue()).thenReturn(4);
            expectedOpinions.put(1, true);
            expectedOpinions.put(2, true);
            expectedOpinions.put(4, true);

            // Act
            Map<Integer, Boolean> actualOpinions = uvc.generateOpinions(cell1);

            // Assert
            assertEquals(expectedOpinions, actualOpinions);
        }
    }

    @Nested
    class GenerateUpdatedOpinionsTests {

        @Test
        void testInitialCallShouldReturnFalseMap() {
            Map<Integer, Boolean> expectedOpinions = new HashMap<>();
            when(cell1.getValue()).thenReturn(null);
            when(cell2.getValue()).thenReturn(null);
            when(cell3.getValue()).thenReturn(3);
            expectedOpinions.put(1, false);
            expectedOpinions.put(2, false);
            expectedOpinions.put(4, false);
            uvc.resetProp();

            Map<Integer, Boolean> actualOpinions = uvc.generateUpdatedOpinions(cell1, cell1, null, null);

            assertEquals(expectedOpinions, actualOpinions);
        }

        @Test
        void testGenerateUpdatedOpinions() {
            // Arrange
            Map<Integer, Boolean> expectedOpinions = new HashMap<>();
            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(null);
            when(cell3.getValue()).thenReturn(null);
            expectedOpinions.put(1, true);
            expectedOpinions.put(2, false);
            expectedOpinions.put(4, false);
            uvc.resetProp();

            // Act
            Map<Integer, Boolean> actualOpinions = uvc.generateUpdatedOpinions(cell1, cell1, null, null);

            // Assert
            assertEquals(expectedOpinions, actualOpinions);
        }

        @Test
        void testGenerateUpdatedOpinionsMultiplesCellValues() {
            // Arrange
            Map<Integer, Boolean> expectedOpinions = new HashMap<>();

            when(cell1.getValue()).thenReturn(1);
            when(cell2.getValue()).thenReturn(2);
            when(cell3.getValue()).thenReturn(4);
            expectedOpinions.put(1, true);
            expectedOpinions.put(2, true);
            expectedOpinions.put(4, true);
            uvc.resetProp();

            // Act
            Map<Integer, Boolean> actualOpinions = uvc.generateUpdatedOpinions(cell1, cell1, null, null);

            // Assert
            assertEquals(expectedOpinions, actualOpinions);
        }
    }

    @Nested
    class PropagateCellTests {

        @Test
        void testNotInSubsetPropagation() {
            // Arrange

            // Act
            boolean result = ((UniqueValueConstraint) uvc).innerRulesPropagateCell(cellOutOfSubset, 2);

            // Assert
            assertFalse(result);
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell3), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell3), anyInt());

        }

        @Test
        void testNoChangePropagation() {
            // Arrange
            when(cell1.getValue()).thenReturn(2);
            uvc.resetProp();
            when(cell1.getValue()).thenReturn(2);

            // Act
            boolean result = ((UniqueValueConstraint) uvc).innerRulesPropagateCell(cell1, 2);

            // Assert
            assertFalse(result);
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell3), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell3), anyInt());

        }

        @Test
        void testPropagationWhenChange() {
            // Arrange
            when(cell1.getValue()).thenReturn(2);
            uvc.resetProp();
            when(cell1.getValue()).thenReturn(4);

            // Act
            reset(pvm);
            when(pvm.getCellsForConstraint(uvc)).thenReturn(gridSubset);
            boolean result = ((UniqueValueConstraint) uvc).innerRulesPropagateCell(cell1, 2);

            // Assert
            assertTrue(result);
            verify(pvm, atLeast(1)).forbidCellValue(cell1, 4);
            verify(pvm, atLeast(1)).allowCellValue(cell1, 2);
            verify(pvm, atLeast(1)).forbidCellValue(cell2, 4);
            verify(pvm, atLeast(1)).allowCellValue(cell2, 2);
            verify(pvm, atLeast(1)).forbidCellValue(cell3,4);
            verify(pvm, atLeast(1)).allowCellValue(cell3, 2);
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell1), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell2), anyInt());
            verify(pvm, atLeast(0)).forbidCellValue(eq(cell3), anyInt());
            verify(pvm, atLeast(0)).allowCellValue(eq(cell3), anyInt());
        }
    }

}