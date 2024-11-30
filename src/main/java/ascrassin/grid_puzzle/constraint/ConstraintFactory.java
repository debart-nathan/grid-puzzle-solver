package ascrassin.grid_puzzle.constraint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.List;
import ascrassin.grid_puzzle.kernel.*;
import ascrassin.grid_puzzle.value_manager.*;

public class ConstraintFactory {
    protected ConstraintFactory() {}

    public static <T extends Constraint> T createInstance(Class<T> constraintClass, List<Cell> gridSubset,
            PossibleValuesManager pvm) {
        try {
            Constructor<T> constructor = constraintClass.getDeclaredConstructor(List.class, PossibleValuesManager.class);
            T c = constructor.newInstance(gridSubset, pvm);
            c.resetProp();
            return c;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
           System.err.println("Unexpected error creating Constraint:"+ e.getMessage());
        }
        return null;
    }
}