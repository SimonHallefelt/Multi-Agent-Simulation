package simulation;

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
            if (goal.reached(pos, a.getAgentSize())) {
                a.increaseScore();
                warehouse.increaseScore();
                a.setTarget(null);
                assignNextTask(a);
            }
        }
    }

    public void assignNextTask(Agent a) {
        ArrayList<Task> agentTasks = tasks.get(a);
        if (agentTasks == null) {
            //System.out.println("Agent " + a + " does not have any tasks");
            return;
        }
        Task current = agentTasks.get(0);
        if (current.complete()) {
            agentTasks.remove(0);
            if (agentTasks.isEmpty()) {
                //System.out.println("Agent " + a + " ran out of tasks");
                tasks.remove(a);
                return;
            }
            current = agentTasks.get(0);
        }
        a.setTarget(current.getGoal());
        a.makeInitialPath(warehouse);
    }

    public void assignTask(ArrayList<Int2D> starts, ArrayList<Int2D> goals, ArrayList<Agent> AgentList) {
        Task t = generateTask(starts, goals, AgentList);
        @SuppressWarnings("unchecked")
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
        a.makeInitialPath(warehouse);
        //System.out.println("Assigned task to " + a + ", fitness: " + timeToReach(a, t));
    }

    public Task generateTask(ArrayList<Int2D> starts, ArrayList<Int2D> goals, ArrayList<Agent> AgentList) {
        Int2D[] targets = new Int2D[] {
            starts.get(warehouse.random.nextInt(starts.size())),
            goals.get(warehouse.random.nextInt(goals.size()))
        };
        return new Task(targets);
    }

    public boolean canPerform(Agent a, Task t) {
        Int2D startPos = a.pos;
        ArrayList<Int2D> path;
        for (Int2D target : t.getTargets()) {
            if (!reached(startPos, a.size, target)) {
                path = PathFinding.aStar(warehouse, target, startPos, a.size, true);
                if (path.isEmpty()) return false;
                startPos = path.get(path.size()-1);
            }
        }
        return true;
    }

    public int timeToReach(Agent a, Task t) {
        int TTR = 0;
        Int2D startPos = a.pos;
        ArrayList<Task> agentTasks = tasks.get(a);
        if (agentTasks != null) {
            for (Task ts: agentTasks) {
                TTR += ts.getCompletionDistance(startPos, a.size);
                startPos = ts.getLastTarget();
            }
        }
        TTR += PathFinding.getDistance(startPos, t.getFirstTarget(), a.size);
        return TTR * a.moveTime;
    } 

    public boolean reached(Int2D pos, Int2D size, Int2D target) {
        return target.x >= pos.x && target.x < pos.x + size.x && target.y >= pos.y && target.y < pos.y + size.y;
    }

    private class Task {
        private Int2D[] targets;
        private int targetIndex = 0;

        public Task(Int2D[] targets) {
            this.targets = targets;
        }

        public Int2D getGoal() {
            return targets[targetIndex];
        }

        public Int2D[] getTargets() {
            return targets;
        }

        public Int2D getFirstTarget() {
            return targets[0];
        }

        public Int2D getLastTarget() {
            return targets[targets.length-1];
        }

        public boolean reached(Int2D pos, Int2D size) {
            Int2D target = getGoal();
            if (target.x >= pos.x && target.x < pos.x + size.x && target.y >= pos.y && target.y < pos.y + size.y) {
                targetIndex++;
                return true;
            }
            return false;
        }

        public boolean complete() {
            return targetIndex == targets.length;
        }

        public int getCompletionDistance(Int2D from, Int2D size) {
            int dist = 0;
            for (int i = targetIndex; i < targets.length; i++) {
                Int2D target = targets[targetIndex];
                dist += PathFinding.getDistance(from, target, size);
                from = target;
            }
            return dist;
        }
    }
}
