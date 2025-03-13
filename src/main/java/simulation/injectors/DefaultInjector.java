package simulation.injectors;

import simulation.AgentFactory;
import simulation.agents.AStarAgent;
import simulation.agents.AStarNoPathCollisionAgent;
import simulation.agents.AStarSmarter;
import simulation.agents.PacmanAgent;
import simulation.agents.RandomAgent;
import simulation.interfaces.FactoryInjector;

public class DefaultInjector implements FactoryInjector {

    public void injectAgents() {
        AgentFactory.registerAgent("aStarSmart", () -> new AStarNoPathCollisionAgent());
        AgentFactory.registerAgent("aStarSmarter", () -> new AStarSmarter());
        AgentFactory.registerAgent("astar", () -> new AStarAgent());
        AgentFactory.registerAgent("pacman", () -> new PacmanAgent());
        AgentFactory.registerAgent("randomWalk", () -> new RandomAgent());
    }
    
}
