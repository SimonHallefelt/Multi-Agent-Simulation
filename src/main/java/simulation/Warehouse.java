package simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sim.engine.SimState;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import simulation.Agent.AgentClone;
import simulation.setup.DefaultSetup;

/*
 * Warehouse is a class that extends SimState and represents a simulation of a warehouse.
 * It initializes the simulation with a given seed and file paths,
 * reads the warehouse configuration from a file,
 * and manages the agents, tasks, and the warehouse environment.
 * It also provides methods for moving agents, and checking for walls or occupied spaces.
 * The class also includes methods for scoring and running the simulation.
 * It uses the MASON simulation library for agent-based modeling.
 */
public class Warehouse extends SimState {
    public int height;
    public int width;
    public int num_agents;
    public IntGrid2D map; // = new IntGrid2D(width, height);
    public SparseGrid2D agents; // = new SparseGrid2D(width, height);
    private List<Agent> AgentList;
    private List<Brain> BrainList;
    private List<TaskPosition> itemStorage;
    private List<TaskPosition> depot;
    private int score;
    public AgentFactory factory = new AgentFactory();
    long startTime;
    long endTime;
    Tasks tasks;

    String file_path;
    static String default_file_path = "src\\test\\resources\\simple\\warehouse_3_supply_and_depot.json";

    String instance_path;
    static String default_instance_path = "src\\test\\resources\\standard\\Conventional\\instances\\basic.json";

    // Constructor for the Warehouse class
    // It initializes the simulation with a given seed and default file paths.
    public Warehouse(long seed) {
        this(seed, default_file_path);
    }

    // Constructor for the Warehouse class
    // It initializes the simulation with a given seed and file path.
    public Warehouse(long seed, String file_path) {
        this(seed, file_path, default_instance_path);
    }

    // Constructor for the Warehouse class
    // It initializes the simulation with a given seed, file path, and instance path.
    // It also injects agents using the DefaultSetup class.
    public Warehouse(long seed, String file_path, String instance_path) {
        super(seed);
        this.file_path = file_path;
        this.instance_path = instance_path;
        new DefaultSetup().injectAgents();
        this.readFile(file_path, instance_path);
    }

    // This method adds a trail to the agent at the specified position.
    // It checks if the position is occupied by another agent before adding the trail.
    public void addTrail(Agent.Trail t, Int2D pos) {
        if (agents.numObjectsAtLocation(pos) > 0)
            return;
        agents.setObjectLocation(t, pos);
    }

    // This method clears the trails of all agents that have completed their movement.
    public void clearTrails() {
        for (Agent a : AgentList) {
            a.reduceDelay();
            if (a.getDelay() > 0)
                continue;
            for (Agent.Trail t : a.getTrails()) {
                agents.remove(t);
            }
            a.removeTrails();
        }
    }

    // This method checks if the specified position is a wall in the warehouse.
    // It returns true if the position is out of bounds or if it is occupied by a wall.
    public boolean isWall(Int2D pos) {
        if (pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height)
            return true;
        return map.get(pos.x, pos.y) == 1;
    }

    // This method checks if the specified position is occupied by an agent.
    // It returns true if there is one or more agents at the position.
    public boolean isAgentPresent(Int2D pos) {
        return agents.numObjectsAtLocation(pos) > 0;
    }

    // This method checks if the specified position is occupied by an agent or a wall.
    // It takes a boolean parameter to specify whether to ignore agents.
    public boolean isOccupied(Int2D pos, boolean noAgents) {
        return isWall(pos) || (!noAgents && isAgentPresent(pos));
    }

