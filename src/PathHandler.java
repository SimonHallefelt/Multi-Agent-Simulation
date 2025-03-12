package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sim.util.Int2D;
import sim.util.Int3D;

public class PathHandler {
    HashMap<Agent, Path> agentPathMap = new HashMap<>();
    HashMap<Agent, HashSet<Int3D>> agent3DMap = new HashMap<>();
    HashMap<Int2D, HashSet<Integer>> tileTimeMap = new HashMap<>();
    boolean cooked = true;

    public PathHandler() {

    }

    public PathHandler(Warehouse w) {
        this(w, null);
    }

    public PathHandler(Warehouse w, Agent ignore) {
        Path p;
        for (Agent a: w.getAgentList()) {
            if (a == ignore) continue;
            p = a.path;
            addAgentPath(a, p);
        }
    }

    public void addAgentPath(Agent a, Path p) {
        agentPathMap.put(a, p);
        cooked = false;
    }

    public void removeAgent(Agent a) {
        agentPathMap.remove(a);
        cooked = false;
    }

    private void updateValues() {
        if (cooked) return;
        cooked = true;
        tileTimeMap.clear();
        HashMap<Int2D, HashSet<Integer>> map;
        HashSet<Int3D> set;
        for (Agent a: agentPathMap.keySet()) {
            ArrayList<Int2D> p = agentPathMap.get(a).getPositionPath();
            map = generatePathMap(p, a.size, a.pos, a.moveTime, a.getDelay());
            for (Int2D k: map.keySet()) {
                tileTimeMap.merge(k, map.get(k), (s1,s2) -> {s1.addAll(s2); return s1;});
            }
            set = generatePathSet(p, a.size, a.pos, a.moveTime, a.getDelay());
            agent3DMap.put(a, set);
        }
        //System.out.println(tileTimeMap);
    }
    
    private HashSet<Int3D> generatePathSet(ArrayList<Int2D> positionPath, Int2D size, Int2D previous, int moveTime, int delay) {
        HashSet<Int3D> set = new HashSet<>();
        Int2D delta;
        //int elapsedTime = delay;
        /** */
        int elapsedTime = delay - moveTime;
        for (int i = 0; i < moveTime; i++) {
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    delta = new Int2D(x,y);
                    set.add(new Int3D(previous.add(delta),elapsedTime));
                }
            }
            elapsedTime++;
        }
        /** */
        for (Int2D p: positionPath) {
            for (int i = 0; i < moveTime; i++) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        delta = new Int2D(x,y);
                        set.add(new Int3D(previous.add(delta),elapsedTime));
                        set.add(new Int3D(p.add(delta),elapsedTime));
                    }
                }
                elapsedTime++;
                if (p == previous) break;
            }
            previous = p;
        }
        for (int i = 0; i < moveTime; i++) {
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    delta = new Int2D(x,y);
                    set.add(new Int3D(previous.add(delta),elapsedTime));
                }
            }
            elapsedTime++;
        }
        return set;
    }

    private HashMap<Int2D, HashSet<Integer>> generatePathMap(ArrayList<Int2D> positionPath, Int2D size, Int2D previous, int moveTime, int delay) {
        HashMap<Int2D, HashSet<Integer>> map = new HashMap<>();
        HashSet<Integer> set;
        Int2D delta;
        //int elapsedTime = delay;
        /** */
        int elapsedTime = delay - moveTime;
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
                }
            }
            elapsedTime++;
        }
        /** */
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
                if (p == previous) break;
            }
            previous = p;
        }
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
                }
            }
            elapsedTime++;
        }
        return map;
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow) {
        updateValues();
        HashSet<Integer> times = tileTimeMap.get(tile);
        if (times == null) return false;
        return times.contains(timeFromNow);
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow, Int2D size) {
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Int2D delta = new Int2D(x,y);
                if (isTileClaimed(tile.add(delta), timeFromNow)) return true;
            }
        }
        return false;
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow, int moveTime) {
        for (int i = 0; i < moveTime; i++) {
            if (isTileClaimed(tile, timeFromNow+i)) return true;
        }
        return false;
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow, Int2D size, int moveTime) {
        for (int i = 0; i < moveTime; i++) {
            if (isTileClaimed(tile, timeFromNow+i, size)) return true;
        }
        return false;
    }

    public boolean willTileBeClaimed(Int2D tile) {
        updateValues();
        return tileTimeMap.containsKey(tile);
    }

    public boolean willTileBeClaimed(Int2D tile, Int2D size) {
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Int2D delta = new Int2D(x,y);
                if (willTileBeClaimed(tile.add(delta))) return true;
            }
        }
        return false;
    }

    public int nextClaim(Int2D tile) {
        updateValues();
        if (tileTimeMap.containsKey(tile)) {
            
        }
        return Integer.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    public PathHandler clone() {
        PathHandler ph = new PathHandler();
        ph.agentPathMap = (HashMap<Agent,Path>) agentPathMap.clone();
        ph.tileTimeMap = (HashMap<Int2D,HashSet<Integer>>) tileTimeMap.clone();
        return ph;
    }

    public void printDiagnostics() {
        for(Agent a: agentPathMap.keySet()) {
            System.out.println(a + ": " +agentPathMap.get(a));
        }
    }
}
