package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import sim.util.Int2D;
import sim.util.Int3D;

public class PathFinding {

    public static Int2D randomWalk(Warehouse warehouse, Int2D dir) {
        double d = warehouse.random.nextDouble();
        boolean vert = (dir.x == 0);
        if (d < 0.25) {
            if (vert) dir = new Int2D(1,0); 
            else      dir = new Int2D(0,1);
        }
        else if (d < 0.5) {
            if (vert) dir = new Int2D(-1,0); 
            else      dir = new Int2D(0,-1);
        }
        return dir;
    }

    public static Int2D pacman(Warehouse warehouse, Int2D pos, Int2D target, Int2D direction, Int2D size) {
        ArrayList<Int2D> dirs = new ArrayList<>(
            Arrays.asList(direction, new Int2D(direction.y, direction.x), new Int2D(-direction.y, -direction.x))
        );

        dirs.sort((d,o) -> getDistance(pos.add(d), target, size) - getDistance(pos.add(o), target, size));

        for (Int2D dir : dirs) {
            if (warehouse.canMove(pos.add(dir), dir, size)) {
                return dir;
            }
        }

        return new Int2D(-direction.x,-direction.y);
    }

    public static ArrayList<Int2D> aStar(Warehouse warehouse, Int2D target, Int2D startPos, Int2D size, boolean noAgents) {
        Int2D endPos = startPos;
        ArrayList<Int2D> dirs = new ArrayList<>(
            Arrays.asList(new Int2D(1,0),new Int2D(-1,0),new Int2D(0,1),new Int2D(0,-1))
        );
        HashMap<Int2D, Int2D> reached = new HashMap<>();
        AStarNode startNode = new AStarNode(0, 0, startPos, startPos);
        PriorityQueue<AStarNode> pq = new PriorityQueue<>();
        pq.add(startNode);
        
        while (!pq.isEmpty()) {
            AStarNode node = pq.poll();
            Int2D pos = node.pos;
            if (!reached.containsKey(pos)) {
                reached.put(pos, node.oldPos);
                if (targetReached(pos, size, target)) {
                    endPos = pos;
                    break;
                }

                for (Int2D dir : dirs) {
                    Int2D newPos = pos.add(dir);
                    if (warehouse.canMove(newPos, dir, size, noAgents) && 
                    !reached.containsKey(newPos)) {
                        int dist = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);
                        AStarNode newNode = new AStarNode(dist + node.previousCost+1, node.previousCost+1, pos, newPos);
                        pq.add(newNode);
                    }
                }
            }
        }

        if (startPos.equals(endPos)) {
            return new ArrayList<>(Arrays.asList(startPos));
        }

        ArrayList<Int2D> steps = new ArrayList<>();
        Int2D pos = endPos;
        while (!startPos.equals(pos)) {
            steps.add(pos);
            pos = reached.get(pos);
        }
        Collections.reverse(steps);
        
        return steps;
    }

    public static ArrayList<Int2D> aStar(Warehouse warehouse, Int2D target, Int2D startPos, Int2D size) {
        return aStar(warehouse, target, startPos, size, false);
    }

    private static class AStarNode implements Comparable<AStarNode> {
        // cost to reach, cost before, old position, new position
        int cost, previousCost;
        Int2D oldPos, pos;

        public AStarNode(int cost, int previousCost, Int2D oldPos, Int2D pos) {
            this.cost = cost;
            this.previousCost = previousCost;
            this.oldPos = oldPos;
            this.pos = pos;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    public static boolean targetReached(Int2D pos, Int2D size, Int2D target) {
        Int2D diff = target.subtract(pos);
        return diff.x < size.x && diff.y < size.y && diff.x >= 0 && diff.y >= 0;
    }

    public static int getDistance(Int2D start, Int2D finish, Int2D size) {
        int dx = start.x - finish.x;
        int dy = start.y - finish.y;
        if (dx < 0) dx = Math.min(dx + size.x - 1,0);
        if (dy < 0) dy = Math.min(dy + size.y - 1,0);
        return Math.abs(dx) + Math.abs(dy);
    }

    
    public static boolean isTileClaimed(HashSet<Int3D> pathSet, Int2D tile, int timeFromNow) {
        return pathSet.contains(new Int3D(tile.x,tile.y,timeFromNow));
    }
}
