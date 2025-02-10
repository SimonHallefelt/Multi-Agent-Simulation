package src;

public class PathFinding {
    int dx = 1;
    int dy = 0;


    public void pickDirection(Warehouse warehouse) {
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

    public int getDX() {
        return this.dx;
    }

    public int getDY() {
        return this.dy;
    }
}
