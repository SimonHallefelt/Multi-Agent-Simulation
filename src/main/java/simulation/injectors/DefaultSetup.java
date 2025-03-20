package simulation.injectors;

import simulation.AgentFactory;
import simulation.BrainFactory;
import simulation.agents.AStarAgent;
import simulation.agents.AStarNoPathCollisionAgent;
import simulation.agents.AStarSmarter;
import simulation.agents.PacmanAgent;
import simulation.agents.RandomAgent;
import simulation.brains.AstarBrain;
import simulation.interfaces.FactorySetup;

public class DefaultSetup implements FactorySetup {

    public void injectAgents() {
        AgentFactory.registerAgent("aStarSmart", () -> new AStarNoPathCollisionAgent());
        AgentFactory.registerAgent("aStarSmarter", () -> new AStarSmarter());
        AgentFactory.registerAgent("astar", () -> new AStarAgent());
        AgentFactory.registerAgent("pacman", () -> new PacmanAgent());
        AgentFactory.registerAgent("randomWalk", () -> new RandomAgent());
        BrainFactory.registerBrain("astarBrain", () -> new AstarBrain());
    }

}
