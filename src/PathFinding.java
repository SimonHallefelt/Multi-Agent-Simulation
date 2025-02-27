package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.util.Int2D;

public class PathFinding {

    public static Int2D randomWalk(Warehouse warehouse, Agent a) {
        double d = warehouse.random.nextDouble();
        Int2D dir = a.delta;
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

    public static Int2D pacman(Warehouse warehouse, Agent a) {
        Int2D target, pos, delta, size;
        target = a.getTarget();
        size = a.getAgentSize();
        pos = a.pos;
        delta = a.delta;
        double dist = target.distance(pos);
        ArrayList<Int2D> dirs = new ArrayList<>(
            Arrays.asList(delta, new Int2D(delta.y, delta.x), new Int2D(-delta.y, -delta.x))
        );

        for (Int2D dir : dirs) {
            if (target.distance(pos.add(dir)) < dist && warehouse.canMove(pos.add(dir), dir, size)) {
                return dir;
            }
        }
        for (Int2D dir : dirs) {
            if (warehouse.canMove(pos.add(dir), dir, size)) {
                return dir;
            }
        }
        return new Int2D(-delta.x,-delta.y);
    }

    public static Int2D aStar(Warehouse warehouse, Agent a) {
        Int2D target,startPos, endPos;
        target = a.getTarget();
        startPos = a.pos;
        endPos = startPos;
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
                if (warehouse.taskReached(pos.x, pos.y, a)) {
                    endPos = pos;
                    break;
                }

                for (Int2D dir : dirs) {
                    Int2D newPos = pos.add(dir);
                    if (warehouse.canMove(newPos, dir, a.getAgentSize()) && 
                    !reached.containsKey(newPos)) {
                        int dist = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);
                        AStarNode newNode = new AStarNode(dist + node.previousCost+1, node.previousCost+1, pos, newPos);
                        pq.add(newNode);
                    }
                }
            }
        }

        if (startPos.equals(endPos)) {
            return new Int2D(0, 0);
        }

        Int2D pos = endPos;
        while (!startPos.equals(reached.get(pos))) {
            pos = reached.get(pos);
        }

        return new Int2D(pos.x - startPos.x, pos.y - startPos.y);
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

    public static int getDistance(Int2D start, Int2D finish, Int2D size) {
        int dx = start.x - finish.x;
        int dy = start.y - finish.y;
        if (dx < 0) dx = Math.min(dx + size.x - 1,0);
        if (dy < 0) dy = Math.min(dy + size.y - 1,0);
        return Math.abs(dx) + Math.abs(dy);
    }
}
