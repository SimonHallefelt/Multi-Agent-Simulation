package simulation.brains;

import simulation.Brain;
import simulation.Warehouse;

public class PriorityBrain extends Brain {

    @Override
    public void think(Warehouse warehouse) {
        System.out.println("I am thinking really hard");
    }
    
}
