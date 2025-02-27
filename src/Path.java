package src;

import java.util.ArrayList;

import sim.util.Int2D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private Int2D tail;
    private Warehouse warehouse;

    public Path(Int2D start, Warehouse wh) {
        tail = start;
        warehouse = wh;
    }

    public boolean addStep (Int2D dir) {
        Int2D target = tail.add(dir);
        if (!warehouse.isWall(target)) {
            steps.add(dir);
            tail = target;
            return true;
        }
        return false;
    }

    public Int2D pop() {
        Int2D next = steps.get(0);
        steps.remove(0);
        return next;
    }
}
