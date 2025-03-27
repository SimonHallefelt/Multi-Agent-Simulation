package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import simulation.setup.DefaultSetup;

public class BrainTest {

    @Test public void test_1_astarBrain(){
        String warehouseLayout = "src\\test\\resources\\standard\\Conventional\\warehouseLayout.json";
        String instance = "src\\test\\resources\\standard\\Conventional\\instances\\aStarBrain.json";
        new DefaultSetup().injectAgents();
        Warehouse state = new Warehouse(0, warehouseLayout, instance);
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 1000);
        state.finish(); 

        int expected = 100;
        assertTrue(state.getScore() >= expected, "aStarBrain performance worse than expected: " + state.getScore() + "(expected " + expected + ")");
    }
}
