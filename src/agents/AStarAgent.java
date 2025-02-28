package src.agents;

import sim.util.Int2D;
import src.Agent;
import src.PathFinding;
import src.Warehouse;

public class AStarAgent extends Agent {
    
    @Override
    public Int2D pickDirection(Warehouse warehouse) {
        return PathFinding.aStar(warehouse, this.target, this.pos, this.size);
    }
}
