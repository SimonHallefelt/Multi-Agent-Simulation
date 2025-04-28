package simulation;

import java.util.ArrayList;
import java.util.List;

import sim.util.Int2D;

enum Type {
    supply,
    itemStorage,
    depot,
    supplydepot
}
/**
 * Shared class for supply, item storage and depot
 * 
 * Stores information like what agents can reach it and what other 
 * task positions are accessible from it (by at least one other agent)
 */
public class TaskPosition {
    private List<Agent> accessibleAgents = new ArrayList<>();
    private List<TaskPosition> accessibleSupply = new ArrayList<>();
    private List<TaskPosition> accessibleItemStorage = new ArrayList<>();
    private List<TaskPosition> accessibleDepot = new ArrayList<>();
    private Type type;
    private final Int2D position;

    public TaskPosition(String type, Int2D position) {
        this.position = position;
        switch (type.toLowerCase()) {
            case "supply":
                this.type = Type.supply;
                break;
            case "itemstorage":
                this.type = Type.itemStorage;
                break;
            case "depot":
                this.type = Type.depot;
                break;
            case "supplydepot":
                this.type = Type.supplydepot;
                break;
            default:
                System.out.println("Did not give correct type, pos: " + position);
                this.type = Type.supply;
                break;
        }
    }

    public void setType (String type) {
        switch (type.toLowerCase()) {
            case "supply":
                this.type = Type.supply;
                break;
            case "itemstorage":
                this.type = Type.itemStorage;
                break;
            case "depot":
                this.type = Type.depot;
                break;
            case "supplydepot":
                this.type = Type.supplydepot;
                break;
            default:
                System.out.println("Did not give correct type, pos: " + position);
                this.type = Type.supply;
                break;
        }
    }

    public void addAgent(Agent a) {
        accessibleAgents.add(a);
    }

    public void addTaskPosition(TaskPosition tp) {
        switch (tp.type) {
            case supply:
                accessibleSupply.add(tp);
                break;
            case itemStorage:
                accessibleItemStorage.add(tp);
                break;
            case depot:
                accessibleDepot.add(tp);
                break;
            case supplydepot:
                accessibleSupply.add(tp);
                accessibleDepot.add(tp);
                break;
            default:
                break;
        }
    }

    public void addTaskPositions(List<TaskPosition> tps) {
        for (TaskPosition tp : tps) {
            if (tp != this) addTaskPosition(tp);
        }
    }

    public TaskPosition getAccessibleSupply(Warehouse w) {
        int size = accessibleSupply.size();
        if (size <= 0) return null;
        return accessibleSupply.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }

    public TaskPosition getAccessibleItemStorage(Warehouse w) {
        int size = accessibleItemStorage.size();
        if (size <= 0) return null;
        return accessibleItemStorage.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }

    public TaskPosition getAccessibleDepot(Warehouse w) {
        int size = accessibleDepot.size();
        if (size <= 0) return null;
        return accessibleDepot.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }

    public static List<Agent> getCompatibleAgents(Warehouse w, List<TaskPosition> tasks) {
        if (tasks.get(0) == null) return new ArrayList<>();
        List<Agent> possible = new ArrayList<>(tasks.get(0).accessibleAgents);

        for (int i = 1; i < tasks.size(); i++) {
            if (tasks.get(i) == null) return new ArrayList<>();
            List<Agent> possible2 = tasks.get(i).accessibleAgents;
            possible.removeIf(t -> !possible2.contains(t));
        }
        return possible;
    }

    public Int2D getPosition() {
        return position;
    }

    public String toString() {
        return "Taskposition:" + position; 
    }
}
