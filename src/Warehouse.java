package src;

import sim.engine.*;
import sim.util.*;
import src.Agent.AgentClone;
import sim.field.grid.*;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

// import org.json.simple.JSONArray; 
// import org.json.simple.JSONObject; 
// import org.json.simple.parser.*; 
import org.json.*;


public class Warehouse extends SimState {
    public int height;
    public int width;
    public int num_agents;
    public IntGrid2D map; // = new IntGrid2D(width, height);
    public SparseGrid2D agents; // = new SparseGrid2D(width, height);
    public SparseGrid2D agentTrail; // = new SparseGrid2D(width, height);

    private ArrayList<Agent> AgentList;
    private ArrayList<Int2D> starts;
    private ArrayList<Int2D> goals;
    private int score;
    public AgentFactory factory = new AgentFactory();
    Stack<Agent.Trail> trails = new Stack<>();
    long startTime;

    private HashMap<Agent, ArrayList<Task>> tasks; // = new HashMap<>();


    private Color[] defaultColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE, Color.PINK, Color.GRAY, Color.DARK_GRAY};
    private int colorIndex = 0;
    private HashMap<String, Color> defaultColorIndex = new HashMap<>();

    // String file_path = "test_files\\warehouse_1.json";
    //String file_path = "test_files\\warehouse_1_size_test.json";
     String file_path = "test_files\\warehouse_1_lonely.json";


    public Warehouse(long seed) {
        super(seed);
        this.readJson(file_path);
    }

    public void setOccupiedTrail(Agent.Trail t, Int2D pos) {
        agentTrail.setObjectLocation(t, pos.x, pos.y);
        trails.push(t);
    }

    public void clearTrails() {
        Stack<Agent.Trail> newTrails = new Stack<>();
        while(!trails.empty()){
            Agent.Trail t = trails.pop();
            if (t.TimeToCompletedMovement() > 0) {
                newTrails.push(t);
            } else {
                Int2D pos = t.delate();
                freeOccupiedTrail(pos);
            }
        }
        trails = newTrails;
    }

    public void freeOccupiedTrail(Int2D pos) {
        agentTrail.removeObjectsAtLocation(pos);
    }

    public boolean isWall(Int2D pos) {
        if (pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height) return true;
        return map.get(pos.x, pos.y) == 1;
    }

    public boolean isAgentPresent(Int2D pos) {
        int numOfAgents = agents.numObjectsAtLocation(pos);
        int numOfTrails = agentTrail.numObjectsAtLocation(pos);
        return numOfAgents + numOfTrails > 0;
    }

    public boolean isOccupied(Int2D pos, boolean noAgents) {
        return isWall(pos) || (!noAgents && isAgentPresent(pos));
    }

    public boolean canMove(Int2D pos, Int2D delta, Int2D agentSize, boolean noAgents) {
        int size = delta.x == 0 ? agentSize.y : agentSize.x;
        if (delta.x != 0) {
            int x = delta.x > 0 ? size-1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(x, d), noAgents)) return false;
            }
        } else {
            int y = delta.y > 0 ? size-1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(d, y), noAgents)) return false;
            }
        }
        return true;
    }

    public boolean canMove(Int2D pos, Int2D delta, Int2D agentSize) {
        return canMove(pos, delta, agentSize, false);
    }

    public boolean move(Agent a, Int2D delta, Int2D agentSize) {
        Int2D loc = agents.getObjectLocation(a);
        Int2D pos = loc.add(delta);
        if (!canMove(pos, delta, agentSize)) return false;
        ArrayList<Agent.AgentClone> agentClones = a.getAgentClones();
        for (int x = 0; x < agentSize.x; x++) {
            for (int y = 0; y < agentSize.y; y++) {
                if (x == 0 && y == 0) {
                    agents.setObjectLocation(a, pos.x, pos.y);
                } else {
                    AgentClone ac = agentClones.get(x + y * agentSize.x - 1);
                    agentClones.get(x + y*agentSize.x - 1).makeTrail(this, agents.getObjectLocation(ac));
                    agents.setObjectLocation(ac, pos.x + x, pos.y + y);
                }
            }
        }
        ArrayList<Task> goals = tasks.get(a);
        if (goals != null) {
            Task goal = goals.get(0);
            if (goal.reached(pos.x, pos.y ,agentSize)) {
                a.score++;
                score++;
                assignNextTask(a);
            }
        }
        return true;
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

    /** 
    public void assignTask(Agent a) {
        Task current = tasks.get(a);
        if (current == null || current.progress()) {
            int startSize = starts.size();
            int goalSize = goals.size();
            Int2D start = starts.get(random.nextInt(startSize));
            Int2D goal = goals.get(random.nextInt(goalSize));
            current = new Task(start, goal);
            tasks.put(a, current);
        }
        a.setTarget(current.getGoal());
    }
    */

    public void assignTask() {
        int startSize = starts.size();
        int goalSize = goals.size();
        Int2D start = starts.get(random.nextInt(startSize));
        Int2D goal = goals.get(random.nextInt(goalSize));
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

    public void readJson(String path) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        List<ArrayList<String>> jsonMap = new ArrayList<>();
        this.width = 0;
        for (Object o : obj.getJSONArray("map").toList()) {
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(o.toString().split(" ")));
            this.width = Math.max(this.width, list.size());
            jsonMap.add(list);
        }
        this.height = jsonMap.size();

        this.map = new IntGrid2D(width, height);
        this.agents = new SparseGrid2D(width, height);
        this.agentTrail = new SparseGrid2D(width, height);
        this.tasks = new HashMap<>();
        this.starts = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.score = 0;

        HashMap<String, JSONObject> agentTypes = new HashMap<>();
        this.AgentList = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < jsonMap.get(y).size(); x++) {
                String value = jsonMap.get(y).get(x);
                String[] split = value.split("-");
                if (split[0].chars().allMatch( Character::isDigit )) { // if it is numberic, create an agent here
                    JSONObject o = agentTypes.get(split[0]);
                    if (o == null) {
                        o = obj.getJSONObject(split[0]);
                        if (o != null) {
                            agentTypes.put(split[0], o);
                        }
                    }
                    String algo;
                    String[] sizeString = {""};
                    String[] colorString = {""};
                    int moveTime;
                    if (o != null) {
                        algo = o.has("algo") ? o.getString("algo") : "none";
                        sizeString[0] = o.has("size") ? o.getString("size") : "1,1";
                        moveTime = o.has("moveTime") ? o.getInt("moveTime") : 0;
                        colorString[0] = o.has("color") ? o.getString("color") : "default";

                    } else {
                        algo = "none";
                        sizeString[0] = "1,1";
                        moveTime = 0;
                        colorString[0] = "default";

                    }
                    sizeString = sizeString[0].split(",");
                    Int2D size = new Int2D(Integer.parseInt(sizeString[0]), Integer.parseInt(sizeString[1]));
                    Agent a = factory.createAgent(x, y, algo, moveTime, size);
                    if (colorString[0].equals("default")) {
                        Color color = getDefaultColor(split[0]);
                        a.setColor(color);
                    }
                    else {
                        colorString = colorString[0].split(",");
                        Color color = new Color(Integer.parseInt(colorString[0]), Integer.parseInt(colorString[1]), Integer.parseInt(colorString[2]));
                        a.setColor(color);
                    }
                    this.agents.setObjectLocation(a, x, y);
                    for (int yy = y; yy < y + size.y; yy++){ // make sure the agent size is correct and stop duplicates
                        for (int xx = x; xx < x + size.x; xx++){
                            if (xx == x && yy == y) continue;
                            String value2 = jsonMap.get(yy).get(xx);
                            String[] split2 = value2.split("-");
                            if (!split2[0].chars().allMatch( Character::isDigit )) {
                                throw new IllegalArgumentException("should have been an agent at this position (" + xx + ", " + yy + ")");
                            }
                            Agent.AgentClone ag = a.makeAgentClone();
                            this.agents.setObjectLocation(ag, xx, yy);
                            jsonMap.get(yy).set(xx, split2.length > 1 ? split2[1] : ".");
                        }
                    }
                    schedule.scheduleRepeating(a);
                    AgentList.add(a);
                }
                if (split.length > 1) {
                    value = split[1];
                }
                switch (value) {
                    case "#":
                        this.map.set(x, y, 1);
                        break;
                    case "E":
                        starts.add(new Int2D(x,y));
                        break;
                    case "D":
                        goals.add(new Int2D(x,y));
                        break;
                    default:
                        break;
                }
            }
        }
        schedule.scheduleRepeating(new AfterEveryStep(), 100, 1);
    }

    public void start() {
        super.start();
        defaultColorIndex = new HashMap<>();
        colorIndex = 0;
        this.readJson(file_path);
        startTime = System.currentTimeMillis();
        // this.readJson("test_files\\warehouse_simple.json");
    }

    private Color getDefaultColor(String id) {
        if (defaultColorIndex.containsKey(id)) return defaultColorIndex.get(id);
        else {
            defaultColorIndex.put(id, defaultColors[colorIndex]);
            colorIndex++;
            if (colorIndex >= defaultColors.length) colorIndex = 0;
            return defaultColorIndex.get(id);
        }
    }

    public void finish() {
        for (Agent a: AgentList) {
            System.out.println("agent: " + a + ", score " + a.score);
            // System.out.println ("pos: " + a.pos + ", delta: " + a.delta + ", target: " + a.target + "\n");
        }
        System.out.println("\nFinal score: " + score);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
    }

    public static void main(String[] args) {
        doLoop(Warehouse.class, args);
        System.exit(0);
    }

    public boolean canPerform(Agent a, Task t) {
        Int2D next;
        ArrayList<Int2D> path = PathFinding.aStar(this, t.start, a.pos, a.size, true);
        if (  !t.start.equals(a.pos)) {
            next = path.get(0).subtract(a.pos);
            if (next.x == 0 && next.y == 0) return false;
        }
        Int2D startPos = path.get(path.size()-1);
        next = PathFinding.aStar(this, t.finish, startPos, a.size, true).get(0).subtract(startPos);
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