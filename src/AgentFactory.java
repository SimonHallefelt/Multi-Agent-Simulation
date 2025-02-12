package src;

import src.agents.*;

public class AgentFactory {

    
    public AgentFactory() {

    }

    public Agent createAgent(int x, int y, String algo) {
        Agent a;
        switch (algo) {
            case "astar":
                a = new Agent();
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
        a.updatePosition(x, y);
        return a;
    }
}
