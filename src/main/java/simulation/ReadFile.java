package simulation;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

public class ReadFile {
    private HashMap<String, Color> defaultColorIndex = new HashMap<>();
    private Color[] defaultColors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA,
            Color.YELLOW, Color.ORANGE, Color.PINK, Color.GRAY, Color.DARK_GRAY };
    private int colorIndex = 0;
    private AgentFactory factory = new AgentFactory();
    JSONObject obj = null;

    public FileData readInput(String path) {
        return readInput(path, null);
    }

    public FileData readInput(String path, String instance) {
        try {
            obj = new JSONObject(new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        String inputFormat = obj.getString("format");
        switch (inputFormat.toLowerCase()) {
            case "standard":
                System.out.println("inputFormat: standard");
                return standardFormat(path, instance);
            case "simple":
                System.out.println("inputFormat: simple");
                return simpleFormat(path);
            default:
                System.out.println("inputFormat: " + inputFormat.toLowerCase() + " selected default");
                return simpleFormat(path);
        }
    }

    public FileData simpleFormat(String path) {
        List<ArrayList<String>> jsonMap = new ArrayList<>();
        int width = 0;
        for (Object o : obj.getJSONArray("map").toList()) {
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(o.toString().split(" ")));
            width = Math.max(width, list.size());
            jsonMap.add(list);
        }
        int height = jsonMap.size();

        IntGrid2D map = new IntGrid2D(width, height);
        SparseGrid2D agents = new SparseGrid2D(width, height);
        // Tasks tasks = new Tasks(this);
        ArrayList<Int2D> pickup = new ArrayList<>();
        ArrayList<Int2D> delivery = new ArrayList<>();
        BrainFactory brainFactory = new BrainFactory();
        // this.score = 0;

        // brain settings
        HashMap<String, AgentType> agentTypes = new HashMap<>();
        ArrayList<Agent> AgentList = new ArrayList<>();
        ArrayList<Brain> BrainList = new ArrayList<>();

        // task settings
        double tasksPerStep = 1.0;
        String taskGeneration = "random";
        ArrayList<Tasks.Task> taskList = new ArrayList<>();

        if (obj.has("brains")) {
            for (Object o : obj.getJSONArray("brains").toList()) {
                String name = o.toString();
                Brain b = brainFactory.createBrain(name);
                if (b != null) {
                    BrainList.add(b);
                }
            }
        }

        AgentType defaultAgentType = new AgentType();
        if (obj.has("default")) {
            JSONObject def = obj.getJSONObject("default");
            if(def.has("algo")) defaultAgentType.setAlgo(def.getString("algo"));
            if(def.has("size")) defaultAgentType.setSize(def.getString("size"));
            if(def.has("moveTime")) defaultAgentType.setMoveTime(def.getInt("moveTime"));
            if(def.has("color")) defaultAgentType.setColor(def.getString("color"));
        }

        if (obj.has("task-settings")) {
            JSONObject def = obj.getJSONObject("task-settings");
            if (def.has("TasksPerStep")) 
                tasksPerStep = def.getDouble("TasksPerStep");
            if (def.has("taskGeneration")) 
                taskGeneration = def.getString("taskGeneration");
            if (def.has("taskList")) {
                Tasks tasks = new Tasks(null);
                JSONArray JSONtasks = def.getJSONArray("taskList");
                for (int i = 0; i < JSONtasks.length(); i++) {
                    ArrayList<Int2D> goals = new ArrayList<>();
                    for (Object o : JSONtasks.getJSONArray(i).toList()) {
                        String[] goal = o.toString().split(",");
                        Int2D pos = new Int2D(Integer.parseInt(goal[0]), Integer.parseInt(goal[1]));
                        goals.add(pos);
                    }
                    taskList.add(tasks.makeTask(goals));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < jsonMap.get(y).size(); x++) {
                String value = jsonMap.get(y).get(x);
                String[] split = value.split("-");
                if (split[0].chars().allMatch(Character::isDigit)) { // if it is numberic, create an agent here
                    AgentType agentType = agentTypes.get(split[0]);
                    if (agentType == null) {
                        JSONObject o = obj.getJSONObject(split[0]);
                        if (o != null) {
                            agentType = new AgentType();
                            agentType.setAlgo(o.has("algo") ? o.getString("algo") : defaultAgentType.algo);
                            agentType.setSize(o.has("size") ? o.getString("size") : defaultAgentType.size);
                            agentType.setMoveTime(o.has("moveTime") ? o.getInt("moveTime") : defaultAgentType.moveTime);
                            agentType.setColor(o.has("color") ? o.getString("color") : defaultAgentType.color);
                            agentTypes.put(split[0], agentType);
                        }
                    }
                    Agent a = makeAgent(agentType, defaultAgentType, Integer.parseInt(split[0]), new Int2D(x,y), AgentList.size(), map, agents);
                    AgentList.add(a);
                    
                    // stop agent duplicates
                    for (int yy = y; yy < y + a.size.y; yy++) { 
                        for (int xx = x; xx < x + a.size.x; xx++) {
                            if (xx == x && yy == y)
                                continue;
                            String value2 = jsonMap.get(yy).get(xx);
                            String[] split2 = value2.split("-");
                            if (!split2[0].chars().allMatch(Character::isDigit)) {
                                throw new IllegalArgumentException(
                                        "should have been an agent at this position (" + xx + ", " + yy + ")");
                            }
                            jsonMap.get(yy).set(xx, split2.length > 1 ? split2[1] : ".");
                        }
                    }
                }
                if (split.length > 1) {
                    value = split[1];
                }
                switch (value) {
                    case "#":
                        map.set(x, y, 1);
                        break;
                    case "E":
                        pickup.add(new Int2D(x, y));
                        map.set(x, y, 2);
                        break;
                    case "D":
                        delivery.add(new Int2D(x, y));
                        map.set(x, y, 3);
                        break;
                    default:
                        break;
                }
            }
        }

        FileData fd = new FileData(map, agents, AgentList, pickup, delivery);
        fd.addTaskSettings(tasksPerStep, taskGeneration, taskList);
        fd.addBrains(BrainList);
        return fd;
    }

    // TSPLIB-extended
    public FileData standardFormat(String warehouseLayout, String instance) { 
        // locations
        ArrayList<Int2D> locations = new ArrayList<>();
        JSONObject LOCATION_COORD_SECTION = obj.getJSONObject("LOCATION_COORD_SECTION");
        for (int i = 0; i < LOCATION_COORD_SECTION.length(); i++) {
            JSONArray location = LOCATION_COORD_SECTION.getJSONArray(i+"");
            Int2D pos = new Int2D(location.getInt(0), location.getInt(1));
            locations.add(pos);
        }
        HashSet<Int2D> locationsUsed = new HashSet<>();

        // obstacles
        ArrayList<ArrayList<Int2D>> obstacles = new ArrayList<>();
        JSONObject OBSTACLES = obj.getJSONObject("OBSTACLES");
        for (int i = 0; i < OBSTACLES.length(); i++) {
            ArrayList<Int2D> obstacle = new ArrayList<>();
            for (Object o : OBSTACLES.getJSONArray(i+1+"")) {
                int location = Integer.parseInt(o.toString());
                obstacle.add(locations.get(location));
                locationsUsed.add(locations.get(location));
            }
            obstacles.add(obstacle);
        }

        // make map
        JSONArray size_xy = obj.getJSONArray("size_xy");
        Int2D warehouseSize = new Int2D(size_xy.getInt(0), size_xy.getInt(1));
        ArrayList<Int2D> pickup = new ArrayList<>();
        ArrayList<Int2D> depots = new ArrayList<>();
        IntGrid2D map = new IntGrid2D(warehouseSize.x, warehouseSize.y);
        for (ArrayList<Int2D> o : obstacles) { // add obstacles
            if (!isRectangle(o)) {
                System.out.println("Exception: Walls must be straight");
                System.exit(2);
            }
            Int2D p1 = o.get(0);
            Int2D p3 = o.get(2);
            for (int y = Math.min(p1.y, p3.y); y <= Math.max(p1.y, p3.y); y++) {
                for (int x = Math.min(p1.x, p3.x); x <= Math.max(p1.x, p3.x); x++) {
                    map.set(x, y, 1);
                }
            }
        }
        for (Object o : obj.getJSONArray("DEPOTS").toList()) { // add depots
            int location = Integer.parseInt(o.toString());
            Int2D pos = locations.get(location);
            depots.add(pos);
            locationsUsed.add(pos);
            map.set(pos.x, pos.y, 3);
        }
        for (Int2D location : locations) { // add pickup
            if (locationsUsed.contains(location)) continue;
            pickup.add(location);
            map.set(location.x, location.y, 2);
        }


        // instance
        try {
            obj = new JSONObject(new String(Files.readAllBytes(Paths.get(instance)), StandardCharsets.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // default agent type
        AgentType defaultAgentType = new AgentType();
        if (obj.has("default")) {
            JSONObject def = obj.getJSONObject("default");
            if(def.has("algo")) defaultAgentType.setAlgo(def.getString("algo"));
            if(def.has("size")) defaultAgentType.setSize(def.getString("size"));
            if(def.has("moveTime")) defaultAgentType.setMoveTime(def.getInt("moveTime"));
            if(def.has("color")) defaultAgentType.setColor(def.getString("color"));
        } 

        // agent types
        HashMap<Integer, AgentType> agentTypes = new HashMap<>();
        JSONObject AgentTypesJSON = obj.getJSONObject("agent-types");
        for (int i = 0; i < AgentTypesJSON.length(); i++) {
            JSONObject agentTypeJSON = AgentTypesJSON.getJSONObject(i+"");
            AgentType agentType = new AgentType();
            agentType.setAlgo(agentTypeJSON.has("algo") ? agentTypeJSON.getString("algo") : defaultAgentType.algo);
            agentType.setSize(agentTypeJSON.has("size") ? agentTypeJSON.getString("size") : defaultAgentType.size);
            agentType.setMoveTime(agentTypeJSON.has("moveTime") ? agentTypeJSON.getInt("moveTime") : defaultAgentType.moveTime);
            agentType.setColor(agentTypeJSON.has("color") ? agentTypeJSON.getString("color") : defaultAgentType.color);
            agentTypes.put(i, agentType);
        }

        // agents
        SparseGrid2D agents = new SparseGrid2D(warehouseSize.x, warehouseSize.y); 
        ArrayList<Agent> agentList = new ArrayList<>();
        JSONObject agentsJSON = obj.getJSONObject("agents");
        for (int i = 0; i < agentsJSON.length(); i++) {
            JSONObject agentJson = agentsJSON.getJSONObject(i+"");
            int type = agentJson.has("agent_type") ? agentJson.getInt("agent_type") : 0;
            String[] stringPos = agentJson.getString("initial_xy_pos").split(",");
            Int2D pos = new Int2D(Integer.parseInt(stringPos[0]), Integer.parseInt(stringPos[1]));
            Agent a = makeAgent(agentTypes.get(type), defaultAgentType, type, pos, i, map, agents);
            agentList.add(a);
        }

        // task-settings
        double tasksPerStep = 1.0;
        String taskGeneration = "random";
        ArrayList<Tasks.Task> taskList = new ArrayList<>();
        if (obj.has("task-settings")) {
            JSONObject def = obj.getJSONObject("task-settings");
            if (def.has("TasksPerStep")) 
                tasksPerStep = def.getDouble("TasksPerStep");
            if (def.has("taskGeneration")) 
                taskGeneration = def.getString("taskGeneration");
            if (def.has("taskList")) {
                Tasks tasks = new Tasks(null);
                JSONArray JSONtasks = def.getJSONArray("taskList");
                for (int i = 0; i < JSONtasks.length(); i++) {
                    ArrayList<Int2D> goals = new ArrayList<>();
                    for (Object o : JSONtasks.getJSONArray(i).toList()) {
                        String[] goal = o.toString().split(",");
                        Int2D pos = new Int2D(Integer.parseInt(goal[0]), Integer.parseInt(goal[1]));
                        goals.add(pos);
                    }
                    taskList.add(tasks.makeTask(goals));
                }
            }
        }

        // brains
        BrainFactory brainFactory = new BrainFactory();
        ArrayList<Brain> BrainList = new ArrayList<>();
        if (obj.has("brains")) {
            for (Object o : obj.getJSONArray("brains").toList()) {
                String name = o.toString();
                Brain b = brainFactory.createBrain(name);
                if (b != null) {
                    BrainList.add(b);
                }
            }
        }

        FileData fd = new FileData(map, agents, agentList, pickup, depots);
        fd.addTaskSettings(tasksPerStep, taskGeneration, taskList);
        fd.addBrains(BrainList);

        return fd;
    }

    private Agent makeAgent(AgentType agentType, AgentType defaultAgentType, int type, 
    Int2D pos, int agentNumber, IntGrid2D map, SparseGrid2D agents) {
        if (agentType == null) {
            agentType = defaultAgentType;
        }
        String algo = agentType.algo;
        String[] sizeString = { agentType.size };
        int moveTime = agentType.moveTime;
        String[] colorString = { agentType.color };
        
        sizeString = sizeString[0].split(",");
        Int2D size = new Int2D(Integer.parseInt(sizeString[0]), Integer.parseInt(sizeString[1]));
        Agent a = factory.createAgent(type+"", pos.x, pos.y, algo, moveTime, size, agentNumber);
        if (colorString[0].equals("default")) {
            Color color = getDefaultColor(type+"");
            a.setColor(color);
        } else {
            colorString = colorString[0].split(",");
            Color color = new Color(Integer.parseInt(colorString[0]), Integer.parseInt(colorString[1]),
                    Integer.parseInt(colorString[2]));
            a.setColor(color);
        }
        agents.setObjectLocation(a, pos);
        for (int y = pos.y; y < pos.y + a.size.y; y++) {
            for (int x = pos.x; x < pos.x + a.size.x; x++) {
                if (x != pos.x || y != pos.y) {
                    Agent.AgentClone ag = a.makeAgentClone();
                    agents.setObjectLocation(ag, x, y);
                }
                if (map.get(pos.x, pos.y) == 1) {
                    System.out.println("agent: " + agentNumber + " is spawning on a wall");
                    System.exit(1);
                }
            }
        }
        return a;
    }

    private Color getDefaultColor(String id) {
        if (defaultColorIndex.containsKey(id))
            return defaultColorIndex.get(id);

        defaultColorIndex.put(id, defaultColors[colorIndex]);
        colorIndex++;
        if (colorIndex >= defaultColors.length)
            colorIndex = 0;
        return defaultColorIndex.get(id);
    }

    // assumption, shape is point_1 -> point_2 -> point_3 -> point_4 -> point_1
    private Boolean isRectangle(ArrayList<Int2D> rec) {
        if (rec.size() > 4) return false;
        return isRectangle(rec.get(0), rec.get(1), rec.get(2), rec.get(3));
    }
    private Boolean isRectangle(Int2D p1, Int2D p2, Int2D p3, Int2D p4) {
        return p1.subtract(p2).equals(p4.subtract(p3)) && p1.subtract(p4).equals(p2.subtract(p3));
    }

    public class FileData {
        IntGrid2D map;
        SparseGrid2D agents;
        ArrayList<Agent> agentList;
        ArrayList<Int2D> pickup;
        ArrayList<Int2D> delivery;
        double tasksPerStep;
        String taskGeneration;
        ArrayList<Tasks.Task> taskList;
        ArrayList<Brain> brainList;
        long seed;

        public FileData(IntGrid2D map, SparseGrid2D agents, ArrayList<Agent> agentList, 
        ArrayList<Int2D> pickup, ArrayList<Int2D> delivery) {
            this.map = map;
            this.agents = agents;
            this.agentList = agentList;
            this.pickup = pickup;
            this.delivery = delivery;
            this.tasksPerStep = 1;
            this.taskGeneration = "random";
            this.taskList = new ArrayList<>();
            this.brainList = new ArrayList<>();
            this.seed = System.currentTimeMillis();
        }

        public void addTaskSettings(double tasksPerStep, String taskGeneration,
        ArrayList<Tasks.Task> taskList) {
            this.tasksPerStep = tasksPerStep;
            this.taskGeneration = taskGeneration;
            this.taskList = taskList;
        }

        public void addBrains(ArrayList<Brain> brainList) {
            this.brainList = brainList;
        }

        public void addSeed(long seed) {
            this.seed = seed;
        }
    }

    private class AgentType {
        String algo = "none";
        String size = "1,1";
        int moveTime = 1;
        String color = "default";

        public void setAlgo(String algo) {
            this.algo = algo;
        }
        public void setSize(String size) {
            this.size = size;
        }
        public void setMoveTime(int moveTime) {
            this.moveTime = moveTime;
        }
        public void setColor(String color) {
            this.color = color;
        }
    }
}
