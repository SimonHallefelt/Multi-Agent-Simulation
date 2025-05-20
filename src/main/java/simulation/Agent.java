package simulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import simulation.interfaces.Colorable;

public abstract class Agent implements Steppable, Colorable {
    protected List<Long> scores = new ArrayList<>();
    protected Int2D pos = new Int2D(0, 0);
    protected Int2D dir = new Int2D(1, 0);
    protected Int2D target = null;
    protected int moveTime = 1;
    protected Boolean isMoving = false;
    protected List<AgentClone> agentClones = new ArrayList<>();
    protected List<Trail> trails = new ArrayList<>();
    protected Int2D size = new Int2D(1, 1);
    protected Color color = Color.GRAY;
    private Path path = new Path();
    protected Path desirePath = null;
    private int timeToCompletedMovement;
    private int distanceBetweenAllTargets = 0;
    private String id = "Agent";
    protected Boolean moveIfBlocking = false;
    boolean debug = false;

    private HashSet<String> tags = new HashSet<>();
    private HashMap<Int2D, Integer> visited = new HashMap<>();

    public void setPath(Path p) {
        path = p;
        removeTag("stuck");
    }

    public void setColor(Color c) {
        color = c;
    }

    public void setPosition(Int2D pos) {
        this.pos = pos;
    }

    public void setPosition(int x, int y) {
        setPosition(new Int2D(x, y));
    }

    public void setMoveTime(int moveTime) {
        this.moveTime = moveTime;
    }

    public int getMoveTime() {
        return moveTime;
    }

    public void setSize(Int2D size) {
        this.size = size;
    }

    public Int2D getSize() {
        return size;
    }

    public int getDistanceBetweenAllTargets() {
        return distanceBetweenAllTargets;
    }

    public int getDistanceCompletedAllTargets() {
        if (target == null) return 0;
        return distanceBetweenAllTargets + PathFinding.getDistance(pos,target,size);
    }

    public HashMap<Int2D, Integer> getVisited() {
        return visited;
    }

