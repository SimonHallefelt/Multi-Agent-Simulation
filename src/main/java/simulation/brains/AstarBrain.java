package simulation.brains;

import java.util.ArrayList;
import java.util.List;

import sim.util.Int2D;
import simulation.Agent;
import simulation.Brain;
import simulation.Path;
import simulation.PathFinding;
import simulation.Warehouse;

public class AstarBrain extends Brain {

    @Override
    public void think(Warehouse warehouse) {
        List<Agent> agents = warehouse.getAgentList();
        for (Agent a : agents) {
            if (a.getPathPositionPath().size() == 0) {
                ArrayList<Int2D> p = PathFinding.aStar(warehouse, a.getTarget(), a.getPos(), a.getSize());
                Path path = new Path(a.getPos());
                path.addNewPositionPath(a.getPos(), p);
                a.setPath(path);
            }
        }
    }

}
