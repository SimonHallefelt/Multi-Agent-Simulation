package src.agents;

import sim.util.Int2D;
import src.Agent;
import src.PathFinding;
import src.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public void makePath(Warehouse warehouse) {
        this.path.addStep(PathFinding.pacman(warehouse, this.pos, this.target, this.dir, this.size));
    }
}
