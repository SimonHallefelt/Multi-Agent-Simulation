package simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import sim.util.Int2D;

public class PathHandler {
    HashMap<Agent, Path> agentPathMap = new HashMap<>();
    HashMap<Agent, Path> agentDesireMap = new HashMap<>();
    HashMap<Int2D, HashSet<Integer>> tileTimeMap = new HashMap<>();
    HashMap<Int2D, HashSet<Integer>> tileTimeMapDesired = new HashMap<>();
    boolean cooked = true;

    public PathHandler() {

    }

    public PathHandler(Warehouse w) {
        this(w, null);
    }

    public PathHandler(Warehouse w, Agent ignore) {
        Path p;
        for (Agent a : w.getAgentList()) {
            if (a == ignore)
                continue;
            p = a.getPath();
            addAgentPath(a, p);
            if (a.desirePath != null) addAgentDesire(a, a.desirePath);
        }
    }

    public void addAgentPath(Agent a, Path p) {
        agentPathMap.put(a, p);
        cooked = false;
    }

    public void addAgentDesire(Agent a, Path p) {
        agentDesireMap.put(a, p);
        cooked = false;
    }

    public void removeAgent(Agent a) {
        agentPathMap.remove(a);
        agentDesireMap.remove(a);
        cooked = false;
    }

    private void updateValues() {
        if (cooked)
            return;
        cooked = true;
        tileTimeMap.clear();
        tileTimeMapDesired.clear();
        HashMap<Int2D, HashSet<Integer>> map;
        for (Agent a : agentPathMap.keySet()) {
            List<Int2D> p = agentPathMap.get(a).getList();
            map = generatePathMap(p, a.size, a.pos, a.moveTime, a.getDelay(), a.hasTag("stuck"));
            for (Int2D k : map.keySet()) {
                tileTimeMap.merge(k, map.get(k), (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
            }
        }
        for (Agent a : agentDesireMap.keySet()) {
            List<Int2D> p = agentDesireMap.get(a).getList();
            map = generatePathMap(p, a.size, a.pos, a.moveTime, a.getDelay(), false);
            for (Int2D k : map.keySet()) {
                tileTimeMap.merge(k, map.get(k), (s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });
            }
        }
        // System.out.println(tileTimeMap);
    }

    private HashMap<Int2D, HashSet<Integer>> generatePathMap(List<Int2D> positionPath, Int2D size, Int2D previous,
            int moveTime, int delay, boolean stayPut) {
        HashMap<Int2D, HashSet<Integer>> map = new HashMap<>();
        HashSet<Integer> set;
        Int2D delta;
        // int elapsedTime = delay;
        /** */
        int elapsedTime = delay - moveTime;
        for (int i = 0; i < moveTime; i++) {
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    delta = new Int2D(x, y);
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
        for (Int2D p : positionPath) {
            for (int i = 0; i < moveTime; i++) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        delta = new Int2D(x, y);
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
                if (p == previous)
                    break;
            }
            previous = p;
        }
        if (stayPut)
            elapsedTime = Integer.MAX_VALUE;
        for (int i = 0; i < moveTime; i++) {
            for (int x = 0; x < size.x; x++) {
                for (int y = 0; y < size.y; y++) {
                    delta = new Int2D(x, y);
                    set = map.get(previous.add(delta));
                    if (set == null) {
                        set = new HashSet<>();
                        map.put(previous.add(delta), set);
                    }
                    set.add(elapsedTime);
                }
            }
            if (stayPut)
                break;
            elapsedTime++;
        }
        return map;
    }

    private boolean isTileClaimed(Int2D tile, int timeFromNow, boolean considerDesire) {
        updateValues();
        HashSet<Integer> times = tileTimeMap.get(tile);
        if (considerDesire) {
            HashSet<Integer> desireTimes = tileTimeMap.get(tile);
            if (times == null)
                times = desireTimes;
            else if (desireTimes != null)
                times.addAll(desireTimes);
        }
        if (times == null)
            return false;
        return times.contains(timeFromNow) || times.contains(Integer.MAX_VALUE);
    }

    private boolean isTileClaimed(Int2D tile, int timeFromNow, Int2D size, boolean considerDesire) {
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Int2D delta = new Int2D(x, y);
                if (isTileClaimed(tile.add(delta), timeFromNow, considerDesire))
                    return true;
            }
        }
        return false;
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow, Int2D size, int moveTime, boolean considerDesire) {
        for (int i = 0; i < moveTime; i++) {
            if (isTileClaimed(tile, timeFromNow + i, size, considerDesire))
                return true;
        }
        return false;
    }

    public boolean isTileClaimed(Int2D tile, int timeFromNow, Int2D size, int moveTime) {
        return isTileClaimed(tile, timeFromNow, size, moveTime, false);
    }

    private boolean willTileBeClaimed(Int2D tile, boolean considerDesire) {
        updateValues();
        return tileTimeMap.containsKey(tile);
    }

    public boolean willTileBeClaimed(Int2D tile, Int2D size, boolean considerDesire) {
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Int2D delta = new Int2D(x, y);
                if (willTileBeClaimed(tile.add(delta), considerDesire))
                    return true;
            }
        }
        return false;
    }

    public boolean willTileBeClaimed(Int2D tile, Int2D size) {
        return willTileBeClaimed(tile, size, false);
    }

    public int nextClaim(Int2D tile) {
        updateValues();
        if (tileTimeMap.containsKey(tile)) {
            HashSet<Integer> set = tileTimeMap.get(tile);
            int smallest = Integer.MAX_VALUE;
            for (int i : set) {
                if (smallest > i)
                    smallest = i;
            }
            return smallest;
        }
        return Integer.MAX_VALUE;
    }

    public int nextClaim(Int2D tile, Int2D size) {
        int smallest = Integer.MAX_VALUE;
        int c;
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                Int2D delta = new Int2D(x,y);
                c = nextClaim(tile.add(delta));
                if (c < smallest) smallest = c;
            }
        }
        return smallest;
    }

    @SuppressWarnings("unchecked")
    public PathHandler clone() {
        PathHandler ph = new PathHandler();
        ph.agentPathMap = (HashMap<Agent, Path>) agentPathMap.clone();
        ph.tileTimeMap = (HashMap<Int2D, HashSet<Integer>>) tileTimeMap.clone();
        return ph;
    }

    public void printDiagnostics() {
        for (Agent a : agentPathMap.keySet()) {
            System.out.println(a + ": " + agentPathMap.get(a));
        }
    }
}
