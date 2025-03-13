package src.agents;

import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class RandomAgent extends Agent{
    
    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        path.addStep(PathFinding.randomWalk(warehouse, this.dir));
        return path;
    }
}
