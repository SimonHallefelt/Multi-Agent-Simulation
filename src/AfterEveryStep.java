package src;

import sim.engine.SimState;
import sim.engine.Steppable;

public class AfterEveryStep implements Steppable {
    int i = 0;

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        warehouse.clearTrails();
        System.out.println("AfterEveryStep " + i++);
    }
    
}
