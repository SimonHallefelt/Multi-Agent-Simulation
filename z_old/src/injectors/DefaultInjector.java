package src.injectors;

import src.AgentFactory;
import src.agents.AStarAgent;
import src.agents.AStarNoPathCollisionAgent;
import src.agents.AStarSmarter;
import src.agents.PacmanAgent;
import src.agents.RandomAgent;
import src.interfaces.FactoryInjector;

public class DefaultInjector implements FactoryInjector {

    public void injectAgents() {
        AgentFactory.registerAgent("aStarSmart", () -> new AStarNoPathCollisionAgent());
        AgentFactory.registerAgent("aStarSmarter", () -> new AStarSmarter());
        AgentFactory.registerAgent("astar", () -> new AStarAgent());
        AgentFactory.registerAgent("pacman", () -> new PacmanAgent());
        AgentFactory.registerAgent("randomWalk", () -> new RandomAgent());
    }
    
}
