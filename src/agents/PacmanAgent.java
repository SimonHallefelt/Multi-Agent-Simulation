package src.agents;

import src.Agent;
import src.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public void pickDirection(Warehouse warehouse) {
        int[] directions = pf.pacman(warehouse, this.targetx, this.targety, this.posx, this.posy);
        this.dx = directions[0];
        this.dy = directions[1];
    }
}
