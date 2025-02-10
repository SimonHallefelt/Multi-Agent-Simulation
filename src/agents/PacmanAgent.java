package src.agents;

import src.Agent;
import src.Warehouse;

public class PacmanAgent extends Agent {
    
    @Override
    public void pickDirection(Warehouse warehouse) {
        pf.pacman(warehouse, this.targetx, this.targety, this.posx, this.posy);
        this.dx = pf.getDX();
        this.dy = pf.getDY();
    }
}
