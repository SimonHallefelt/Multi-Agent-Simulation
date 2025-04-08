package simulation.agents;

import simulation.Agent;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class RandomAgent extends Agent {

    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        return new Path(PathFinding.randomWalk(warehouse, pos, dir));
    }

    public Path makeNoTargetPath(Warehouse warehouse, PathHandler pathHandler) {
        return makePath(warehouse, pathHandler);
    }
}
