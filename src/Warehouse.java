package src;

import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;

public class Warehouse extends SimState {
    int height = 100;
    int width = 100;
    int num_agents = 3;
    public IntGrid2D map = new IntGrid2D(width, height);
    public SparseGrid2D agents = new SparseGrid2D(width, height);

    public Warehouse(long seed) {
        super(seed);
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return map.get(x, y) == 1;
    }

    public boolean isAgentPresent(int x, int y) {
        Bag bag = agents.getObjectsAtLocation(x, y);
        if(bag == null) return false;
        int bag_s = bag.size();
        return  bag_s > 0;
    }

    public boolean isOccupied(int x, int y) {
        return isWall(x, y) || isAgentPresent(x, y);
    }

    public boolean move(Agent a, int dx, int dy) {
        Int2D loc = agents.getObjectLocation(a);
        int x = loc.x + dx;
        int y = loc.y + dy;
        if (isOccupied(x, y)) return false;
        agents.setObjectLocation(a, x, y);
        return true;
    }

    public void start() {
        super.start();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int v = random.nextDouble() < 0.5 ? 1 : 0;
                map.set(x, y, v);
            }
        }
        for (int i = 0; i < num_agents; i++) {
            Agent a = new Agent();
            agents.setObjectLocation(a, i, i);
            schedule.scheduleRepeating(a);
        }
    }

    public static void main(String[] args) {
        doLoop(Agents.class, args);
        System.exit(0);
    }
}