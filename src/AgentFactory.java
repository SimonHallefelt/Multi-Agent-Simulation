package src;

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
                a = new Agent();
                break;
            default:
                a =  new Agent();
                break;
        }
        a.updatePosition(x, y);
        return a;
    }
}
