package src;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Agent implements Steppable {

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        if (warehouse.move(this, 1, 0)) return;
        if (warehouse.move(this, 0, 1)) return;
        if (warehouse.move(this, -1, 0)) return;
        if (warehouse.move(this, 0, -1)) return;
    }
    
}
