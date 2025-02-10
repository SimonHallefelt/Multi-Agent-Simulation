package src;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Agent implements Steppable {
    int dx = 1;
    int dy = 0;
    int[] target = new int[2];
    PathFinding pf = new PathFinding();

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
    
}
