package src.agents;

import src.Agent;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class RandomAgent extends Agent{
    
    @Override
    public boolean makePath(Warehouse warehouse, PathHandler pathHandler) {
        this.path.addStep(PathFinding.randomWalk(warehouse, this.dir));
        return true;
    }
}
