package src.agents;

import java.util.ArrayList;
import java.util.HashSet;

import sim.util.Int2D;
import sim.util.Int3D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class AStarNoPathCollisionAgent extends Agent {

    @Override
    public void makePath(Warehouse warehouse) {
        PathHandler othersPaths = new PathHandler(warehouse, this);
        ArrayList<Int2D> path = PathFinding.aStarNoPathCollisions(warehouse, this.target, this.pos, this.size, 
                                                                  this.moveTime, othersPaths);
        
        if (path.isEmpty()) {
            super.path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, this.pos, this.size));
            return;
        }
        super.path = new Path(target);
        super.path.addNewPositionPath(pos, path);
    }

    @Override
    public void noTarget(Warehouse warehouse) {
        path.addStep(PathFinding.randomUnobstructiveWalk(warehouse, pos, size));
    }
}
