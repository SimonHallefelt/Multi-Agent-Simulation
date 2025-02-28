package src.agents;

import java.util.ArrayList;

import sim.util.Int2D;
import src.Agent;
import src.Path;
import src.PathFinding;
import src.Warehouse;

public class AStarAgent extends Agent {
    
    @Override
    public Int2D pickDirection(Warehouse warehouse) {
        Int2D step = super.path.pop();
        if (step != null) return step;
        
        ArrayList<Int2D> path = PathFinding.aStar(warehouse, this.target, this.pos, this.size);
        if (path.get(0).equals(pos)) {
            path.set(0, path.get(0).add(PathFinding.randomWalk(warehouse, dir)));
        }
        super.path = new Path(target, warehouse);
        super.path.addNewPositionPath(pos, path);
        return super.path.pop();
    }
}
