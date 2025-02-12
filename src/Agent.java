package src;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable {
    public int score = 0;
    public Int2D pos = new Int2D(0,0);
    public Int2D delta = new Int2D(1,0);
    public Int2D target = new Int2D(0,0);

    public void updatePosition(int x, int y) {
        this.pos = new Int2D(x,y);
    }
    
    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        delta = pickDirection(warehouse);
        //System.out.println ("pos: " + pos + " delta: " + delta + " target: " + target);
        if (warehouse.move(this, delta)) {
            pos = pos.add(delta);
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
    
}
