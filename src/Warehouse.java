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
import java.util.HashSet;
import java.util.List;

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

    private ArrayList<Agent> AgentList;
    private ArrayList<Int2D> starts;
    private ArrayList<Int2D> goals;
    private int score;
    public AgentFactory factory = new AgentFactory();
    long startTime;


    private Color[] defaultColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE, Color.PINK, Color.GRAY, Color.DARK_GRAY};
    private int colorIndex = 0;
    private HashMap<String, Color> defaultColorIndex = new HashMap<>();
    private Tasks tasks;

    // String file_path = "test_files\\warehouse_1.json";
    // String file_path = "test_files\\warehouse_2.json";
    // String file_path = "test_files\\warehouse_2_no_path_collision.json";
    String file_path = "test_files\\warehouse_3.json";
    // String file_path = "test_files\\warehouse_1_size_test.json";
    // String file_path = "test_files\\warehouse_1_lonely.json";
    // String file_path = "test_files\\warehouse_simple.json";


    public Warehouse(long seed) {
        super(seed);
        this.readJson(file_path);
    }

    public void addTrail(Agent.Trail t, Int2D pos) {
        if (agents.numObjectsAtLocation(pos) > 0) return;
        agents.setObjectLocation(t, pos);
    }

    public void clearTrails() {
        for (Agent a : AgentList) {
            if (a.TimeToCompletedMovement() > 0) continue;
            for (Agent.Trail t : a.getTrails()) {
                agents.remove(t);
            }
            a.removeTrails();
        }
    }

    public boolean isWall(Int2D pos) {
        if (pos.x < 0 || pos.x >= width || pos.y < 0 || pos.y >= height) return true;
        return map.get(pos.x, pos.y) == 1;
    }

    public boolean isAgentPresent(Int2D pos) {
        return agents.numObjectsAtLocation(pos) > 0;
    }

    public List<Agent> getAgentList() {
        return AgentList;
    }

    public HashSet<Int3D> getPathSet(Agent ignore) {
        HashSet<Int3D> set = new HashSet<>();
        for (Agent a: AgentList) {
            if (a == ignore) continue;
            set.addAll(a.path.getPathSet(a.size,a.pos,a.moveTime,a.getDelay()));
        }
        return set;
    }

    public HashMap<Int2D,HashSet<Integer>> getPathMap(Agent ignore) {
        HashMap<Int2D,HashSet<Integer>> map = new HashMap<>();
        HashMap<Int2D,HashSet<Integer>> map2;
        for (Agent a: AgentList) {
            if (a == ignore) continue;
            map2 = a.path.getPathMap(a.size,a.pos,a.moveTime,a.getDelay());
            Path.addPathMap(map, map2);
        }
        return map;
    }

    public boolean isOccupied(Int2D pos, boolean noAgents) {
        return isWall(pos) || (!noAgents && isAgentPresent(pos));
    }

    public boolean canMove(Int2D pos, Int2D delta, Int2D agentSize, boolean noAgents) {
        if (delta.x == 0 && delta.y == 0) return true;
        int size = delta.x == 0 ? agentSize.x : agentSize.y;
        if (delta.x != 0) {
            int x = delta.x > 0 ? agentSize.x-1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(x, d), noAgents)) return false;
            }
        } else {
            int y = delta.y > 0 ? agentSize.y-1 : 0;
            for (int d = 0; d < size; d++) {
                if (isOccupied(pos.add(d, y), noAgents)) return false;
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
        if (!canMove(pos, delta, agentSize)) return false;
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

    public void assignTask() {
        tasks.assignTask(starts, goals, AgentList);
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
        this.tasks = new Tasks(this);
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
                    Agent a = factory.createAgent(split[0],x, y, algo, moveTime, size);
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

    public void increaseScore() {
        this.score++;
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

        defaultColorIndex.put(id, defaultColors[colorIndex]);
        colorIndex++;
        if (colorIndex >= defaultColors.length) colorIndex = 0;
        return defaultColorIndex.get(id);
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
}