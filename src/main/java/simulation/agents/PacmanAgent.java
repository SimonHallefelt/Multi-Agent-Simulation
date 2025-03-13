package simulation.agents;


import simulation.Agent;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public Path makePath(Warehouse warehouse, PathHandler pathHandler) {
        Path path = new Path(pos);
        path.addStep(PathFinding.pacman(warehouse, this.pos, this.target, this.dir, this.size));
        return path;
    }
}
