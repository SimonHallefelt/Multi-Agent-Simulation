package src;

import sim.engine.*;

public class Agents extends SimState {
    public Agents(long seed) {
        super(seed);
    }
    public static void main(String[] args) {
        doLoop(Agents.class, args);
        System.exit(0);
    }
}
