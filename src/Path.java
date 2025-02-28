package src;

import java.util.ArrayList;

import sim.util.Int2D;

public class Path {
    private ArrayList<Int2D> steps = new ArrayList<>();
    private ArrayList<Int2D> positionPath = new ArrayList<>();
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

    public void addNewPositionPath(Int2D pos, ArrayList<Int2D> positionPath) {
        this.positionPath = positionPath;
        generateStepsFromPositionPath(pos);
    }

    private void generateStepsFromPositionPath(Int2D pos) {
        steps = new ArrayList<>();
        Int2D nextPos;
        for (int i = 0; i < positionPath.size(); i++) {
            nextPos = this.positionPath.get(i);
            steps.add(nextPos.subtract(pos));
            pos = nextPos;
        }
    }

    public Int2D pop() {
        if (steps.isEmpty()) return null;
        Int2D next = steps.get(0);
        steps.remove(0);
        positionPath.remove(0);
        return next;
    }
}
