package simulation.setup;

import simulation.agents.AStarAgent;
import simulation.agents.AStarNoPathCollisionAgent;
import simulation.agents.AStarSmarter;
import simulation.agents.PacmanAgent;
import simulation.agents.RandomAgent;
import simulation.brains.AstarBrain;
import simulation.interfaces.FactorySetup;

public class DefaultSetup extends FactorySetup {

    public void injectAgents() {
        registerAgent("aStarSmart", () -> new AStarNoPathCollisionAgent());
        registerAgent("aStarSmarter", () -> new AStarSmarter());
        registerAgent("astar", () -> new AStarAgent());
        registerAgent("pacman", () -> new PacmanAgent());
        registerAgent("RandomWalk", () -> new RandomAgent());
        registerBrain("astarBrain", () -> new AstarBrain());
    }

}
