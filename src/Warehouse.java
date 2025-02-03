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
import java.util.List;

// import org.json.simple.JSONArray; 
// import org.json.simple.JSONObject; 
// import org.json.simple.parser.*; 
import org.json.*;


public class Warehouse extends SimState {
    int height = 100;
    int width = 100;
    int num_agents = 100;
    public IntGrid2D map = new IntGrid2D(width, height);
    public SparseGrid2D agents = new SparseGrid2D(width, height);

    public Warehouse(long seed) {
        super(seed);
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

    public boolean move(Agent a, int dx, int dy) {
        Int2D loc = agents.getObjectLocation(a);
        int x = loc.x + dx;
        int y = loc.y + dy;
        if (isOccupied(x, y)) return false;
        agents.setObjectLocation(a, x, y);
        return true;
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

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < jsonMap.get(y).size(); x++) {
                switch (jsonMap.get(y).get(x)) {
                    case "#":
                        this.map.set(x, y, 1);
                        break;
                    case "1":
                        Agent a = new Agent();
                        this.agents.setObjectLocation(a, x, y);
                        schedule.scheduleRepeating(a);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void start() {
        super.start();
        this.readJson("test_files\\warehouse_1.json");
    }

    public static void main(String[] args) {
        doLoop(Warehouse.class, args);
        System.exit(0);
    }
}