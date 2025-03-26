package simulation;

import java.util.ArrayList;
import java.util.List;

import sim.util.Int2D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private Int2D endPos;
    private Boolean remakePath = false;

    public Path(Int2D startPos) {
        endPos = startPos == null ? new Int2D(0, 0) : startPos;
    }

    public void addPos(Int2D pos) {
        steps.add(pos);
        endPos = pos;
    }

    public void addNewPositionPath(Int2D pos, ArrayList<Int2D> positionPath) {
        this.steps = positionPath;
        endPos = positionPath.size() != 0 ? positionPath.get(positionPath.size() - 1) : pos;
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

    public int size() {
        return steps.size();
    }

    @SuppressWarnings("unchecked")
    public List<Int2D> getList() {
        return (ArrayList<Int2D>) steps.clone();
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
