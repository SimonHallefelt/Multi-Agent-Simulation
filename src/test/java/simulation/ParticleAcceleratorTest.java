package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ParticleAcceleratorTest {
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

}
