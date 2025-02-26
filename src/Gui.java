package src;

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

public class Gui extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    ValueGridPortrayal2D warehousePortrayal = new ValueGridPortrayal2D("floor_typ");
    SparseGridPortrayal2D agentPortrayal = new SparseGridPortrayal2D();
    SparseGridPortrayal2D agentTrailPortrayal = new SparseGridPortrayal2D();
    Color[] color;
    SimpleColorMap simpleColorMap;
    Warehouse warehouse = (Warehouse) state;

    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse(System.currentTimeMillis());
        Gui vid = new Gui(warehouse);
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
        warehousePortrayal.setField( warehouse.map );
        // set color map
        color = new Color[2];
        color[0] = Color.WHITE;
        color[1] = Color.BLACK;
        simpleColorMap = new SimpleColorMap(color);
        warehousePortrayal.setMap(simpleColorMap);

        // agent trail portrayal
        agentTrailPortrayal.setField( warehouse.agentTrail );
        agentTrailPortrayal.setPortrayalForAll( new RectanglePortrayal2D(Color.LIGHT_GRAY) );

        // agent portrayal
        agentPortrayal.setField( warehouse.agents );
        agentPortrayal.setPortrayalForAll( new RectanglePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Colorable agent = (Colorable) object;
                paint = agent.getColor();
                super.draw(object, graphics, info);
                }
        });

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.white);
        // redraw the display
        display.repaint();
    }
    
    public void init(Controller c) {
        super.init(c);
        display = new Display2D(warehouse.width * 32, warehouse.height * 32,this);
        display.setClipping(false);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Warehouse Display");
        c.registerFrame(displayFrame); // so the frame appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach( warehousePortrayal, "Warehouse" );
        display.attach( agentTrailPortrayal, "AgentTrail" );
        display.attach( agentPortrayal, "Agents" );
    }

    public void quit() {
        super.quit();
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }


    public Gui() { 
        super(new Warehouse(System.currentTimeMillis())); 
    }
    public Gui(SimState state) { 
        super(state); 
    }
    public static String getName() { 
        return "Heterogeneous Agents"; 
    }
}
