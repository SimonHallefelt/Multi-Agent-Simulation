package simulation;

import java.util.ArrayList;
import java.util.List;

import sim.util.Int2D;

public class Path {
    private List<Int2D> steps;
    private Boolean remakePath = false;

    public Path() {
        steps = new ArrayList<>();
    }

    public Path(Int2D position) {
        steps = new ArrayList<>();
        steps.add(position);
    }

    public Path(List<Int2D> positionPath) {
        steps = new ArrayList<>(positionPath);
    }

    /**
     * Adds a position to the end of the path
     * 
     * @param position - the position to be added
     */
    public void addStep(Int2D position) {
        steps.add(position);
    }

    /**
     * Adds a list of elements to the path in order
     * 
     * @param positionList - the list of positions to be added.
     */
    public void addSteps(List<Int2D> positionList) {
        for (Int2D i: positionList) {
            addStep(i);
        }
    }

    public Int2D pop() {
        if (steps.isEmpty())
            return null;
        return steps.remove(0);
    }

    public Int2D peek() {
        if (steps.isEmpty())
            return null;
        return steps.get(0);
    }
    
    public Int2D getDestination() {
        if (steps.isEmpty())
            return null;
        return steps.get(steps.size()-1);
    }

    public int size() {
        return steps.size();
    }

    public List<Int2D> getList() {
        return new ArrayList<>(steps);
    }

    public Boolean getRemakePath() {
        return remakePath;
    }

    public void setRemakePath(Boolean remakePath) {
        this.remakePath = remakePath;
    }

    public Boolean isEmpty() {
        return steps.isEmpty();
    }

    public String toString() {
        String s = "";
        for (Int2D p : steps) {
            s += p;
        }
        return s;
    }
}
