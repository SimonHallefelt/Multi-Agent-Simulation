package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

public class warehouseTest {

    @Test void test_1_warehouse() {
        Warehouse state = new Warehouse(0, "src\\test\\resources\\simple\\warehouse_3.json");
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 1000);
        state.finish();

        assertTrue(true);
    }
}
