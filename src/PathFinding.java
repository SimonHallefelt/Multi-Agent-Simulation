package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    public static Int2D pacman(Warehouse warehouse, Agent a) { //int target.x, int target.y, int x, int y) {
        Int2D target, pos, dir;
        target = a.getTarget();
        pos = a.pos;
        dir = a.delta;
        int disx = Math.abs(target.x-pos.x);
        int disy = Math.abs(target.y-pos.y);
        int tempx = dir.x;
        if (warehouse.canMove(pos.x+dir.x, pos.y+dir.y, new Int2D(dir.x,dir.y), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x+dir.x)) < disx) || 
        (Math.abs(target.y-(pos.y+dir.y)) < disy))) {
            return dir;
        } else if (warehouse.canMove(pos.x+dir.y, pos.y+dir.x, new Int2D(dir.y,dir.x), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x+dir.y)) < disx) || 
        (Math.abs(target.y-(pos.y+dir.x)) < disy))) {
            return new Int2D(dir.y,tempx);
        } else if (warehouse.canMove(pos.x-dir.y, pos.y-dir.x, new Int2D(-dir.y,-dir.x), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x-dir.y)) < disx) || 
        (Math.abs(target.y-(pos.y-dir.x)) < disy))) {
            return new Int2D(-dir.y,-tempx);
        } else if (warehouse.canMove(pos.x+dir.x, pos.y+dir.y, new Int2D(dir.x,dir.y), a.getAgentSize())) {
            return dir;
        } else if (warehouse.canMove(pos.x+dir.y, pos.y+dir.x, new Int2D(dir.y,dir.x), a.getAgentSize())) {
            return new Int2D(dir.y,tempx);
        } else if (warehouse.canMove(pos.x-dir.y, pos.y-dir.x, new Int2D(-dir.y,-dir.x), a.getAgentSize())) {
            return new Int2D(-dir.y,-tempx);
        } else {
            return new Int2D(-dir.x,-dir.y);
        }
    }

    public static Int2D aStar(Warehouse warehouse, Agent a) {
        Int2D target, startPos, endPos;
        target = a.getTarget();
        startPos = a.pos;
        endPos = startPos;
        ArrayList<Int2D> dirs = new ArrayList<>(
            Arrays.asList(new Int2D(1,0),new Int2D(-1,0),new Int2D(0,1),new Int2D(0,-1)));
        HashMap<Int2D, Int2D> reached = new HashMap<>();
        AStarNode startNode = new AStarNode(0, 0, startPos, startPos);
        PriorityQueue<AStarNode> pq = new PriorityQueue<>();
        pq.add(startNode);
        
        while (!pq.isEmpty()) {
            AStarNode node = pq.poll();
            Int2D pos = node.pos;
            if (!reached.containsKey(pos)) {
                reached.put(pos, node.oldPos);
                // if (pos.equals(target)) {
                if (warehouse.taskReached(pos.x, pos.y, a)) {
                    endPos = pos;
                    break;
                }

                for (Int2D dir : dirs) {
                    Int2D newPos = pos.add(dir);
                    if (warehouse.canMove(newPos.x, newPos.y, dir, a.getAgentSize()) && 
                            !reached.containsKey(newPos)) {
                        AStarNode newNode = new AStarNode(node.cost+1, node.cost, pos, newPos);
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
}
