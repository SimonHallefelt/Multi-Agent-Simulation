package simulation;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


import sim.util.Int2D;

public class Tasks {
    private Warehouse warehouse;
    private List<TaskPosition> itemStorage;
    private List<TaskPosition> depot;
    private List<TaskPosition> supply;
    private HashMap<Agent, List<Task>> activeTasks = new HashMap<>();
    private List<Task> taskList = new ArrayList<>();
    private List<Task> availableTasks = new ArrayList<>();
    private List<Task> impossibleTask = new ArrayList<>();
    private TaskConfiguration tc = TaskConfiguration.generateTasksUsingItemStorageAndDepot;
    private double TasksPerStep = 1.0;
    private long generatedTasks = 0;
    private long completedTasks = 0;
    private String addDeliveryAndSupply = "no";

    public Tasks(Warehouse warehouse) {
        this(warehouse, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1.0);
    }

    public Tasks(Warehouse warehouse, List<TaskPosition> itemStorage, List<TaskPosition> depot, List<TaskPosition> supply, double TasksPerStep) {
        this.warehouse = warehouse;
        this.itemStorage = itemStorage;
        this.depot = depot;
        this.supply = supply;
        this.TasksPerStep = TasksPerStep;
    }

    public void setAddDepotAndSupply(String addDeliveryAndSupply) {
        this.addDeliveryAndSupply = addDeliveryAndSupply;
    }

    public void setTaskList(List<List<TaskPosition>> taskList) {
        this.taskList = new ArrayList<>();
        for (List<TaskPosition> task : taskList) {
            this.taskList.add(makeTask(task));
        }
    }

    public void setTaskConfiguration(String s) {
        switch (s) {
            case "random":
                tc = TaskConfiguration.generateTasksUsingItemStorageAndDepot;
                break;
            case "completeList":
                tc = TaskConfiguration.completeTaskList;
                break;
            case "selectTasksFromList":
                tc = TaskConfiguration.selectTasksFromTaskList;
                break;
            default:
                break;
        }
    }

    public void generateTasks() {
        while ((warehouse.schedule.getSteps() + 1) * TasksPerStep > generatedTasks) {
            switch (tc) {
                case generateTasksUsingItemStorageAndDepot:
                    generateTasksUsingItemStorageAndDepot();
                    break;
                case completeTaskList:
                    if (taskList.isEmpty()) return;
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

    public void generateTasksUsingItemStorageAndDepot() {
        TaskPosition start = itemStorage.get(warehouse.random.nextInt(itemStorage.size()));
        TaskPosition end = start.getAccessibleDepot(warehouse);
        TaskPosition[] targets = new TaskPosition[] {
            start,
            end
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

    public void assignTasks(List<Agent> AgentList) {
        Boolean noActiveTasks = activeTasks.values().stream().map(a -> a == null || a.isEmpty()).allMatch(a -> a == true);
        if(taskList.isEmpty() && availableTasks.isEmpty() && noActiveTasks) {
            warehouse.kill();
            return;
        }
        while (!availableTasks.isEmpty()) {
            Task t = availableTasks.remove(0);
            // select best agent for task
            List<TaskPosition> tpl = new ArrayList<>();
            TaskPosition[] tpa = t.getTargets();
            for (int i = 0; i < tpa.length;i++) tpl.add(tpa[i]);
            // Agent a = TaskPosition.getCompatibleAgent(warehouse, tpl);
            List<Agent> agents = TaskPosition.getCompatibleAgents(warehouse, tpl);
            if (agents.isEmpty()) {
                impossibleTask.add(t);
                continue;
            }

            // get fastest agent for the task
            agents.sort((a, b) -> a.getDistanceCompletedAllTargets() - b.getDistanceCompletedAllTargets());
            Agent a = agents.get(0);
            
            // assign task to agent
            List<Task> assigned = activeTasks.get(a);
            if (assigned != null) {
                assigned.add(t);
            } else {
                assigned = new ArrayList<>();
                assigned.add(t);
                activeTasks.put(a, assigned);
            }
            int newDist = t.getCompletionDistance(t.getFirstTarget().getPosition(), a.size);
            if (assigned.size() >= 2) newDist += PathFinding.getDistance(assigned.get(assigned.size()-2).getLastTarget().getPosition(), t.getFirstTarget().getPosition(), a.size);
            a.addDistanceBetweenTargets(newDist * a.moveTime);
            if (a.getTarget() == null) {
                a.setTarget(t.getGoal());
                a.makeDesirePath(warehouse);
            }
        }
    }

    public void reachedTarget(Agent a, Int2D pos) {
        List<Task> goals = activeTasks.get(a);
        if (goals != null && !goals.isEmpty()) {
            Task goal = goals.get(0);
            Int2D goalPos = goal.getGoal();
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
                if (a.getTarget() == null) {
                    a.setDistanceBetweenTargets(0);
                } else {
                    int newDist = a.getDistanceBetweenAllTargets() - (PathFinding.getDistance(goalPos, a.getTarget(), a.size)*a.moveTime);
                    a.setDistanceBetweenTargets(newDist);
                }
            }
        }
    }

    public void assignNextTask(Agent a) {
        List<Task> goals = activeTasks.get(a);
        if (goals == null || goals.isEmpty())
            return;
        Task t = goals.get(0);
        a.setTarget(t.getGoal());
        a.makeDesirePath(warehouse);
    }

    public boolean reached(Int2D pos, Int2D size, Int2D target) {
        return target.x >= pos.x && target.x < pos.x + size.x && target.y >= pos.y && target.y < pos.y + size.y;
    }

    private Task makeTask(List<TaskPosition> goals) {
        switch (addDeliveryAndSupply) {
            case "no":
                return new Task(goals.toArray(new TaskPosition[0]));
            case "addDeliveryPoint":
                TaskPosition lastPos = goals.get(goals.size()-1);
                if(!depot.contains(lastPos)) {
                    goals.add(depot.get(warehouse.random.nextInt(depot.size())));
                }
                return new Task(goals.toArray(new TaskPosition[0]));
            case "addSupplyPoint":
                TaskPosition firstPos = goals.get(0);
                if(!supply.contains(firstPos)) {
                    goals.add(0, supply.get(warehouse.random.nextInt(supply.size())));
                }
                return new Task(goals.toArray(new TaskPosition[0]));
            default:
                return new Task(goals.toArray(new TaskPosition[0]));
        }
    }

    public long getNumGeneratedTasks() {
        return generatedTasks;
    }

    public long getNumCompletedTasks() {
        return completedTasks;
    }

    public int getNumImpossibleTasks() {
        return impossibleTask.size();
    }

    private class Task {
        private TaskPosition[] targets;
        private int targetIndex = 0;

        public Task(TaskPosition[] targets) {
            this.targets = targets;
        }

        public Int2D getGoal() {
            return targets[targetIndex].getPosition();
        }

        public TaskPosition[] getTargets() {
            return targets;
        }

        public TaskPosition getFirstTarget() {
            return targets[0];
        }

        public TaskPosition getLastTarget() {
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
                Int2D target = targets[i].getPosition();
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
        generateTasksUsingItemStorageAndDepot
    }
}