    // This method checks if the specified positions is occupied by an agent or a wall.
    // It takes a boolean parameter to specify whether to ignore agents.
    public boolean canMove(Int2D oldPos, Int2D newPos, Int2D agentSize, boolean noAgents) {
        Int2D delta = newPos.subtract(oldPos);
        if (delta.x == 0 && delta.y == 0)
            return true;
        if (newPos.distance(oldPos) > 1.0) {
            System.out.println("Teleportation attempted: " + oldPos + " -> " + newPos);
            return false;
        }
        int size = delta.x == 0 ? agentSize.x : agentSize.y;
        if (delta.x != 0) {
            int x = delta.x > 0 ? agentSize.x - 1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(newPos.add(x, d), noAgents))
                    return false;
            }
        } else {
            int y = delta.y > 0 ? agentSize.y - 1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(newPos.add(d, y), noAgents))
                    return false;
            }
        }
        return true;
    }

    // This method calls the canMove method with noAgents set to false.
    // It checks if the agent can move from the old position to the new position.
    public boolean canMove(Int2D oldPos, Int2D newPos, Int2D agentSize) {
        return canMove(oldPos, newPos, agentSize, false);
    }

    // This method checks if an agent can move to a new position.
    public boolean move(Agent a, Int2D newPos) {
        Int2D oldPos = agents.getObjectLocation(a);
        Int2D agentSize = a.getAgentSize();
        if (!canMove(oldPos, newPos, agentSize))
            return false;
        a.setPosition(newPos);
        List<Agent.AgentClone> agentClones = a.getAgentClones();
        List<Int2D> oldPositions = new ArrayList<>(Arrays.asList(oldPos));
        for (int x = 0; x < agentSize.x; x++) {
            for (int y = 0; y < agentSize.y; y++) {
                if (x == 0 && y == 0) {
                    agents.setObjectLocation(a, newPos);
                } else {
                    AgentClone ac = agentClones.get(x + y * agentSize.x - 1);
                    oldPositions.add(agents.getObjectLocation(ac));
                    agents.setObjectLocation(ac, newPos.x + x, newPos.y + y);
                }
            }
        }
        for (Int2D op : oldPositions) {
            a.makeTrail(this, op);
        }
        tasks.reachedTarget(a, newPos);
        return true;
    }

    // This method calls generateTasks method of the Tasks class. 
    public void generateTasks() {
        tasks.generateTasks();
    }

    // This method calls assignTasks method of the Tasks class.
    // It assigns tasks to the agents based on their positions and capabilities.
    public void assignTasks() {
        tasks.assignTasks(AgentList);
    }

    // readFile method reads the warehouse configuration from a file or files.
    // It initializes the map, agents, item storage, depot, and task positions.
    // It also sets up the tasks and agent schedules.
    public void readFile(String path, String instance) {
        ReadFile rf = new ReadFile();
        ReadFile.FileData fd = rf.readInput(path, instance);;

        this.map = fd.map;
        this.agents = fd.agents;
        this.itemStorage = fd.itemStorage;
        this.depot = fd.depot;
        this.AgentList = fd.agentList;
        this.BrainList = fd.brainList;

        for (Agent a: AgentList) {
            List<Int2D> accessible = PathFinding.getAccessiblePoints(this, a.pos, a.size, fd.positions);
            List<TaskPosition> accessibleTP = accessible.stream().map(e -> fd.tpMap.get(e)).collect(Collectors.toList());
            for (TaskPosition tp: accessibleTP) {
                tp.addAgent(a);
                tp.addTaskPositions(accessibleTP);
            }
        }

        this.width = map.getWidth();
        this.height = map.getHeight();

        this.tasks = new Tasks(this, fd.itemStorage, fd.depot, fd.supply, fd.tasksPerStep);
        this.tasks.setTaskConfiguration(fd.taskGeneration);
        this.tasks.setAddDepotOrSupply(fd.addDepotOrSupply);
        this.tasks.setTaskList(fd.taskList);

        this.score = 0;

        for (Agent a : this.AgentList) {
            schedule.scheduleRepeating(a, 10, 1);
        }

        schedule.scheduleRepeating(new BeforeEveryStep(), 0, 1);
        schedule.scheduleRepeating(new AfterEveryStep(), 100, 1);
    }

    // increaseScore method increments the score of the simulation.
    // It is called when an agent completes a task or reaches a target.
    public void increaseScore() {
        this.score++;
    }

    // This method is called to start the simulation.
    public void start() {
        super.start();
        this.readFile(this.file_path, this.instance_path);
        startTime = System.currentTimeMillis();
    }

    // This method is called to finish the simulation.
    // It sorts the agents by their IDs and prints their scores and times.
    // It also prints the final score, elapsed time, number of steps, and task statistics.
    // It prints the seed, number of brains, and warehouse file path.
    public void finish() {
        endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        AgentList.sort((a1, a2) -> a1.getId().compareTo(a2.getId()));
        for (Agent a : AgentList) {
            List<Long> scoreTimes = new ArrayList<>();
            List<Long> score = a.getScores();
            if (!score.isEmpty()) {
                for (int i = 1; i < score.size(); i++) {
                    scoreTimes.add(score.get(i) - score.get(i-1));
                }
                scoreTimes.add(this.schedule.getSteps() - score.get(score.size()-1));
                Collections.sort(scoreTimes);
            }
            Long minTime = score.isEmpty() ? null : scoreTimes.get(0);
            Long maxTime = score.isEmpty() ? null : scoreTimes.get(scoreTimes.size()-1);
            Long medianTime = score.isEmpty() ? null : scoreTimes.get(scoreTimes.size()/2);

            System.out.println(a + ", score: " + score.size() + ", steps between points min/max/median: " + minTime + "/" + maxTime + "/" + medianTime);

        }
        System.out.println("\nFinal score: " + score);
        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
        System.out.println("Number of steps: " + this.schedule.getSteps());
        System.out.println("Number of tasks generated/completed/impossible: " + this.tasks.getNumGeneratedTasks() + "/"
                + this.tasks.getNumCompletedTasks() + "/" + this.tasks.getNumImpossibleTasks());
        System.out.println("Seed: " + this.seed());
        System.out.println("Brains: " + this.BrainList.size() + " ("
                + this.BrainList.stream().map(b -> b.getClass().getSimpleName()).collect(Collectors.joining(", ")) + ")");
        System.out.println("warehouse: " + this.file_path);
        System.out.println("--------------------------");
    }

    // This method is called to run the simulation without a GUI.
    public static void main(String[] args) {
        doLoop(Warehouse.class, args);
        System.exit(0);
    }

    // runSimulation method runs the simulation for 1000 steps.
    public void runSimulation() {
        runSimulation(1000);
    }

    // This method runs the simulation for a specified number of steps.
    public void runSimulation(int steps) {
        start();
        do
            if (!schedule.step(this)) break;
        while (schedule.getSteps() < steps);
        finish();
    }

    // getAgentAt method returns the agent occupying the specified position.
    public Agent getAgentAt(Int2D pos) {
        Bag bag = agents.getObjectsAtLocation(pos);
        if (bag.size() == 0)
            return null;
        for (Object object : bag) {
            if (object instanceof Agent) {
                return (Agent) object;
            } else if (object instanceof Agent.AgentClone) {
                return ((Agent.AgentClone) object).getAgent();
            } else if (object instanceof Agent.Trail) {
                return ((Agent.Trail) object).getAgent();
            }
        }
        return null;
    }
    
    // getScore method returns the current score of the simulation.
    public int getScore() {
        return score;
    }

    // getBrainList method returns the list of brains used in the simulation.
    public List<Brain> getBrainList() {
        return BrainList;
    }

    // getAgentList method returns the list of agents in the simulation.
    public List<Agent> getAgentList() {
        return AgentList;
    }

    // getTasks method returns the tasks object used in the simulation.
    public Tasks getTasks() {
        return tasks;
    }

    // getStartTime and getEndTime methods return the start and end times of the simulation.
    public long getStartTime() {
        return startTime;
    }
    public long getEndTime() {
        return endTime;
    }
}