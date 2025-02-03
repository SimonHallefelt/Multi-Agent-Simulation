package src;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Agent implements Steppable {

    int dx = 1;
    int dy = 0;

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        pickDirection(warehouse);
        warehouse.move(this, dx, dy);
    }

    public void pickDirection(Warehouse warehouse) {
        double d = warehouse.random.nextDouble();
        boolean vert = (dx == 0);
        if (d < 0.25) {
            if (vert) { dx = 1; dy = 0; }
            else      { dx = 0; dy = 1; }
        }
        else if (d < 0.5) {
            if (vert) { dx = -1; dy = 0; }
            else      { dx = 0; dy = -1; }
        }
    }
    
}
