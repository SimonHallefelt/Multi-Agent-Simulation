package src.agents;

import java.util.ArrayList;
import java.util.HashSet;

import sim.util.Int2D;
import sim.util.Int3D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.Warehouse;

public class AStarNoPathCollisionAgent extends Agent {
    @Override
    public Int2D pickDirection(Warehouse warehouse) {
        Int2D step = super.path.pop();
        if (step != null) return step;
        System.err.println("\ncould not move, step: " + step);

        HashSet<Int3D> othersPaths = warehouse.getPathSet(this);
        System.err.println("\n" + this + " : \n" + othersPaths);
        ArrayList<Int2D> path = PathFinding.aStarNoPathCollisions(warehouse, this.target, this.pos, this.size, 
                                                                  this.moveTime, othersPaths);
        if (path.isEmpty()) {
            path.add(this.pos.add(PathFinding.randomWalk(warehouse, dir)));
        }
        super.path = new Path(target, warehouse);
        super.path.addNewPositionPath(pos, path);
        return super.path.pop();
    }
}
