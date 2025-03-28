package simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sim.util.Int2D;

import java.util.List;
import java.util.Random;

enum Type {
    supply,
    itemStorage,
    depot
}
/**
 * Shared class for supply, item storage and depot
 * 
 * Stores information like what agents can reach it and what other 
 * task positions are accessible from it (by at least one other agent)
 */
public class TaskPosition {
    private Set<Agent> accessibleAgents = new HashSet<>();
    private Set<TaskPosition> accessibleSupply = new HashSet<>();
    private Set<TaskPosition> accessibleItemStorage = new HashSet<>();
    private Set<TaskPosition> accessibleDepot = new HashSet<>();
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
            default:
                System.out.println("Simon did something wrong");
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
        int size = accessibleSupply.size();
        if (size <= 0) return null;
        return accessibleItemStorage.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }

    public TaskPosition getAccessibleDepot(Warehouse w) {
        int size = accessibleSupply.size();
        if (size <= 0) return null;
        return accessibleDepot.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }

    public static Agent getCompatibleAgent(Warehouse w, List<TaskPosition> tasks) {
        Set<Agent> possible = new HashSet<>(tasks.get(0).accessibleAgents);
        for (int i = 1; i < tasks.size(); i++) {
            Set<Agent> possible2 = tasks.get(i).accessibleAgents;
            possible.removeIf(t -> !possible2.contains(t));
        }
        int size = possible.size();
        if (size <= 0) return null;
        return possible.stream().skip(w.random.nextInt(size)).findFirst().orElse(null);
    }
}
