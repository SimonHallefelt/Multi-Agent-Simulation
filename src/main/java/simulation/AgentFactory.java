package simulation;

import java.util.HashMap;

import sim.util.Int2D;
import simulation.agents.*;
import simulation.interfaces.AgentConstructor;

public class AgentFactory {
    int amount = 0;
    static HashMap<String,AgentConstructor> agents = new HashMap<>();

    public static void registerAgent(String name, AgentConstructor constructor) {
        agents.put(name, constructor);
    }

    public Agent createAgent(String id, int x, int y, String algo, int moveTime, Int2D size) {
        return createAgent(id, x, y, algo, moveTime, size, amount);
    }
    public Agent createAgent(String id, int x, int y, String algo, int moveTime, Int2D size, int agentNumber) {
        amount++;
        Agent a;
        if (agents.containsKey(algo)) {
            a = agents.get(algo).createAgent();
        }
        else {
            System.out.println("WARING: unknown agent type " + algo + "!");
            a =  new RandomAgent();
        }
        a.setId("agent-" + algo + "-" + id + "-" + agentNumber);
        a.setPosition(x, y);
        a.setMoveTime(moveTime);
        a.setSize(size);
        return a;
    }
}
