package simulation;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
                        break;
                    case "D":
                        delivery.add(new Int2D(x, y));
                        break;
                    default:
                        break;
                }
            }
        }
        return new FileData(map, agents, pickup, delivery, AgentList, BrainList, tasksPerStep, taskGeneration);
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

    public class FileData {
        IntGrid2D map;
        SparseGrid2D agents;
        ArrayList<Int2D> pickup;
        ArrayList<Int2D> delivery;
        ArrayList<Agent> agentList;
        ArrayList<Brain> brainList;
        double tasksPerStep;
        String taskGeneration;

        public FileData(IntGrid2D map, SparseGrid2D agents, ArrayList<Int2D> pickup, ArrayList<Int2D> delivery,
                ArrayList<Agent> agentList, ArrayList<Brain> brainList, double tasksPerStep, String taskGeneration) {
            this.map = map;
            this.agents = agents;
            this.pickup = pickup;
            this.delivery = delivery;
            this.agentList = agentList;
            this.brainList = brainList;
            this.tasksPerStep = tasksPerStep;
            this.taskGeneration = taskGeneration;
        }
    }
}
