package src;

import java.util.HashMap;

import sim.util.Int2D;
import src.agents.*;
import src.interfaces.AgentConstructor;

public class AgentFactory {
    int amount = 0;
    static HashMap<String,AgentConstructor> agents = new HashMap<>();

    public static void registerAgent(String name, AgentConstructor constructor) {
        agents.put(name, constructor);
    }

    public Agent createAgent(String id, int x, int y, String algo, int moveTime, Int2D size) {
        amount++;
        Agent a;
        if (agents.containsKey(id)) {
            a = agents.get(id).createAgent();
        }
        switch (algo) {
            case "aStarSmart":
                a = new AStarNoPathCollisionAgent();
                break;
            case "astar":
                a = new AStarAgent();
                break;
            case "pacman":
                a = new PacmanAgent();
                break;
            case "randomWalk":
                a =  new RandomAgent();
                break;
            default:
                a =  new RandomAgent();
                break;
        }
        a.setId("agent-" + algo + "-" + id + "-" + amount);
        a.setPosition(x, y);
        a.setMoveTime(moveTime);
        a.setSize(size);
        return a;
    }
}
