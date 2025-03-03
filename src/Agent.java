package src;

import java.awt.Color;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public abstract class Agent implements Steppable, Colorable {
    public int score = 0;
    public Int2D pos = new Int2D(0,0);
    public Int2D dir = new Int2D(1,0);
    public Int2D target = new Int2D(0,0);
    protected int moveTime = 0;
    protected Boolean isMoving = false;
    protected ArrayList<AgentClone> agentClones = new ArrayList<>();
    protected ArrayList<Trail> trails = new ArrayList<>();
    protected Int2D size = new Int2D(1,1);
    protected Color color = Color.GRAY;
    protected Path path = new Path(target, null);
    private int timeToCompletedMovement;

    public void setColor(Color c) {
        color = c;
    }

    public void updatePosition(int x, int y) {
        this.pos = new Int2D(x,y);
    }

    public void setMoveTime(int moveTime) {
        this.moveTime = moveTime -1;
    }

    public void setSize(Int2D size) {
        this.size = size;
    }
    
    @Override
    public void step(SimState state) {
        if (isMoving) return;
        Warehouse warehouse = (Warehouse) state;
        dir = pickDirection(warehouse);
        // System.out.println ("pos: " + pos + " dir: " + dir + " target: " + target);
        
        if (warehouse.move(this, dir, size)) {
            pos = pos.add(dir);
            isMoving = true;
            timeToCompletedMovement = this.moveTime;
        } else {
            this.path = new Path(target, warehouse);
        }
    }

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
        return this.timeToCompletedMovement--;
    }

    public Int2D pickDirection(Warehouse warehouse) {
        return PathFinding.randomWalk(warehouse, dir);
    }

    public void setTarget(int x, int y) {
        this.target = new Int2D(x,y);
    }

    public void setTarget(Int2D i) {
        setTarget(i.x, i.y);
    }

    public void moveComplete() {
        this.isMoving = false;
    }

    public AgentClone makeAgentClone() {
        AgentClone ag = new AgentClone(this);
        agentClones.add(ag);
        return ag;
    }
    
    public ArrayList<AgentClone> getAgentClones() {
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
        return score;
    }

    public ArrayList<Trail> getTrails() {
        return trails;
    }

    @Override
    public Color getColor() {
        return color;
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

