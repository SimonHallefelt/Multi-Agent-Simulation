package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

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

    public static Int2D trueRandomWalk(Warehouse warehouse) {
        Int2D[] directions = new Int2D[] {new Int2D(1,0), new Int2D(0,1), new Int2D(-1,0), new Int2D(0,-1)};
        int i = warehouse.random.nextInt(4);
        return directions[i];
    }

    public static Int2D randomAccessibleWalk(Warehouse warehouse, Int2D position, Int2D size) {
        Int2D[] directions = new Int2D[] {new Int2D(1,0), new Int2D(0,1), new Int2D(-1,0), new Int2D(0,-1)};
        ArrayList<Int2D> viableDirections = new ArrayList<>();
        for (Int2D d: directions) {
            if (warehouse.canMove(position.add(d), d, size)) viableDirections.add(d);
        }
        int v = viableDirections.size();
        if (v == 0) return new Int2D(0,0);
        int s = warehouse.random.nextInt(viableDirections.size());
        return viableDirections.get(s);
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

    public static ArrayList<Int2D> aStarNoPathCollisions(Warehouse warehouse, Int2D target, Int2D startPos, 
                                                         Int2D size, int moveTime, HashSet<Int3D> othersPaths) {
        Int3D startPos3d = new Int3D(startPos, 0);
        Int3D endPos3d = startPos3d;
        ArrayList<Int3D> dirs = new ArrayList<>(
            Arrays.asList(new Int3D(1,0,moveTime),new Int3D(-1,0,moveTime),new Int3D(0,1,moveTime),new Int3D(0,-1,moveTime),new Int3D(0,0,moveTime))
        );
        HashMap<Int3D, Int3D> reached = new HashMap<>();
        AStarNodeNoPathCollision startNode = new AStarNodeNoPathCollision(0, 0, startPos3d, startPos3d, 0);
        PriorityQueue<AStarNodeNoPathCollision> pq = new PriorityQueue<>();
        pq.add(startNode);
        
        HashMap<Int2D, Integer> reachedCounter = new HashMap<>();

        while (!pq.isEmpty()) {
            AStarNodeNoPathCollision node = pq.poll();
            Int3D pos3d = node.pos;
            Int2D pos2d = new Int2D(pos3d.x,pos3d.y);
            if (!reached.containsKey(pos3d)) {
                reached.put(pos3d, node.oldPos);
                if (targetReached(pos2d, size, target)) {
                    endPos3d = pos3d;
                    break;
                }

                outerLoop: 
                for (Int3D dir : dirs) {
                    Int3D newPos3d = pos3d.add(dir);
                    Int2D newPos2d = new Int2D(newPos3d.x, newPos3d.y);
                    if (warehouse.canMove(newPos2d, new Int2D(dir.x,dir.y), size, true) && 
                    !reached.containsKey(newPos3d)) {
                        for (int i = 0; i < moveTime; i++){
                            if (isTileClaimed(othersPaths, pos2d, node.previousCost+i) ||
                            isTileClaimed(othersPaths, newPos2d, node.previousCost+i)) {
                                continue outerLoop;
                            }
                        }
                        reachedCounter.put(newPos2d, reachedCounter.getOrDefault(newPos2d, 0)+1);
                        if (reachedCounter.get(newPos2d) >= 5) continue;

                        int wait = dir.equals(new Int3D(0,0,moveTime)) ? 1 : 0;
                        int dist = Math.abs(newPos2d.x - target.x) + Math.abs(newPos2d.y - target.y);
                        AStarNodeNoPathCollision newNode = new AStarNodeNoPathCollision(dist + node.previousCost+moveTime, node.previousCost+moveTime, pos3d, newPos3d, node.waitCounter+wait);
                        pq.add(newNode);
                    }
                }
            }
        }

        ArrayList<Int2D> steps = new ArrayList<>();
        Int3D pos = endPos3d;
        while (!pos.equals(startPos3d)) {
            steps.add(new Int2D(pos.x, pos.y));
            pos = reached.get(pos);
        }
        Collections.reverse(steps);
        return steps;
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

    private static class AStarNodeNoPathCollision implements Comparable<AStarNodeNoPathCollision> {
        // cost to reach, cost before, old position, new position
        int cost, previousCost, waitCounter;
        Int3D oldPos, pos;

        public AStarNodeNoPathCollision(int cost, int previousCost, Int3D oldPos, Int3D pos, int waitCounter) {
            this.cost = cost;
            this.previousCost = previousCost;
            this.oldPos = oldPos;
            this.pos = pos;
            this.waitCounter = waitCounter;
        }

        @Override
        public int compareTo(AStarNodeNoPathCollision other) {
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
        return pathSet.contains(new Int3D(tile.x,tile.y,timeFromNow)) || pathSet.contains(new Int3D(tile.x,tile.y,Integer.MAX_VALUE));
    }
}
