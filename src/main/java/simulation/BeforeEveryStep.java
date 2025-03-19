package simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

public class BeforeEveryStep implements Steppable {

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        for (Brain b : warehouse.BrainList) {
            b.think(warehouse);
        }
    }

}
