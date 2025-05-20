package simulation.agents;

import java.util.List;

import sim.util.Int2D;
import simulation.Agent;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class AStarNoPathCollisionAgent extends Agent {

    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        List<Int2D> rawPath = PathFinding.aStarNoPathCollisions(warehouse, pathHandler, this.target, this.pos, this.size, 
                                                                  this.moveTime);
        
        if (rawPath.isEmpty()) {
            return null;
        }
        return new Path(rawPath);
    }

    @Override
    public Path makeNoTargetPath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path();
        path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, pathHandler, pos, size));
        return path;
    }
}
