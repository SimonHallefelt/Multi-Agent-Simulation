package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PerformanceMeasurement {
    @Test
    void particleAcceleratorTest() {
        Warehouse state = new Warehouse(-929937372, "src\\test\\resources\\simple\\warehouse_3.json");
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 1000);
        state.finish();

        int expected = 208;

        assertTrue(state.getScore() >= expected, "Warehouse3 has lower performance than expected: " + state.getScore() + "(expected " + expected + ")");
    }

    @Test
    void ConventionalWarehouseTest() {
        String warehouseLayout = "src\\test\\resources\\standard\\Conventional\\warehouseLayout.json";
        String instance = "src\\test\\resources\\standard\\Conventional\\instances\\basic.json";
        Warehouse state = new Warehouse(-929937372, warehouseLayout, instance);
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 10000);
        state.finish();

        int expected = 1190;

        assertTrue(state.getScore() >= expected, "Warehouse3 has lower performance than expected: " + state.getScore() + "(expected " + expected + ")");
    }

}
