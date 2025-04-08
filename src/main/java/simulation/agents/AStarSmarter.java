package simulation.agents;

import java.util.ArrayList;
import java.util.List;

import sim.util.Int2D;
import simulation.Agent;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class AStarSmarter extends Agent {

    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        List<Int2D> rawPath = PathFinding.aStarNoPathCollisions(warehouse, pathHandler, this.target, this.pos, this.size, 
                                                                  this.moveTime);
        
        if (rawPath.isEmpty()) {
            // super.path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, this.pos, this.size, pathHandler));
            return null;
        }
        return new Path(rawPath);
    }

    @Override
    public Path makeNoTargetPath(Warehouse warehouse, PathHandler pathHandler) {
        List<Int2D> rawPath = PathFinding.moveOutOfWay(warehouse, pathHandler, pos, size, moveTime);
        if (rawPath.size() > 0) return new Path(rawPath);
        else return new Path(PathFinding.randomUnobstructiveWalk(warehouse, pathHandler, pos, size));
        //path.setRemakePath(true);
    }
}
