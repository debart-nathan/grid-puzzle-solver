package ascrassin.grid_puzzle.kernel;

import java.lang.ref.WeakReference;
import ascrassin.grid_puzzle.constraint.Constraint;
import java.util.ArrayList;
import java.util.List;

public class Cell {

    protected Integer value;
    protected boolean isSolved;
    protected boolean isDefault;
    protected List<Integer> possibleValues;
    protected final List<WeakReference<Constraint>> weakLinkedConstraints;

    public Cell(Integer minValue, Integer maxValue, Integer value) {
        this.value = value;
        this.isSolved = value != null;
        this.isDefault = value != null;
        this.possibleValues = new ArrayList<>();
        this.weakLinkedConstraints = new ArrayList<>();

        if (!isSolved) {
            for (Integer i = minValue; i <= maxValue; i++) {
                possibleValues.add(i);
            }
        }
    }

    public Integer getValue() {
        return this.value;
    }

    public boolean setValue(Integer value) {
        if (!isDefault) {
            if (possibleValues.contains(value) || value == null) {
                Integer oldValue= this.value;
                this.value = value;
                this.isSolved = value != null;
                for (WeakReference<Constraint> constraintRef : this.weakLinkedConstraints) {
                    Constraint constraint = constraintRef.get();
                    if (constraint != null) {
                        constraint.propagateCell(this, oldValue);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public List<Integer> getPossibleValues() {
        return possibleValues;
    }

    public boolean removePossibleValue(Integer value) {
        return possibleValues.remove(value);
    }

    public void addLinkedConstraint(Constraint constraint) {
        weakLinkedConstraints.add(new WeakReference<>(constraint));
    }

    public List<Constraint> getLinkedConstraints() {
        List<Constraint> liveConstraints = new ArrayList<>();
        for (WeakReference<Constraint> ref : weakLinkedConstraints) {
            Constraint constraint = ref.get();
            if (constraint != null) {
                liveConstraints.add(constraint);
            }
        }
        return liveConstraints;
    }
}
