package simulation.brains;

import java.util.ArrayList;
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
            if (a.getPathPositionPath().size() == 0 && a.getTarget() != null) {
                ph.removeAgent(a);
                // ArrayList<Int2D> p = PathFinding.aStarNoPathCollisions(warehouse, ph,
                // a.getTarget(), a.getPos(),
                // a.getSize(), a.getMoveTime());
                ArrayList<Int2D> p = PathFinding.aStar(warehouse, a.getTarget(), a.getPos(), a.getSize());
                Path path = new Path(a.getPos());
                path.addNewPositionPath(a.getPos(), p);
                a.setPath(path);
                ph.addAgentPath(a, path);
                System.out.println("New path for " + a + ": " + p);
            } else {
                System.out.println("Did not make path for " + a + ", " + a.getPathPositionPath().size());
            }
        }
    }

}
