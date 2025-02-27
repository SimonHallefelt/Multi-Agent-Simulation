package src;

import java.awt.Color;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable, Colorable {
    public int score = 0;
    public Int2D pos = new Int2D(0,0);
    public Int2D dir = new Int2D(1,0);
    public Int2D target = new Int2D(0,0);
    protected int moveTime = 0;
    protected Int2D oldPos;
    protected Boolean isMoving = false;
    protected ArrayList<AgentClone> agentClones = new ArrayList<>();
    protected Int2D size = new Int2D(1,1);
    protected Color color = Color.GRAY;

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
        Warehouse warehouse = (Warehouse) state;
        dir = pickDirection(warehouse);
        // System.out.println ("pos: " + pos + " dir: " + dir + " target: " + target);
        
        pickDirection(warehouse);
        if (!isMoving && warehouse.move(this, dir, size)) {
            oldPos = pos;
            pos = pos.add(dir);
            isMoving = true;
            makeTrail(warehouse, oldPos);
        }
    }

    public void makeTrail(Warehouse warehouse, Int2D pos) {
        warehouse.setOccupiedTrail(new Trail(this, this.moveTime, pos), pos);
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

    public void moveComplet() {
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
    
    public class Trail implements Colorable {
        int timeToCompletedMovement;
        Int2D trailPos;
        Agent agent;
        
        public Trail(Agent a, int ttcm, Int2D pos) {
            timeToCompletedMovement = ttcm;
            trailPos = pos;
            agent = a;
        }

        public int TimeToCompletedMovement() {
            return this.timeToCompletedMovement--;
        }

        public Int2D delate() {
            agent.moveComplet();
            return this.trailPos;
        }

        public Agent getAgent() {
            return agent;
        }

        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public class AgentClone implements Colorable {
        Agent agent;

        public AgentClone(Agent agent) {
            this.agent = agent;
        }

        public void makeTrail(Warehouse warehouse, Int2D pos) {
            agent.makeTrail(warehouse, pos);
        }

        public Agent getAgent() {
            return agent;
        }

        @Override
        public Color getColor() {
            return agent.getColor();
        }
    }

    @Override
    public Color getColor() {
        return color;
    }
}

