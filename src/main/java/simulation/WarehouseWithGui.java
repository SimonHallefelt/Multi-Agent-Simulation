package simulation;

import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.engine.*;
import sim.display.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;

import sim.util.gui.SimpleColorMap;
import simulation.interfaces.Colorable;

public class WarehouseWithGui extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    ValueGridPortrayal2D warehousePortrayal = new ValueGridPortrayal2D("floor_typ");
    SparseGridPortrayal2D agentPortrayal = new SparseGridPortrayal2D();
    Color[] color;
    SimpleColorMap simpleColorMap;
    Warehouse warehouse = (Warehouse) state;

    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse(System.currentTimeMillis());
        WarehouseWithGui vid = new WarehouseWithGui(warehouse);
        Console c = new Console(vid);
        c.setVisible(true);
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load() {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals() {
        // tell the portrayals what to portray and how to portray them
        warehousePortrayal.setField(warehouse.map);
        // set color map
        color = new Color[4];
        color[0] = Color.WHITE;
        color[1] = Color.BLACK;
        color[2] = new Color(255,255,200);//Color.yellow;
        color[3] = new Color(200,255,200);
        simpleColorMap = new SimpleColorMap(color);
        warehousePortrayal.setMap(simpleColorMap);

        // agent portrayal
        agentPortrayal.setField(warehouse.agents);
        agentPortrayal.setPortrayalForAll(new RectanglePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Colorable agent = (Colorable) object;
                paint = agent.getColor();
                super.draw(object, graphics, info);
            }
        });

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.black);
        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);
        int pixelScale = 900 / warehouse.height;
        if (pixelScale <= 0) pixelScale = 1;
        display = new Display2D(warehouse.width * pixelScale, warehouse.height * pixelScale, this);
        display.setClipping(false);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Warehouse Display");
        c.registerFrame(displayFrame); // so the frame appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach(warehousePortrayal, "Warehouse");
        display.attach(agentPortrayal, "Agents");
    }

    public void quit() {
        super.quit();
        if (displayFrame != null)
            displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    public WarehouseWithGui() {
        super(new Warehouse(System.currentTimeMillis()));
    }

    public WarehouseWithGui(SimState state) {
        super(state);
    }

    public static String getName() {
        return "Heterogeneous Agents";
    }
}
