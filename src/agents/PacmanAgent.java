package src.agents;


import src.Agent;
import src.Path;
import src.PathFinding;
import src.PathHandler;
import src.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        path.addStep(PathFinding.pacman(warehouse, this.pos, this.target, this.dir, this.size));
        return path;
    }
}
