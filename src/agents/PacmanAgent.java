package src.agents;

import src.Agent;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public boolean makePath(Warehouse warehouse, PathHandler pathHandler) {
        this.path.addStep(PathFinding.pacman(warehouse, this.pos, this.target, this.dir, this.size));
        return true;
    }
}
