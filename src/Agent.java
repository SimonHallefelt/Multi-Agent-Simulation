package src;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable {
    int x;
    int y;
    int dx = 1;
    int dy = 0;
    int[] target = new int[2];
    PathFinding pf = new PathFinding();

    public void updatePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        pickDirection(warehouse);
        warehouse.move(this, dx, dy);
    }



    public void pickDirection(Warehouse warehouse) {
        pf.pickDirection(warehouse);
        this.dx = pf.getDX();
        this.dy = pf.getDY();
    }

    public void setTarget(int y, int x) {
        this.target[0] = y;
        this.target[1] = x;
    }

    public void setTarget(Int2D i) {
        setTarget(i.x, i.y);
    }
    
}
