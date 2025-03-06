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

    public HashSet<Int3D> getPathSet(Int2D size, Int2D previous, int moveTime, int delay) {
        HashSet<Int3D> set = new HashSet<>();
        int elapsedTime = 0;
        if (positionPath.size() == 0) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        set.add(new Int3D(previous.x + x,previous.y + y,Integer.MAX_VALUE));
                    }
                }
            elapsedTime++;
        }
        for (Int2D p: positionPath) {
            for (int i = 0; i < moveTime; i++) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        set.add(new Int3D(previous.x + x,previous.y + y,elapsedTime - delay));
                        set.add(new Int3D(p.x + x,p.y + y,elapsedTime - delay));
                    }
                }
                elapsedTime++;
            }
            previous = p;
        }
        return set;
    }

    public HashMap<Int2D, HashSet<Integer>> getPathMap(Int2D size, Int2D previous, int moveTime, int delay) {
        HashMap<Int2D, HashSet<Integer>> map = new HashMap<>();
        HashSet<Integer> set;
        Int2D delta;
        int elapsedTime = 0;
        if (positionPath.size() == 0) {
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    set = map.get(previous);
                    if (set == null) {
                        set = new HashSet<>();
                        map.put(previous, set);
                    }
                    set.add(elapsedTime);
                }
            }
        }
        for (Int2D p: positionPath) {
            for (int i = 0; i < moveTime; i++) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        delta = new Int2D(x,y);
                        set = map.get(previous.add(delta));
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(previous.add(delta), set);
                        }
                        set.add(elapsedTime);
                        set = map.get(p.add(delta));
                        if (set == null) {
                            set = new HashSet<>();
                            map.put(p.add(delta), set);
                        }
                        set.add(elapsedTime);
                    }
                }
                elapsedTime++;
            }
            previous = p;
        }
        return map;
    }

    public static void addPathMap(HashMap<Int2D, HashSet<Integer>> primary, HashMap<Int2D, HashSet<Integer>> secondary) {
        for (Int2D k: secondary.keySet()) {
            primary.merge(k, secondary.get(k), (s1,s2) -> {s1.addAll(s2); return s1;});
        }
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
