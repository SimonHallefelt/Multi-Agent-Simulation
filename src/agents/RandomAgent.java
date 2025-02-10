package src.agents;

import src.Agent;
import src.Warehouse;

public class RandomAgent extends Agent{
    
    @Override
    public void pickDirection(Warehouse warehouse) {
        int[] directions = pf.randomWalk(warehouse);
        this.dx = directions[0];
        this.dy = directions[1];
    }
}
