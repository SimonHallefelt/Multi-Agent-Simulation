package src.agents;

import java.util.ArrayList;

import sim.util.Int2D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class AStarAgent extends Agent {
    
    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        ArrayList<Int2D> rawPath = PathFinding.aStar(warehouse, this.target, this.pos, this.size);
        if (rawPath.isEmpty()) {
            return null;
        }
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
