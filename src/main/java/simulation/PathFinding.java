package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import sim.util.Int2D;
import sim.util.Int3D;

public class PathFinding {

    public static Int2D randomWalk(Warehouse warehouse, Int2D pos, Int2D dir) {
        double d = warehouse.random.nextDouble();
        boolean vert = (dir.x == 0);
        if (d < 0.25) {
            if (vert)
                dir = new Int2D(1, 0);
            else
                dir = new Int2D(0, 1);
        } else if (d < 0.5) {
            if (vert)
                dir = new Int2D(-1, 0);
            else
                dir = new Int2D(0, -1);
        }
        return pos.add(dir);
    }

    public static Int2D trueRandomWalk(Warehouse warehouse, Int2D position) {
        Int2D[] directions = new Int2D[] { position.add(1, 0), position.add(0, 1), position.add(-1, 0), position.add(0, -1) };
        int i = warehouse.random.nextInt(4);
        return directions[i];
    }

    public static Int2D randomAccessibleWalk(Warehouse warehouse, Int2D position, Int2D size) {
        Int2D[] directions = new Int2D[] { position.add(1, 0), position.add(0, 1), position.add(-1, 0), position.add(0, -1) };
        List<Int2D> viableDirections = new ArrayList<>();
        for (Int2D d : directions) {
            if (warehouse.canMove(position, d, size))
                viableDirections.add(d);
        }
        int v = viableDirections.size();
        if (v == 0)
            return position;
        int s = warehouse.random.nextInt(viableDirections.size());
        return viableDirections.get(s);
    }

    public static Int2D randomUnobstructiveWalk(Warehouse warehouse, PathHandler pathhandler, Int2D position, Int2D size) {
        Int2D[] directions = new Int2D[] { position.add(1, 0), position.add(0, 1), position.add(-1, 0), position.add(0, -1), position.add(0, 0) };

        List<Int2D> viableDirections = new ArrayList<>();
        for (Int2D d : directions) {
            if (warehouse.canMove(position, d, size) && !pathhandler.willTileBeClaimed(d, size, true))
                viableDirections.add(d);
        }
        int v = viableDirections.size();
        if (v == 0) return randomAccessibleWalk(warehouse, position, size);
        int s = warehouse.random.nextInt(viableDirections.size());
        return viableDirections.get(s);
    }

    public static Int2D pacman(Warehouse warehouse, Int2D pos, Int2D target, Int2D direction, Int2D size) {
        direction = direction.x == direction.y ? new Int2D(1, 0) : direction;
        List<Int2D> dirs = new ArrayList<>(
                Arrays.asList(direction, new Int2D(direction.y, direction.x), new Int2D(-direction.y, -direction.x)));

        dirs.sort((d, o) -> getDistance(pos.add(d), target, size) - getDistance(pos.add(o), target, size));

        for (Int2D dir : dirs) {
            if (warehouse.canMove(pos.add(dir), dir, size)) {
                return dir;
            }
        }

        return new Int2D(-direction.x, -direction.y);
    }

    public static List<Int2D> moveOutOfWay(Warehouse warehouse, PathHandler pathHandler, Int2D startPos,
            Int2D size, int moveTime) {
        Int2D endPos = startPos;
        Int2D[] dirs = new Int2D[] {
                new Int2D(1, 0),
                new Int2D(-1, 0),
                new Int2D(0, 1),
                new Int2D(0, -1)
        };
        HashMap<Int2D, Int2D> reached = new HashMap<>();
        AStarNode startNode = new AStarNode(0, 0, startPos, startPos);
        PriorityQueue<AStarNode> pq = new PriorityQueue<>();
        pq.add(startNode);

        while (!pq.isEmpty()) {
            AStarNode node = pq.poll();
            Int2D pos = node.pos;
            if (!reached.containsKey(pos)) {
                reached.put(pos, node.oldPos);
                // if (targetReached(pos, size, target)) {
                if (!pathHandler.willTileBeClaimed(pos, size, true)) {
                    endPos = pos;
                    break;
                }

                for (Int2D dir : dirs) {
                    Int2D newPos = pos.add(dir);
                    if (warehouse.canMove(pos, newPos, size, true) &&
                            !reached.containsKey(newPos) &&
                            pathHandler.isTileClaimed(newPos, node.previousCost, size, moveTime)) {
                        int safety = node.previousCost + moveTime - pathHandler.nextClaim(newPos, size);// Math.abs(newPos.x -
                                                                                                  // target.x) +
                                                                                                  // Math.abs(newPos.y -
                                                                                                  // target.y);
                        AStarNode newNode = new AStarNode(safety, node.previousCost + 1, pos, newPos);
                        pq.add(newNode);
                    }
                }
            }
        }

        List<Int2D> steps = new ArrayList<>();
        Int2D pos = endPos;
        while (!startPos.equals(pos)) {
            steps.add(pos);
            pos = reached.get(pos);
        }
        Collections.reverse(steps);

        return steps;
    }

