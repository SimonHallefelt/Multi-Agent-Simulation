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
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        ArrayList<Int2D> rawPath = PathFinding.aStarNoPathCollisions(warehouse, this.target, this.pos, this.size, 
                                                                  this.moveTime, pathHandler);
        
        if (rawPath.isEmpty()) {
            // super.path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, this.pos, this.size, pathHandler));
            return null;
        }
        Path path = new Path(pos);
        path.addNewPositionPath(pos, rawPath);
        return path;
    }

    @Override
    public Path noTarget(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, pos, size, pathHandler));
        return path;
    }
}
