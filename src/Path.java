package src;

import java.util.ArrayList;

import sim.util.Int2D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private ArrayList<Int2D> positionPath = new ArrayList<>();
    private Int2D endPos;
    private Boolean remakePath = false;
    private Int2D poppedPos;
    private Int2D poppedDir;

    public Path(Int2D startPos) {
        endPos = startPos==null ? new Int2D(0,0) : startPos;
    }

    public void addStep(Int2D dir) {
        steps.add(dir);
        endPos = endPos.add(dir);
        positionPath.add(endPos);
    }

    public void addNewStepPath(Int2D pos, ArrayList<Int2D> stepPath) {
        this.steps = stepPath;
        generatePositionPathFromSteps(pos);
    }

    public void addNewPositionPath(Int2D pos, ArrayList<Int2D> positionPath) {
        this.positionPath = positionPath;
        endPos = positionPath.size() != 0 ? positionPath.get(positionPath.size()-1) : pos;
        generateStepsFromPositionPath(pos);
    }

    private void generateStepsFromPositionPath(Int2D pos) {
        steps = new ArrayList<>();
        Int2D nextPos;
        for (int i = 0; i < positionPath.size(); i++) {
            nextPos = this.positionPath.get(i);
            steps.add(nextPos.subtract(pos));
            pos = nextPos;
        }
    }

    private void generatePositionPathFromSteps(Int2D pos) {
        positionPath = new ArrayList<>();
        for (Int2D step : steps) {
            pos = pos.add(step);
            positionPath.add(pos);
        }
        endPos = positionPath.get(positionPath.size()-1);
    }

    public Int2D pop() {
        if (steps.isEmpty()) return null;
        poppedPos = positionPath.remove(0);
        poppedDir = steps.remove(0);
        return poppedDir;
    }

    public Int2D peek() {
        if (steps.isEmpty()) return null;
        return steps.get(0);
    }

    public ArrayList<Int2D> getPositionPath() {
        return positionPath;
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
        for (Int2D p: positionPath) {
            s += p;
        }
        return s;
    }

    public void backtrack() {
        // TODO Auto-generated method stub
        if (poppedDir == null) return;
        positionPath.add(0, poppedPos);
        steps.add(0, poppedDir);
        poppedDir = null;
    }
}
