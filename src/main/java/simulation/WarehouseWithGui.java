package simulation;

import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.display.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;

import sim.util.gui.SimpleColorMap;
import simulation.interfaces.Colorable;

/*
 * WarehouseWithGui is a class that extends GUIState and 
 * provides a graphical user interface for the Warehouse simulation.
 * It sets up the display, portrays the warehouse and agents,
 * and handles the simulation loop.
 */
public class WarehouseWithGui extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    ValueGridPortrayal2D warehousePortrayal = new ValueGridPortrayal2D("floor_typ");
    SparseGridPortrayal2D agentPortrayal = new SparseGridPortrayal2D();
    Color[] color;
    SimpleColorMap simpleColorMap;
    Warehouse warehouse = (Warehouse) state;

    // This is the main method that starts the simulation with GUI.
    // It creates a new Warehouse instance and a WarehouseWithGui instance,
    // and then calls the runSimulation method to start the GUI.
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse(System.currentTimeMillis());
        WarehouseWithGui vid = new WarehouseWithGui(warehouse);
        vid.runSimulation();
    }

    // This method sets up the GUI and starts the simulation.
    public void runSimulation() {
        Console c = new Console(this);
        c.setVisible(true);
    }

    // This method sets up the portrayals for the warehouse and agents.
    public void start() {
        super.start();
        setupPortrayals();
    }

    // This method is called to update the display.
    public void load() {
        super.load(state);
        setupPortrayals();
    }

    // This method sets up the portrayals for the warehouse and agents.
    // It configures the color map for the warehouse and the agents.
    public void setupPortrayals() {
        // tell the portrayals what to portray and how to portray them
        warehousePortrayal.setField(warehouse.map);
        // set color map
        color = new Color[6];
        color[0] = Color.WHITE;
        color[1] = Color.BLACK;
        color[2] = new Color(255,255,200);//Color.yellow;
        color[3] = new Color(200,255,200);
        color[4] = new Color(210,240,200);
        color[5] = new Color(190,250,220);
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

    // This method initializes the GUI and sets up the display.
    // It creates a new Display2D instance and attaches the portrayals to it.
    // It also sets the size of the display based on the warehouse dimensions.
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

    // This method is called to stop the simulation.
    // It disposes of the display frame and sets the display to null.
    public void quit() {
        super.quit();
        if (displayFrame != null)
            displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    // This is a constructor for the WarehouseWithGui class.
    // It initializes the GUI with a new Warehouse instance.
    // It sets the seed for the simulation based on the current time.
    // It also initializes the warehouse with a default file path.
    public WarehouseWithGui() {
        super(new Warehouse(System.currentTimeMillis()));
    }

    // This constructor initializes the GUI with a new Warehouse instance
    // and a specified file path.
    // It sets the seed for the simulation based on the current time.
    public WarehouseWithGui(String file_path) {
        super(new Warehouse(System.currentTimeMillis(), file_path));
    }

    // This constructor initializes the GUI with a new Warehouse instance
    // and a specified seed and file path.
    public WarehouseWithGui(long seed, String file_path) {
        super(new Warehouse(seed, file_path));
    }

    // This constructor initializes the GUI with a new Warehouse instance
    // and a specified seed, file path, and instance path.
    public WarehouseWithGui(long seed, String file_path, String instance_path) {
        super(new Warehouse(seed, file_path, instance_path));
    }

    // This constructor initializes the GUI with a specified Warehouse state.
    public WarehouseWithGui(Warehouse state) {
        super(state);
    }

    // This method returns the name of the simulation.
    // It is used to identify the simulation in the GUI.
    public static String getName() {
        return "Heterogeneous Agents";
    }
}
