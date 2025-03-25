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

import jogamp.opengl.glu.error.Error;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

public class ReadFile {
    private HashMap<String, Color> defaultColorIndex = new HashMap<>();
    private Color[] defaultColors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA,
            Color.YELLOW, Color.ORANGE, Color.PINK, Color.GRAY, Color.DARK_GRAY };
    private int colorIndex = 0;

    public FileData simpleMapJson(String path) {
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
        AgentFactory factory = new AgentFactory();
        BrainFactory brainFactory = new BrainFactory();
        // this.score = 0;

        // brain settings
        HashMap<String, JSONObject> agentTypes = new HashMap<>();
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


        String defaultAlgo = "none";
        String defaultSize = "1,1";
        int defaultMoveTime = 1;
        String defaultColor = "default";

        if (obj.has("default")) {
            JSONObject def = obj.getJSONObject("default");
            if (def.has("algo"))
                defaultAlgo = def.getString("algo");
            if (def.has("size"))
                defaultSize = def.getString("size");
            if (def.has("moveTime"))
                defaultMoveTime = def.getInt("moveTime");
            if (def.has("color"))
                defaultColor = def.getString("color");
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
                    JSONObject o = agentTypes.get(split[0]);
                    if (o == null) {
                        o = obj.getJSONObject(split[0]);
                        if (o != null) {
                            agentTypes.put(split[0], o);
                        }
                    }
                    String algo;
                    String[] sizeString = { "" };
                    String[] colorString = { "" };
                    int moveTime;
                    if (o != null) {
                        algo = o.has("algo") ? o.getString("algo") : defaultAlgo;
                        sizeString[0] = o.has("size") ? o.getString("size") : defaultSize;
                        moveTime = o.has("moveTime") ? o.getInt("moveTime") : defaultMoveTime;
                        colorString[0] = o.has("color") ? o.getString("color") : defaultColor;

                    } else {
                        algo = defaultAlgo;
                        sizeString[0] = defaultSize;
                        moveTime = defaultMoveTime;
                        colorString[0] = defaultColor;

                    }
                    sizeString = sizeString[0].split(",");
                    Int2D size = new Int2D(Integer.parseInt(sizeString[0]), Integer.parseInt(sizeString[1]));
                    Agent a = factory.createAgent(split[0], x, y, algo, moveTime, size);
                    if (colorString[0].equals("default")) {
                        Color color = getDefaultColor(split[0]);
                        a.setColor(color);
                    } else {
                        colorString = colorString[0].split(",");
                        Color color = new Color(Integer.parseInt(colorString[0]), Integer.parseInt(colorString[1]),
                                Integer.parseInt(colorString[2]));
                        a.setColor(color);
                    }
                    agents.setObjectLocation(a, x, y);
                    for (int yy = y; yy < y + size.y; yy++) { // make sure the agent size is correct and stop duplicates
                        for (int xx = x; xx < x + size.x; xx++) {
                            if (xx == x && yy == y)
                                continue;
                            String value2 = jsonMap.get(yy).get(xx);
                            String[] split2 = value2.split("-");
                            if (!split2[0].chars().allMatch(Character::isDigit)) {
                                throw new IllegalArgumentException(
                                        "should have been an agent at this position (" + xx + ", " + yy + ")");
                            }
                            Agent.AgentClone ag = a.makeAgentClone();
                            agents.setObjectLocation(ag, xx, yy);
                            jsonMap.get(yy).set(xx, split2.length > 1 ? split2[1] : ".");
                        }
                    }
                    AgentList.add(a);
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
    public FileData readStandardFormat(String path) { 
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

        // locations
        ArrayList<Int2D> locations = new ArrayList<>();
        int NUM_LOCATIONS = obj.getInt("NUM_LOCATIONS");
        JSONObject LOCATION_COORD_SECTION = obj.getJSONObject("LOCATION_COORD_SECTION");
        for (int i = 0; i < NUM_LOCATIONS; i++) {
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
        Int2D warehouseSize = new Int2D(80, 80); // temporary assumption (change this)
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
            map.set(location.x, location.y, 2);
        }

        FileData fd = new FileData(map, new SparseGrid2D(warehouseSize.x, warehouseSize.y), 
        new ArrayList<>(), pickup, depots);

        return fd;
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
        ArrayList<Int2D> pickup;
        ArrayList<Int2D> delivery;
        ArrayList<Agent> agentList;
        ArrayList<Brain> brainList;
        double tasksPerStep;
        String taskGeneration;
        ArrayList<Tasks.Task> taskList;

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
    }
}
