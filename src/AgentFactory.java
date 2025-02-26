package src;

import sim.util.Int2D;
import src.agents.*;

public class AgentFactory {

    
    public AgentFactory() {

    }

    public Agent createAgent(int x, int y, String algo, int moveTime, Int2D size) {
        Agent a;
        switch (algo) {
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
        a.updatePosition(x, y);
        a.setMoveTime(moveTime);
        a.setSize(size);
        return a;
    }
}
