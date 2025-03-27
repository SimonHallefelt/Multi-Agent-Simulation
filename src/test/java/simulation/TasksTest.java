package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class TasksTest {

    @Test void test_1_completeList() {
        Warehouse state = new Warehouse(0, "src\\test\\resources\\simple\\warehouse_5_completeList.json");
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

    @Test void test_1_addDelivery() {
        String warehouseLayout = "src\\test\\resources\\standard\\Conventional\\warehouseLayout.json";
        String instance = "src\\test\\resources\\standard\\Conventional\\instances\\completeTaskList.json";
        Warehouse state = new Warehouse(0, warehouseLayout, instance);
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < 10000);
        state.finish();

        assertEquals(23, state.tasks.getNumGeneratedTasks());
        assertEquals(23, state.tasks.getNumCompletedTasks());
        assertEquals(0, state.tasks.getNumImpossibleTasks());
        assertEquals(126, state.getScore());

        assertTrue(state.schedule.getSteps() < 10000);
    }
}
