package src;

import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;

public class Warehouse extends SimState {
    int height = 100;
    int width = 100;
    public IntGrid2D map = new IntGrid2D(width, height);

    public Warehouse(long seed) {
        super(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int v = random.nextDouble() < 0.5 ? 1 : 0;
                map.set(x, y, v);
            }
        }
    }

    public static void main(String[] args) {
        doLoop(Agents.class, args);
        System.exit(0);
    }
}