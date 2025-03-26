package simulation.agents;

import simulation.Agent;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class RandomAgent extends Agent {

    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        path.addPos(PathFinding.randomWalk(warehouse, pos, dir));
        return path;
    }

    public Path noTarget(Warehouse warehouse, PathHandler pathHandler) {
        return makePath(warehouse, pathHandler);
    }
}
