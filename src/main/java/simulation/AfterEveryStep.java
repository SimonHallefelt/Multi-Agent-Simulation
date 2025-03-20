package simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

public class AfterEveryStep implements Steppable {

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        warehouse.clearTrails();
    }
    
}
