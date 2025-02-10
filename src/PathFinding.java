package src;

public class PathFinding {
    int[] dir = {1, 0};


    public int[] randomWalk(Warehouse warehouse) {
        double d = warehouse.random.nextDouble();
        boolean vert = (dir[0] == 0);
        if (d < 0.25) {
            if (vert) { dir[0] = 1; dir[1] = 0; }
            else      { dir[0] = 0; dir[1] = 1; }
        }
        else if (d < 0.5) {
            if (vert) { dir[0] = -1; dir[1] = 0; }
            else      { dir[0] = 0; dir[1] = -1; }
        }
        return dir;
    }

    public int[] pacman(Warehouse warehouse, int targetx, int targety, int x, int y) {
        int disx = Math.abs(targetx-x);
        int disy = Math.abs(targety-y);
        int tempx = dir[0];
        if ((!warehouse.isOccupied(x+dir[0], y+dir[1])) && 
        ((Math.abs(targetx-(x+dir[0])) < disx) || 
        (Math.abs(targety-(y+dir[1])) < disy))) {
            return dir;
        } else if ((!warehouse.isOccupied(x+dir[1], y+dir[0])) && 
        ((Math.abs(targetx-(x+dir[1])) < disx) || 
        (Math.abs(targety-(y+dir[0])) < disy))) {
            dir[0] = dir[1];
            dir[1] = tempx;
        } else if ((!warehouse.isOccupied(x-dir[1], y-dir[0])) && 
        ((Math.abs(targetx-(x-dir[1])) < disx) || 
        (Math.abs(targety-(y-dir[0])) < disy))) {
            dir[0] = -dir[1];
            dir[1] = -tempx;
        } else if (!warehouse.isOccupied(x+dir[0], y+dir[1])) {
            return dir;
        } else if (!warehouse.isOccupied(x+dir[1], y+dir[0])) {
            dir[0] = dir[1];
            dir[1] = tempx;
        } else if (!warehouse.isOccupied(x-dir[1], y-dir[0])) {
            dir[0] = -dir[1];
            dir[1] = -tempx;
        } else {
            dir[0] = -dir[0];
            dir[1] = -dir[1];
        }
        return dir;
    }

    public int getDX() {
        return this.dir[0];
    }

    public int getDY() {
        return this.dir[1];
    }
}
