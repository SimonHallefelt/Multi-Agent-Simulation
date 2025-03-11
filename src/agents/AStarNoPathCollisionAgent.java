package src.agents;

import java.util.ArrayList;

import sim.util.Int2D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class AStarNoPathCollisionAgent extends Agent {

    @Override
    public boolean makePath(Warehouse warehouse, PathHandler pathHandler) {
        ArrayList<Int2D> path = PathFinding.aStarNoPathCollisions(warehouse, this.target, this.pos, this.size, 
                                                                  this.moveTime, pathHandler);
        
        if (path.isEmpty()) {
            // super.path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, this.pos, this.size, pathHandler));
            return false;
        }
        super.path.addNewPositionPath(pos, path);
        return true;
    }

    @Override
    public void noTarget(Warehouse warehouse, PathHandler pathHandler) {
        path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, pos, size, pathHandler));
    }
}
