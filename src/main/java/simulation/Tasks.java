package simulation;

import java.util.ArrayList;
import java.util.HashMap;

import sim.util.Int2D;

public class Tasks {
    private Warehouse warehouse;
    private ArrayList<Int2D> pickup;
    private ArrayList<Int2D> delivery;
    private HashMap<Agent, ArrayList<Task>> activeTasks = new HashMap<>();
    private ArrayList<Task> taskList = new ArrayList<>();
    private ArrayList<Task> availableTasks = new ArrayList<>();
    private ArrayList<Task> impossibleTask = new ArrayList<>();
    private TaskConfiguration tc = TaskConfiguration.generateTasksUsingPickupAndDelivery;
    private double generateTasksPerStep = 1.0;
    private long generatedTasks = 0;
    private long completedTasks = 0;

    public Tasks(Warehouse warehouse) {
        this(warehouse, new ArrayList<>(), new ArrayList<>(), 1.0);
    }

    public Tasks(Warehouse warehouse, ArrayList<Int2D> pickup, ArrayList<Int2D> delivery, double generateTasksPerStep) {
        this.warehouse = warehouse;
        this.pickup = pickup;
        this.delivery = delivery;
        this.generateTasksPerStep = generateTasksPerStep;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public void setTaskConfiguration(int i) {
        switch (i) {
            case 0:
                tc = TaskConfiguration.generateTasksUsingPickupAndDelivery;
                break;
            case 1:
                tc = TaskConfiguration.completeTaskList;
                break;
            case 2:
                tc = TaskConfiguration.selectTasksFromTaskList;
                break;
            default:
                break;
        }
    }

    public void generateTasks() {
        while ((warehouse.schedule.getSteps() + 1) * generateTasksPerStep > generatedTasks) {
            switch (tc) {
                case generateTasksUsingPickupAndDelivery:
                    generateTasksUsingPickupAndDelivery();
                    break;
                case completeTaskList:
                    getFirstInTaskList();
                    break;
                case selectTasksFromTaskList:
                    copyTaskFromTaskList();
                    break;
                default:
                    break;
            }
            generatedTasks++;
        }
    }

    public void generateTasksUsingPickupAndDelivery() {
        Int2D[] targets = new Int2D[] {
                pickup.get(warehouse.random.nextInt(pickup.size())),
                delivery.get(warehouse.random.nextInt(delivery.size()))
        };
        availableTasks.add(new Task(targets));
    }

    public void getFirstInTaskList() {
        if (taskList.isEmpty()) {
            Boolean noActiveTasks = !activeTasks.values().stream().map(a -> a.isEmpty()).anyMatch(a -> false);
            if (availableTasks.isEmpty() && noActiveTasks)
                warehouse.kill();
            return;
        }
        availableTasks.add(taskList.remove(0));
    }

    public void copyTaskFromTaskList() {
        availableTasks.add(taskList.get(warehouse.random.nextInt(taskList.size())).clone());
    }

    public void assignTasks(ArrayList<Agent> AgentList) {
        ArrayList<Agent> availableAgents = (ArrayList) AgentList.clone();
        int size = availableTasks.size();
        for (int i = 0; i < size; i++) {
            Task t = availableTasks.remove(0);
            // select best agent for task
            ArrayList<Agent> possibleAgents = (ArrayList) availableAgents.clone();
            possibleAgents.removeIf(a -> !canPerform(a, t));
            if (possibleAgents.isEmpty()) {
                impossibleTask.add(t);
                continue;
            }
            possibleAgents.sort((a, b) -> timeToReach(a, t) - timeToReach(b, t));
            Agent a = possibleAgents.get(0);

            // assign task to agent
            ArrayList<Task> assigned = activeTasks.get(a);
            if (assigned != null) {
                assigned.add(t);
            } else {
                assigned = new ArrayList<>();
                assigned.add(t);
                activeTasks.put(a, assigned);
            }
            if (a.getTarget() == null) {
                a.setTarget(t.getGoal());
                a.makeDesirePath(warehouse);
            }
        }
    }

    public void reachedTarget(Agent a, Int2D pos) {
        ArrayList<Task> goals = activeTasks.get(a);
        if (goals != null && !goals.isEmpty()) {
            Task goal = goals.get(0);
            if (goal.reached(pos, a.getAgentSize())) {
                a.increaseScore();
                warehouse.increaseScore();
                if (goal.complete()) {
                    completedTasks++;
                    goals.remove(0);
                    a.setTarget(null);
                    assignNextTask(a);
                } else {
                    a.setTarget(goal.getGoal());
                    a.makeDesirePath(warehouse);
                }
            }
        }
    }

    public void assignNextTask(Agent a) {
        ArrayList<Task> goals = activeTasks.get(a);
        if (goals == null || goals.isEmpty())
            return;
        Task t = goals.get(0);
        a.setTarget(t.getGoal());
        a.makeDesirePath(warehouse);
    }

    public boolean canPerform(Agent a, Task t) {
        Int2D startPos = a.pos;
        ArrayList<Int2D> path;
        for (Int2D target : t.getTargets()) {
            if (!reached(startPos, a.size, target)) {
                path = PathFinding.aStar(warehouse, target, startPos, a.size, true);
                if (path.isEmpty())
                    return false;
                startPos = path.get(path.size() - 1);
            }
        }
        return true;
    }

    public int timeToReach(Agent a, Task t) {
        int TTR = 0;
        Int2D startPos = a.pos;
        ArrayList<Task> agentTasks = activeTasks.get(a);
        if (agentTasks != null) {
            for (Task ts : agentTasks) {
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

    public long getGeneratedTasks() {
        return generatedTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
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
            return targets[targets.length - 1];
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
            return targetIndex >= targets.length;
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

        public Task clone() {
            return new Task(targets.clone());
        }
    }

    enum TaskConfiguration {
        completeTaskList,
        selectTasksFromTaskList,
        generateTasksUsingPickupAndDelivery
    }
}