    public static List<Int2D> aStar(Warehouse warehouse, Int2D target, Int2D startPos, Int2D size,
            boolean noAgents) {
        Int2D endPos = startPos;
        Int2D[] dirs = new Int2D[] {
                new Int2D(1, 0),
                new Int2D(-1, 0),
                new Int2D(0, 1),
                new Int2D(0, -1)
        };
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
                    if (warehouse.canMove(pos, newPos, size, noAgents) &&
                            !reached.containsKey(newPos)) {
                        int dist = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);
                        AStarNode newNode = new AStarNode(dist + node.previousCost + 1, node.previousCost + 1, pos,
                                newPos);
                        pq.add(newNode);
                    }
                }
            }
        }

        List<Int2D> steps = new ArrayList<>();
        Int2D pos = endPos;
        while (!startPos.equals(pos)) {
            steps.add(pos);
            pos = reached.get(pos);
        }
        Collections.reverse(steps);

        return steps;
    }

    public static List<Int2D> aStar(Warehouse warehouse, Int2D target, Int2D startPos, Int2D size) {
        return aStar(warehouse, target, startPos, size, false);
    }

    public static List<Int2D> aStarNoPathCollisions(Warehouse warehouse, PathHandler othersPaths, Int2D target,
            Int2D startPos,
            Int2D size, int moveTime) {
        Int3D[] dirs = new Int3D[] {
                new Int3D(1, 0, moveTime),
                new Int3D(-1, 0, moveTime),
                new Int3D(0, 1, moveTime),
                new Int3D(0, -1, moveTime),
                new Int3D(0, 0, 1)
        };
        HashSet<Int3D> reached = new HashSet<>();
        AStarNodeNoPathCollision startNode = new AStarNodeNoPathCollision(0, 0, null, startPos);
        PriorityQueue<AStarNodeNoPathCollision> pq = new PriorityQueue<>();
        pq.add(startNode);
        AStarNodeNoPathCollision endNode = startNode;
        HashMap<Int2D, Integer> reachedCounter = new HashMap<>();

        while (!pq.isEmpty()) {
            AStarNodeNoPathCollision node = pq.poll();
            if (reached.add(new Int3D(node.pos, node.previousCost))) {
                if (targetReached(node.pos, size, target)) {
                    endNode = node;
                    break;
                }

                for (Int3D dir : dirs) {
                    Int2D newPos = node.pos.add(dir.x, dir.y);
                    if (warehouse.canMove(node.pos, newPos, size, true) &&
                            !reached.contains(new Int3D(newPos, node.previousCost + dir.z))) {
                        if (othersPaths.isTileClaimed(node.pos, node.previousCost, size, dir.z) ||
                                othersPaths.isTileClaimed(newPos, node.previousCost, size, dir.z))
                            continue;

                        reachedCounter.put(newPos, reachedCounter.getOrDefault(newPos, 0) + 1);
                        if (reachedCounter.get(newPos) > moveTime * 10)
                            continue;

                        int dist = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);
                        pq.add(new AStarNodeNoPathCollision(dist + node.previousCost + dir.z, node.previousCost + dir.z,
                                node, newPos));
                    }
                }
            }
        }

        List<Int2D> steps = new ArrayList<>();
        AStarNodeNoPathCollision node = endNode;
        while (node.previousNode != null) {
            steps.add(node.pos);
            node = node.previousNode;
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
        int cost, previousCost;
        AStarNodeNoPathCollision previousNode;
        Int2D pos;

        public AStarNodeNoPathCollision(int cost, int previousCost, AStarNodeNoPathCollision previousNode, Int2D pos) {
            this.cost = cost;
            this.previousCost = previousCost;
            this.previousNode = previousNode;
            this.pos = pos;
        }

        @Override
        public int compareTo(AStarNodeNoPathCollision other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    public static List<Int2D> getAccessiblePoints(Warehouse warehouse, Int2D startPos, Int2D size, List<Int2D> targets) {
        Queue<Int2D> possibleRouts = new LinkedList<>();
        HashSet<Int2D> visited = new HashSet<>();
        HashSet<Int2D> reached = new HashSet<>();
        Int2D[] dirs = new Int2D[] {
            new Int2D(1, 0),
            new Int2D(-1, 0),
            new Int2D(0, 1),
            new Int2D(0, -1)
        };
        possibleRouts.add(startPos);
        visited.add(startPos);

        while(!possibleRouts.isEmpty()){
            Int2D pos = possibleRouts.poll();
            for (Int2D target : targets) {
                if(targetReached(pos, size, target)) {
                    reached.add(target);
                }
            }
            for(Int2D dir : dirs) {
                Int2D newPos = pos.add(dir);
                if(warehouse.canMove(pos, newPos, size, true) && !visited.contains(newPos)){
                    possibleRouts.add(newPos);
                    visited.add(newPos);
                }
            }
        }

        return new ArrayList<>(reached);
    }

    public static boolean targetReached(Int2D pos, Int2D size, Int2D target) {
        Int2D diff = target.subtract(pos);
        return diff.x < size.x && diff.y < size.y && diff.x >= 0 && diff.y >= 0;
    }

    public static boolean targetReached(Int3D pos, Int2D size, Int2D target) {
        return targetReached(new Int2D(pos.x, pos.y), size, target);
    }

    public static int getDistance(Int2D start, Int2D finish, Int2D size) {
        int dx = start.x - finish.x;
        int dy = start.y - finish.y;
        if (dx < 0)
            dx = Math.min(dx + size.x - 1, 0);
        if (dy < 0)
            dy = Math.min(dy + size.y - 1, 0);
        return Math.abs(dx) + Math.abs(dy);
    }
}
