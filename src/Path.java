package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sim.util.Int2D;
import sim.util.Int3D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private ArrayList<Int2D> positionPath = new ArrayList<>();
    private Int2D endPos;
    private Boolean remakePath = false;

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
        positionPath.remove(0);
        return steps.remove(0);
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

}
