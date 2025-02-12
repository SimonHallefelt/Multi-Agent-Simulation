package src;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable {
    public int score = 0;
    public Int2D pos = new Int2D(0,0);
    public Int2D delta = new Int2D(1,0);
    public Int2D target = new Int2D(0,0);
    protected int moveTime = 0;
    protected Int2D oldPos;
    protected PathFinding pf = new PathFinding();
    protected Boolean isMoving = false;


    public void updatePosition(int x, int y) {
        this.pos = new Int2D(x,y);
    }

    public void setMoveTime(int moveTime) {
        this.moveTime = moveTime -1;
    }
    
    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        delta = pickDirection(warehouse);
        System.out.println ("pos: " + pos + " delta: " + delta + " target: " + target);
        
        pickDirection(warehouse);
        if (!isMoving && warehouse.move(this, delta)) {
            oldPos = pos;
            pos = pos.add(delta);
            isMoving = true;
            warehouse.setOccupied(new Trail(this, this.moveTime, oldPos), oldPos.x, oldPos.y);
        }
    }


    public Int2D pickDirection(Warehouse warehouse) {
        return PathFinding.randomWalk(warehouse, this);
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
    
    public class Trail {
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
    }
}

