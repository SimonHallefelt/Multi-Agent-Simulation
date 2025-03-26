package simulation;

import sim.engine.*;
import sim.util.*;
import simulation.Agent.AgentClone;
import simulation.injectors.DefaultSetup;
import sim.field.grid.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Warehouse extends SimState {
    public int height;
    public int width;
    public int num_agents;
    public IntGrid2D map; // = new IntGrid2D(width, height);
    public SparseGrid2D agents; // = new SparseGrid2D(width, height);

    private ArrayList<Agent> AgentList;
    public ArrayList<Brain> BrainList;
    private ArrayList<Int2D> pickup;
    private ArrayList<Int2D> delivery;
    private int score;
    public AgentFactory factory = new AgentFactory();
    long startTime;
    Tasks tasks;

    String file_path;
    static String default_file_path = "src\\main\\resources\\Conventional\\warehouseLayout.json";
    // static String default_file_path = "src\\main\\resources\\warehouse_3.json";
    // static String default_file_path = "src\\main\\resources\\warehouse_5_completeList.json";

    String instance_path;
    static String default_instance_path = "src\\main\\resources\\Conventional\\instances\\basic.json";


    public Warehouse(long seed) {
        this(seed, default_file_path);
    }

    public Warehouse(long seed, String file_path) {
        this(seed, file_path, default_instance_path);
    }

    public Warehouse(long seed, String file_path, String instance_path) {
        super(seed);
        this.file_path = file_path;
        this.instance_path = instance_path;
        new DefaultSetup().injectAgents();
        this.readFile(file_path, instance_path);
    }

    public void addTrail(Agent.Trail t, Int2D pos) {
        if (agents.numObjectsAtLocation(pos) > 0)
            return;
        agents.setObjectLocation(t, pos);
    }

    public void clearTrails() {
        for (Agent a : AgentList) {
            if (a.TimeToCompletedMovement() > 0)
                continue;
            for (Agent.Trail t : a.getTrails()) {
                agents.remove(t);
            }
            a.removeTrails();
        }
    }

    public boolean isWall(Int2D pos) {
        if (pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height)
            return true;
        return map.get(pos.x, pos.y) == 1;
    }

    public boolean isAgentPresent(Int2D pos) {
        return agents.numObjectsAtLocation(pos) > 0;
    }

    public ArrayList<Agent> getAgentList() {
        return AgentList;
    }

    public boolean isOccupied(Int2D pos, boolean noAgents) {
        return isWall(pos) || (!noAgents && isAgentPresent(pos));
    }

    public boolean canMove(Int2D pos, Int2D delta, Int2D agentSize, boolean noAgents) {
        if (delta.x == 0 && delta.y == 0)
            return true;
        int size = delta.x == 0 ? agentSize.x : agentSize.y;
        if (delta.x != 0) {
            int x = delta.x > 0 ? agentSize.x - 1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(x, d), noAgents))
                    return false;
            }
        } else {
            int y = delta.y > 0 ? agentSize.y - 1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(d, y), noAgents))
                    return false;
            }
        }
        return true;
    }

    public boolean canMove(Int2D pos, Int2D delta, Int2D agentSize) {
        return canMove(pos, delta, agentSize, false);
    }

    public boolean move(Agent a, Int2D delta) {
        Int2D loc = agents.getObjectLocation(a);
        Int2D pos = loc.add(delta);
        Int2D agentSize = a.getAgentSize();
        if (!canMove(pos, delta, agentSize))
            return false;
        a.setPosition(pos);
        ArrayList<Agent.AgentClone> agentClones = a.getAgentClones();
        ArrayList<Int2D> oldPositions = new ArrayList<>(Arrays.asList(loc));
        for (int x = 0; x < agentSize.x; x++) {
            for (int y = 0; y < agentSize.y; y++) {
                if (x == 0 && y == 0) {
                    agents.setObjectLocation(a, pos);
                } else {
                    AgentClone ac = agentClones.get(x + y * agentSize.x - 1);
                    oldPositions.add(agents.getObjectLocation(ac));
                    agents.setObjectLocation(ac, pos.x + x, pos.y + y);
                }
            }
        }
        for (Int2D oldPos : oldPositions) {
            a.makeTrail(this, oldPos);
        }
        tasks.reachedTarget(a, pos);
        return true;
    }

    public void generateTasks() {
        tasks.generateTasks();
    }

    public void assignTasks() {
        tasks.assignTasks(AgentList);
    }

    public void readFile(String path, String instance) {
        ReadFile rf = new ReadFile();
        ReadFile.FileData fd = rf.readInput(path, instance);;

        this.map = fd.map;
        this.agents = fd.agents;
        this.pickup = fd.pickup;
        this.delivery = fd.delivery;
        this.AgentList = fd.agentList;
        this.BrainList = fd.brainList;

        this.width = map.getWidth();
        this.height = map.getHeight();

        this.tasks = new Tasks(this, pickup, delivery, fd.tasksPerStep);
        this.tasks.setTaskConfiguration(fd.taskGeneration);
        this.tasks.setTaskList(fd.taskList);

        this.score = 0;

        for (Agent a : this.AgentList) {
            schedule.scheduleRepeating(a, 10, 1);
        }

        schedule.scheduleRepeating(new BeforeEveryStep(), 0, 1);
        schedule.scheduleRepeating(new AfterEveryStep(), 100, 1);
    }

    public void increaseScore() {
        this.score++;
    }

    public void start() {
        super.start();
        this.readFile(this.file_path, this.instance_path);
        startTime = System.currentTimeMillis();
    }

    public void finish() {
        AgentList.sort((a1, a2) -> a1.getId().compareTo(a2.getId()));
        for (Agent a : AgentList) {
            System.out.println(a + ", score " + a.score);
            // System.out.println ("pos: " + a.pos + ", delta: " + a.delta + ", target: " +
            // a.target + "\n");
        }
        System.out.println("\nFinal score: " + score);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
        System.out.println("Number of steps: " + this.schedule.getSteps());
        System.out.println("Number of tasks generated/completed/impossible: " + this.tasks.getNumGeneratedTasks() + "/"
                + this.tasks.getNumCompletedTasks() + "/" + this.tasks.getNumImpossibleTasks());
        System.out.println("Seed: " + this.seed());
        System.out.println("Brains: " + this.BrainList.size());
        System.out.println("warehouse: " + this.file_path);
        System.out.println("--------------------------");
    }

    public static void main(String[] args) {
        doLoop(Warehouse.class, args);
        System.exit(0);
    }

    public int getScore() {
        return score;
    }

    public void runSimulation(int steps) {
        start();
        do
            if (!schedule.step(this)) break;
        while (schedule.getSteps() < 1000);
        finish();
    }
}