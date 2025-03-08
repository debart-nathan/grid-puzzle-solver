package ascrassin.grid_puzzle.kernel;

import java.lang.ref.WeakReference;
import ascrassin.grid_puzzle.value_manager.IValueManager;
import java.util.HashSet;
import java.util.Set;

public class Cell {
    protected Integer value;
    protected boolean isSolved;
    protected boolean isDefault;
    protected Set<Integer> possibleValues;
    protected final Set<WeakReference<IValueManager>> weakLinkedValueManagers;

    public Cell(Integer minValue, Integer maxValue, Integer value) {
        this.value = value;
        this.isSolved = value != null;
        this.isDefault = value != null;
        this.possibleValues = new HashSet<>();
        this.weakLinkedValueManagers = new HashSet<>();

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
            
            // Notify all linked Value Managers about the change
            for (WeakReference<IValueManager> managerRef : weakLinkedValueManagers) {
                IValueManager manager = managerRef.get();
                if (manager != null) {
                    manager.propagateCellValueChange(this, oldValue, value);
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

    public void addLinkedValueManager(IValueManager manager) {
        boolean found = false;
        for (WeakReference<IValueManager> existingRef : weakLinkedValueManagers) {
            if (existingRef.get() == manager) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            weakLinkedValueManagers.add(new WeakReference<>(manager));
        }
    }

    public Set<IValueManager> getLinkedValueManagers() {
        Set<IValueManager> liveManagers = new HashSet<>();
        for (WeakReference<IValueManager> ref : weakLinkedValueManagers) {
            IValueManager manager = ref.get();
            if (manager != null) {
                liveManagers.add(manager);
            }
        }
        return liveManagers;
    }
}