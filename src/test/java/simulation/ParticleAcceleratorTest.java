package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ParticleAcceleratorTest {
    @Test
    void particleAcceleratorTest() {
        Warehouse state = new Warehouse(-929937372, "src\\main\\resources\\warehouse_3.json");
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 1000);
        state.finish();

        assertTrue(state.getScore() >= 208);
    }

}
