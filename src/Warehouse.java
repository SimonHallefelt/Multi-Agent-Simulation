package src;

import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;

public class Warehouse extends SimState {
    int height = 100;
    int width = 100;
    public IntGrid2D map = new IntGrid2D(width, height);
    public SparseGrid2D agents = new SparseGrid2D(width, height);

    public Warehouse(long seed) {
        super(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int v = random.nextDouble() < 0.5 ? 1 : 0;
                map.set(x, y, v);
            }
        }
    }

    public boolean isWall(int x, int y) {
        return map.get(x, y) == 1;
    }

    public boolean isAgentPresent(int x, int y) {
        return agents.getObjectsAtLocation(x, y).size() > 0;
    }

    public boolean isOccupied(int x, int y) {
        return isWall(x, y) || isAgentPresent(x, y);
    }

    public static void main(String[] args) {
        doLoop(Agents.class, args);
        System.exit(0);
    }
}