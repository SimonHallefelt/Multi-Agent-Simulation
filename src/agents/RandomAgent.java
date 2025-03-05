package src.agents;

import sim.util.Int2D;
import src.Agent;
import src.PathFinding;
import src.Warehouse;

public class RandomAgent extends Agent{
    
    @Override
    public void makePath(Warehouse warehouse) {
        this.path.addStep(PathFinding.randomWalk(warehouse, this.dir));
    }
}
