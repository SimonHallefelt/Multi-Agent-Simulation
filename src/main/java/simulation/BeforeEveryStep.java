package simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

public class BeforeEveryStep implements Steppable {

    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        // System.out.println("Time to think!");
        for (Brain b : warehouse.BrainList) {
            // System.out.println("Thinking...");
            b.think(warehouse);
        }
        warehouse.generateTasks();
        warehouse.assignTasks();
    }

}
