package src.agents;

import src.Agent;
import src.Warehouse;

public class RandomAgent extends Agent{
    
    @Override
    public void pickDirection(Warehouse warehouse) {
        pf.randomWalk(warehouse);
        this.dx = pf.getDX();
        this.dy = pf.getDY();
    }
}
