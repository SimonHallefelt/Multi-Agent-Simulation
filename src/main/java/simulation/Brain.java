package simulation;

import java.util.ArrayList;

public abstract class Brain {
    private ArrayList<Agent> agents = new ArrayList<>();

    public void addAgent(Agent a) {
        agents.add(a);
    }

    public boolean equals(Object other) {
        return this.getClass().equals(other.getClass());
    }

    public abstract void think(Warehouse warehouse);
}
