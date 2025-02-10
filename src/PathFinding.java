package src;

public class PathFinding {
    int dx = 1;
    int dy = 0;


    public void randomWalk(Warehouse warehouse) {
        double d = warehouse.random.nextDouble();
        boolean vert = (dx == 0);
        if (d < 0.25) {
            if (vert) { dx = 1; dy = 0; }
            else      { dx = 0; dy = 1; }
        }
        else if (d < 0.5) {
            if (vert) { dx = -1; dy = 0; }
            else      { dx = 0; dy = -1; }
        }
    }

    public void pacman(Warehouse warehouse, int targetx, int targety, int x, int y) {
        int disx = Math.abs(targetx-x);
        int disy = Math.abs(targety-y);
        int tempx = dx;
        if ((!warehouse.isOccupied(x+dx, y+dy)) && 
        ((Math.abs(targetx-(x+dx)) < disx) || 
        (Math.abs(targety-(y+dy)) < disy))) {
            return;
        } else if ((!warehouse.isOccupied(x+dy, y+dx)) && 
        ((Math.abs(targetx-(x+dy)) < disx) || 
        (Math.abs(targety-(y+dx)) < disy))) {
            dx = dy;
            dy = tempx;
        } else if ((!warehouse.isOccupied(x-dy, y-dx)) && 
        ((Math.abs(targetx-(x-dy)) < disx) || 
        (Math.abs(targety-(y-dx)) < disy))) {
            dx = -dy;
            dy = -tempx;
        } else if (!warehouse.isOccupied(x+dx, y+dy)) {
            return;
        } else if (!warehouse.isOccupied(x+dy, y+dx)) {
            dx = dy;
            dy = tempx;
        } else if (!warehouse.isOccupied(x-dy, y-dx)) {
            dx = -dy;
            dy = -tempx;
        } else {
            dx = -dx;
            dy = -dy;
        }
    }

    public int getDX() {
        return this.dx;
    }

    public int getDY() {
        return this.dy;
    }
}
