package src;

import sim.util.Int2D;

public class PathFinding {

    public static Int2D randomWalk(Warehouse warehouse, Agent a) {
        double d = warehouse.random.nextDouble();
        Int2D dir = a.delta;
        boolean vert = (dir.x == 0);
        if (d < 0.25) {
            if (vert) dir = new Int2D(1,0); 
            else      dir = new Int2D(0,1);
        }
        else if (d < 0.5) {
            if (vert) dir = new Int2D(-1,0); 
            else      dir = new Int2D(0,-1);
        }
        return dir;
    }

    public static Int2D pacman(Warehouse warehouse, Agent a) { //int target.x, int target.y, int x, int y) {
        Int2D target, pos, dir;
        target = a.getTarget();
        pos = a.pos;
        dir = a.delta;
        int disx = Math.abs(target.x-pos.x);
        int disy = Math.abs(target.y-pos.y);
        int tempx = dir.x;
        if (warehouse.canMove(pos.x+dir.x, pos.y+dir.y, new Int2D(dir.x,dir.y), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x+dir.x)) < disx) || 
        (Math.abs(target.y-(pos.y+dir.y)) < disy))) {
            return dir;
        } else if (warehouse.canMove(pos.x+dir.y, pos.y+dir.x, new Int2D(dir.y,dir.x), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x+dir.y)) < disx) || 
        (Math.abs(target.y-(pos.y+dir.x)) < disy))) {
            return new Int2D(dir.y,tempx);
        } else if (warehouse.canMove(pos.x-dir.y, pos.y-dir.x, new Int2D(-dir.y,-dir.x), a.getAgentSize()) && 
        ((Math.abs(target.x-(pos.x-dir.y)) < disx) || 
        (Math.abs(target.y-(pos.y-dir.x)) < disy))) {
            return new Int2D(-dir.y,-tempx);
        } else if (warehouse.canMove(pos.x+dir.x, pos.y+dir.y, new Int2D(dir.x,dir.y), a.getAgentSize())) {
            return dir;
        } else if (warehouse.canMove(pos.x+dir.y, pos.y+dir.x, new Int2D(dir.y,dir.x), a.getAgentSize())) {
            return new Int2D(dir.y,tempx);
        } else if (warehouse.canMove(pos.x-dir.y, pos.y-dir.x, new Int2D(-dir.y,-dir.x), a.getAgentSize())) {
            return new Int2D(-dir.y,-tempx);
        } else {
            return new Int2D(-dir.x,-dir.y);
        }
    }
}
