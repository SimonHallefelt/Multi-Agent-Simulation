package src;

import java.util.ArrayList;
import java.util.HashMap;

import sim.util.Int2D;

public class Tasks {
    private Warehouse warehouse;
    private HashMap<Agent, ArrayList<Task>> tasks = new HashMap<>();

    public Tasks(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void reachedTarget(Agent a, Int2D pos) {
        ArrayList<Task> goals = tasks.get(a);
        if (goals != null) {
            Task goal = goals.get(0);
            if (goal.reached(pos.x, pos.y , a.getAgentSize())) {
                a.increaseScore();
                warehouse.increaseScore();
                assignNextTask(a);
            }
        }
    }

    public void assignNextTask(Agent a) {
        ArrayList<Task> agentTasks = tasks.get(a);
        if (agentTasks == null) {
            System.out.println("Agent " + a + " does not have any tasks");
            return;
        }
        Task current = agentTasks.get(0);
        if (current.progress()) {
            agentTasks.remove(0);
            if (agentTasks.size() == 0) {
                System.out.println("Agent " + a + " ran out of tasks");
                tasks.remove(a);
                return;
            }
            current = agentTasks.get(0);
        }
        a.setTarget(current.getGoal());
    }

    public void assignTask(ArrayList<Int2D> starts, ArrayList<Int2D> goals, ArrayList<Agent> AgentList) {
        int startSize = starts.size();
        int goalSize = goals.size();
        Int2D start = starts.get(warehouse.random.nextInt(startSize));
        Int2D goal = goals.get(warehouse.random.nextInt(goalSize));
        Task t = new Task(start, goal);
        ArrayList<Agent> viableAgents = (ArrayList<Agent>) AgentList.clone();
        viableAgents.removeIf(a -> !canPerform(a, t));
        viableAgents.sort((a,b) -> timeToReach(a, t) - timeToReach(b, t));
        //System.out.println(start + " " + goal);
        if (viableAgents.size() == 0) return;
        Agent a = viableAgents.get(0);
        ArrayList<Task> assigned = tasks.get(a);
        if (assigned != null) {
            assigned.add(t);
        }
        else {
            assigned = new ArrayList<>();
            assigned.add(t);
            tasks.put(a, assigned);
            a.setTarget(t.getGoal());
        }
        //System.out.println("Assigned task to " + a + ", fitness: " + timeToReach(a, t));
    }

    public boolean canPerform(Agent a, Task t) {
        Int2D next;
        ArrayList<Int2D> path = PathFinding.aStar(warehouse, t.start, a.pos, a.size, true);
        if (  !t.start.equals(a.pos)) {
            next = path.get(0).subtract(a.pos);
            if (next.x == 0 && next.y == 0) return false;
        }
        Int2D startPos = path.get(path.size()-1);
        next = PathFinding.aStar(warehouse, t.finish, startPos, a.size, true).get(0).subtract(startPos);
        if (next.x == 0 && next.y == 0) return false;
        return true;
    }

    public int timeToReach(Agent a, Task t) {
        int TTR = 0;
        Int2D current = a.pos;
        ArrayList<Task> agentTasks = tasks.get(a);
        if (agentTasks != null) {
            for (Task ts: agentTasks) {
                TTR += ts.getCompletionDistance(current, a.size);
                current = ts.finish;
            }
        }
        TTR += PathFinding.getDistance(current, t.start, a.size);
        return TTR;
    } 

    private class Task {
        public Int2D start, finish;
        public boolean started = false;
        public Task(Int2D start, Int2D finish) {
            this.start = start;
            this.finish = finish;
        }

        public Int2D getGoal() {
            if (started) return finish;
            else return start;
        }

        public boolean reached(int x, int y, Int2D size) {
            if (started) return finish.x >= x && finish.x < x + size.x && finish.y >= y && finish.y < y + size.y;
            else return start.x >= x && start.x < x + size.x && start.y >= y && start.y < y + size.y;
        }

        public boolean progress() {
            if (started) return true;
            started = true;
            return false;
        }

        public int getCompletionDistance(Int2D from, Int2D size) {
            if (!started) return PathFinding.getDistance(from, start, size) + PathFinding.getDistance(start, finish, size);
            else return PathFinding.getDistance(from, finish, size);
        }
    }
}
