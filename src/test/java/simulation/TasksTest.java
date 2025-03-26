package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class TasksTest {

    @Test void test_1_completeList() {
        Warehouse state = new Warehouse(0, "simple", "src\\test\\resources\\warehouse_5_completeList.json");
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 1000);
        state.finish();

        assertEquals(state.tasks.getNumGeneratedTasks(), 5);
        assertEquals(state.tasks.getNumCompletedTasks(), 3);
        assertEquals(state.tasks.getNumImpossibleTasks(), 2);
        assertEquals(state.getScore(), 7);

        assertTrue(state.schedule.getSteps() < 50);
    }
}