    public int getCurrentVisited() {
        return visited.getOrDefault(pos, 0);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDistanceBetweenTargets(int distanceBetweenTargets) {
        this.distanceBetweenAllTargets = distanceBetweenTargets;
    }

    public void subDistanceBetweenTargets(int distanceBetweenTargets) {
        this.distanceBetweenAllTargets -= distanceBetweenTargets;
    }
    public void addDistanceBetweenTargets(int distanceBetweenTargets) {
        this.distanceBetweenAllTargets += distanceBetweenTargets;
    }

    public void setDebug(boolean d) {
        debug = d;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setTarget(int x, int y) {
        setTarget(new Int2D(x, y));
    }

    public void setTarget(Int2D i) {
        target = i;
        onSetTarget();
        if (debug)
            System.out.println(id + ": new target " + i);
    }

    public void onSetTarget() {
        path.setRemakePath(true);
        return;
    }

    public boolean getRemakePath() {
        return path.getRemakePath();
    }

    public void increaseScore(Long step) {
        scores.add(step);
        visited.clear();
    }

    private void checkDeadlock() {
        if (visited.containsKey(pos)) {
            int n = visited.get(pos);
            if (n == 100)
                System.out.println(this + ": What have you done " + pos);
            if (n == 20)
                System.out.println(this + ": Almost certainly deadlock at " + pos);
            if (n == 10)
                System.out.println(this + ": Probable deadlock at " + pos);
            if (n == 5)
                System.out.println(this + ": Possible deadlock at " + pos);
        }
    }

    private void increaseVisited() {
        int n = visited.getOrDefault(pos, 0);
        n++;
        visited.put(pos, n);
    }

    @Override
    public void step(SimState state) {
        if (isMoving)
            return;
        desirePath = null;
        Int2D newPos;
        Warehouse warehouse = (Warehouse) state;
        if (path.getRemakePath())
            path = new Path();
        PathHandler pathHandler = new PathHandler(warehouse, this);
        Path newPath = null;
        if (target == null) {
            newPath = makeNoTargetPath(warehouse, pathHandler);
        } else if (path.isEmpty()) {
            newPath = makePath(warehouse, pathHandler);
            if (newPath == null) {
                addTag("stuck");
                path = makeNoTargetPath(warehouse, pathHandler);
            }
            else {
                removeTag("stuck");
            }
        }
        if (newPath != null) {
            path = newPath;
            if (debug)
                System.out.println(id + ": " + getPathList());
        }
        newPos = path.pop();
        // System.out.println ("pos: " + pos + " dir: " + dir + " target: " + target);
        if (newPos == null)
            newPos = pos;

        dir = newPos.subtract(pos);
        if (warehouse.move(this, newPos)) {
            isMoving = true;
            if (dir.x != 0 || dir.y != 0) timeToCompletedMovement = this.moveTime;
            else timeToCompletedMovement = 1;
            // path.setRemakePath(false);
        } else {
            path.setRemakePath(true);
            makeDesirePath(warehouse);
            addTag("stuck");
            // path = new Path(pos);
            if (debug) System.out.println(id + ": agent could not move to " + newPos + "(" + pos + ")");
            timeToCompletedMovement = 1;
        }
        increaseVisited();
        if (debug)
            if (this.target != null)
                checkDeadlock();
    }

    public abstract Path makeNoTargetPath(Warehouse warehouse, PathHandler pathHandler);

    public void makeTrail(Warehouse warehouse, Int2D pos) {
        Trail t = new Trail(this);
        trails.add(t);
        warehouse.addTrail(t, pos);
    }

    public void removeTrails() {
        moveComplete();
        trails = new ArrayList<>();
    }

    public int TimeToCompletedMovement() {
        this.timeToCompletedMovement = Math.max(0, this.timeToCompletedMovement - 1);
        return this.timeToCompletedMovement;
    }

    public abstract Path makePath(Warehouse warehouse, PathHandler pathHandler);

    public void makeDesirePath(Warehouse warehouse) {
        if (target == null) return;
        this.desirePath = new Path(PathFinding.aStar(warehouse, target, pos, size, true));
        if (debug)
            System.out.println("made new desire path");

    }

    public void moveComplete() {
        this.isMoving = false;
    }

    public AgentClone makeAgentClone() {
        AgentClone ag = new AgentClone(this);
        agentClones.add(ag);
        return ag;
    }

    public List<AgentClone> getAgentClones() {
        return this.agentClones;
    }

    public Int2D getAgentSize() {
        return size;
    }

    public Int2D getTarget() {
        return target;
    }

    public Int2D getPos() {
        return pos;
    }

    public int getScore() {
        return scores.size();
    }

    public List<Long> getScores() {
        return scores;
    }

    public int getDelay() {
        return this.timeToCompletedMovement;
    }

    public Int2D getDirection() {
        return this.dir;
    }

    public Path getPath() {
        return this.path;
    }

    public List<Int2D> getPathList() {
        return this.path.getList();
    }

    public List<Int2D> getDesirePathList() {
        if (this.desirePath == null) return null;
        else return this.desirePath.getList();
    }

    public List<Trail> getTrails() {
        return trails;
    }

    public String getId() {
        return id;
    }

    @Override
    public Color getColor() {
        return color;
    }

    public Brain getBrain() {
        return null;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String toString() {
        return id;
    }

    public void addTag(String s) {
        tags.add(s);
    }

    public void removeTag(String s) {
        tags.remove(s);
    }

    public boolean hasTag(String s) {
        return tags.contains(s);
    }

    public class Trail implements Colorable {
        Agent agent;

        public Trail(Agent a) {
            agent = a;
        }

        public void delete() {
            agent.moveComplete();
        }

        public Agent getAgent() {
            return agent;
        }

        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public class AgentClone implements Colorable {
        Agent agent;

        public AgentClone(Agent agent) {
            this.agent = agent;
        }

        public Agent getAgent() {
            return agent;
        }

        @Override
        public Color getColor() {
            return agent.getColor();
        }
    }
}
