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
    public boolean makePath(Warehouse warehouse, PathHandler pathHandler) {
        ArrayList<Int2D> path = PathFinding.aStar(warehouse, this.target, this.pos, this.size);
        if (path.get(0).equals(pos)) {
            super.path.addStep(PathFinding.randomWalk(warehouse, dir));
            return false;
        }
        super.path = new Path(pos);
        super.path.addNewPositionPath(pos, path);
        return true;
    }
}
