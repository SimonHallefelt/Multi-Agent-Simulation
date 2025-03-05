package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sim.util.Int2D;
import sim.util.Int3D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private ArrayList<Int2D> positionPath = new ArrayList<>();
    private Int2D tail;
    private Warehouse warehouse;

    public Path(Int2D start, Warehouse wh) {
        tail = start;
        warehouse = wh;
    }

    public boolean addStep (Int2D dir) {
        Int2D target = tail.add(dir);
        if (!warehouse.isWall(target)) {
            steps.add(dir);
            tail = target;
            return true;
        }
        return false;
    }

    public void addNewPositionPath(Int2D pos, ArrayList<Int2D> positionPath) {
        this.positionPath = positionPath;
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

    public Int2D pop() {
        if (steps.isEmpty()) return null;
        Int2D next = steps.remove(0);
        positionPath.remove(0);
        return next;
    }

    public HashSet<Int3D> getPathSet(Int2D size, Int2D previous, int moveTime, int delay) {
        HashSet<Int3D> set = new HashSet<>();
        int elapsedTime = 0;
        if (positionPath.size() == 0) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        set.add(new Int3D(previous.x,previous.y,Integer.MAX_VALUE));
                    }
                }
            elapsedTime++;
        }
        for (Int2D p: positionPath) {
            for (int i = 0; i < moveTime; i++) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        set.add(new Int3D(previous.x,previous.y,elapsedTime - delay));
                        set.add(new Int3D(p.x,p.y,elapsedTime - delay));
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

}
