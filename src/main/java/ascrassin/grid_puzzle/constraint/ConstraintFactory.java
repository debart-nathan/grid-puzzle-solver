package ascrassin.grid_puzzle.constraint;

import java.lang.reflect.InvocationTargetException;

public class ConstraintFactory {
    private ConstraintFactory() {
    }

    public static Constraint createInstance(Class<? extends Constraint> constraintClass)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constraint instance = constraintClass.getDeclaredConstructor().newInstance();
        instance.resetProp();
        return instance;
    }
}