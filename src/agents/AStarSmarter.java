package src.agents;

import java.util.ArrayList;

import sim.util.Int2D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class AStarSmarter extends Agent {

    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        ArrayList<Int2D> rawPath = PathFinding.aStarNoPathCollisions(warehouse, pathHandler, this.target, this.pos, this.size, 
                                                                  this.moveTime);
        
        if (rawPath.isEmpty()) {
            // super.path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, this.pos, this.size, pathHandler));
            return null;
        }
        path.addNewPositionPath(pos, rawPath);
        return path;
    }

    @Override
    public Path noTarget(Warehouse warehouse, PathHandler pathHandler) {
        ArrayList<Int2D> rawPath = PathFinding.moveOutOfWay(warehouse, pathHandler, pos, size, moveTime);
        Path path = new Path(pos);
        if (rawPath.size() > 0) path.addNewPositionPath(pos, rawPath);
        else path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, pos, size, pathHandler));
        return path;
    }
}
