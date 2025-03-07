package src;

import sim.util.Int2D;
import src.agents.*;

public class AgentFactory {
    int amount = 0;

    
    public AgentFactory() {

    }

    public Agent createAgent(String id, int x, int y, String algo, int moveTime, Int2D size) {
        amount++;
        Agent a;
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
        a.setId("agent-" + id + "-" + amount);
        a.setPosition(x, y);
        a.setMoveTime(moveTime);
        a.setSize(size);
        return a;
    }
}
