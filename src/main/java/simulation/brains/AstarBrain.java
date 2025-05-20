package simulation.brains;

import java.util.List;

import sim.util.Int2D;
import simulation.Agent;
import simulation.Brain;
import simulation.Path;
import simulation.PathFinding;
import simulation.PathHandler;
import simulation.Warehouse;

public class AstarBrain extends Brain {

    @Override
    public void think(Warehouse warehouse) {
        List<Agent> agents = warehouse.getAgentList();
        PathHandler ph = new PathHandler(warehouse);
        for (Agent a : agents) {
            if (a.getPathList().size() == 0 && a.getTarget() != null) {
                ph.removeAgent(a);
                List<Int2D> p = PathFinding.aStarNoPathCollisions(warehouse, ph, a.getTarget(), a.getPos(),
                        a.getSize(), a.getMoveTime());
                // ArrayList<Int2D> p = PathFinding.aStar(warehouse, a.getTarget(), a.getPos(),
                // a.getSize());
                if (p.size() == 0) {
                    p = PathFinding.moveOutOfWay(warehouse, ph, a.getPos(), a.getSize(), a.getMoveTime());
                }
                Path path = new Path(p);
                a.setPath(path);
                ph.addAgentPath(a, path);
            }
        }
    }

}
