package ascrassin.grid_puzzle.kernel;

import java.lang.ref.WeakReference;
import ascrassin.grid_puzzle.constraint.Constraint;
import ascrassin.grid_puzzle.constraint.IConstraint;

import java.util.HashSet;
import java.util.Set;

public class Cell {

    protected Integer value;
    protected boolean isSolved;
    protected boolean isDefault;
    protected Set<Integer> possibleValues;
    protected final Set<WeakReference<Constraint>> weakLinkedConstraints;

    public Cell(Integer minValue, Integer maxValue, Integer value) {
        this.value = value;
        this.isSolved = value != null;
        this.isDefault = value != null;
        this.possibleValues = new HashSet<>();
        this.weakLinkedConstraints = new HashSet<>();

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
        if (!isDefault && possibleValues.contains(value) || value == null) {
            Integer oldValue = this.value;
            this.value = value;
            this.isSolved = value != null;
            for (WeakReference<Constraint> constraintRef : this.weakLinkedConstraints) {
                IConstraint constraint = constraintRef.get();
                if (constraint != null) {
                    constraint.propagateCell(this, oldValue);
                }
            }
            return true;

        }
        return false;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public Set<Integer> getPossibleValues() {
        return possibleValues;
    }

    public boolean removePossibleValue(Integer value) {
        return possibleValues.remove(value);
    }

    public void addLinkedConstraint(Constraint constraint) {
        weakLinkedConstraints.add(new WeakReference<>(constraint));
    }

    public Set<Constraint> getLinkedConstraints() {
        Set<Constraint> liveConstraints = new HashSet<>();
        for (WeakReference<Constraint> ref : weakLinkedConstraints) {
            Constraint constraint = ref.get();
            if (constraint != null) {
                liveConstraints.add(constraint);
            }
        }
        return liveConstraints;
    }
}
