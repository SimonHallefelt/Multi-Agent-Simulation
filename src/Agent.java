package src;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable {
    protected int posx;
    protected int posy;
    protected int dx = 1;
    protected int dy = 0;
    protected int targetx = 0;
    protected int targety = 0;
    protected PathFinding pf = new PathFinding();

    public void updatePosition(int x, int y) {
        this.posx = x;
        this.posy = y;
    }
    
    @Override
    public void step(SimState state) {
        Warehouse warehouse = (Warehouse) state;
        pickDirection(warehouse);
        if (warehouse.move(this, dx, dy)) {
            this.posx += this.dx;
            this.posy += this.dy;
        }
    }



    public void pickDirection(Warehouse warehouse) {
        pf.randomWalk(warehouse);
        // pf.pacman(warehouse, this.targetx, this.targety, this.posx, this.posy);
        this.dx = pf.getDX();
        this.dy = pf.getDY();
    }

    public void setTarget(int x, int y) {
        this.targetx = x;
        this.targety = y;
    }

    public void setTarget(Int2D i) {
        setTarget(i.x, i.y);
    }
    
}
