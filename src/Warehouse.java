package src;

import sim.engine.*;
import sim.util.*;
import sim.field.grid.*;

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
    int height = 100;
    int width = 100;
    int num_agents = 100;
    public IntGrid2D map; // = new IntGrid2D(width, height);
    public SparseGrid2D agents; // = new SparseGrid2D(width, height);
    private HashMap<Agent, Task> tasks; // = new HashMap<>();
    ArrayList<Agent> AgentList;
    private ArrayList<Int2D> starts;
    private ArrayList<Int2D> goals;
    private int score;
    public AgentFactory factory = new AgentFactory();
    Stack<Agent.Trail> trails = new Stack<>();


    public Warehouse(long seed) {
        super(seed);
    }

    public void setOccupied(Agent.Trail t, int x, int y) {
        agents.setObjectLocation(t, x, y);
        trails.push(t);
    }

    public void clearTrails() {
        Stack<Agent.Trail> newTrails = new Stack<>();
        System.out.println("number of trails: " + trails.size());
        while(!trails.empty()){
            Agent.Trail t = trails.pop();
            if (t.TimeToCompletedMovement() > 0) {
                newTrails.push(t);
            } else {
                Int2D pos = t.delate();
                freeOccupied(pos.x, pos.y);
            }
        }
        trails = newTrails;
    }

    public void freeOccupied(int x, int y) {
        agents.removeObjectsAtLocation(x, y);
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return map.get(x, y) == 1;
    }

    public boolean isAgentPresent(int x, int y) {
        Bag bag = agents.getObjectsAtLocation(x, y);
        if(bag == null) return false;
        int bag_s = bag.size();
        return  bag_s > 0;
    }

    public boolean isOccupied(int x, int y) {
        return isWall(x, y) || isAgentPresent(x, y);
    }

    public boolean move(Agent a, Int2D delta) {
        Int2D loc = agents.getObjectLocation(a);
        int x = loc.x + delta.x;
        int y = loc.y + delta.y;
        if (isOccupied(x, y)) return false;
        agents.setObjectLocation(a, x, y);
        Task goal = tasks.get(a);
        if (goal != null) {
            if (goal.reached(x,y)) {
                a.score++;
                score++;
                assignTask(a);
            }
        }
        return true;
    }

    public void assignTask(Agent a) {
        Task current = tasks.get(a);
        if (current == null || current.progress()) {
            int startsize = starts.size();
            int goalsize = goals.size();
            Int2D start = starts.get(random.nextInt(startsize));
            Int2D goal = goals.get(random.nextInt(goalsize));
            current = new Task(start, goal);
            tasks.put(a, current);
        }
        a.setTarget(current.getGoal());
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
                    String algo; //, size;
                    int moveTime;
                    if (o != null) {
                        algo = o.has("algo") ? o.getString("algo") : "none";
                        //size = o.has("size") ? o.getString("size") : "1";
                        moveTime = o.has("moveTime") ? o.getInt("moveTime") : 0;
                    } else {
                        algo = "none";
                        //size = "1";
                        moveTime = 0;
                    }
                    Agent a = factory.createAgent(x, y, algo, moveTime);
                    this.agents.setObjectLocation(a, x, y);
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
        for (Agent a: AgentList) {
            assignTask(a);
        }
        schedule.scheduleRepeating(new AfterEveryStep(), 100, 1);
    }

    public void start() {
        super.start();
        this.readJson("test_files\\warehouse_1.json");
        // this.readJson("test_files\\warehouse_simple.json");
    }

    public void finish() {
        for (Agent a: AgentList) {
            System.out.print(a.score + ", ");
            System.out.println ("pos: " + a.pos + " delta: " + a.delta + " target: " + a.target);
        }
        System.out.println("Final score: " + score);
    }

    public static void main(String[] args) {
        doLoop(Warehouse.class, args);
        System.exit(0);
    }

    private class Task {
        Int2D start, finish;
        boolean started = false;
        public Task(Int2D start, Int2D finish) {
            this.start = start;
            this.finish = finish;
        }

        public Int2D getGoal() {
            if (started) return finish;
            else return start;
        }

        public boolean reached(int x, int y) {
            if (started) return finish.x == x && finish.y == y;
            else return start.x == x && start.y == y;
        }

        public boolean progress() {
            if (started) return true;
            started = true;
            return false;
        }
    }
}