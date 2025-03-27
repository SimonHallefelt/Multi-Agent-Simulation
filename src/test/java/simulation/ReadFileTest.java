package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import simulation.ReadFile.FileData;
import simulation.setup.DefaultSetup;

public class ReadFileTest {

    @Test void test_1_simpleFormat() {
        new DefaultSetup().injectAgents();
        ReadFile rf = new ReadFile();
        FileData fd = rf.readInput("src\\test\\resources\\simple\\warehouse_3.json");

        assertEquals(fd.agentList.size(), 11);
        assertEquals(fd.brainList.size(), 0);
        assertEquals(fd.map.getHeight(), 18);
        assertEquals(fd.map.getWidth(), 18);
        assertEquals(fd.agents.getHeight(), fd.map.getHeight());
        assertEquals(fd.agents.getWidth(), fd.map.getWidth());

        assertEquals(fd.pickup.size(), 13);
        assertEquals(fd.depot.size(), 13);
        assertEquals(fd.tasksPerStep, 1);
        assertTrue(fd.taskList.isEmpty());
    }

    @Test void test_1_standardFormat() {
        String warehouseLayout = "src\\test\\resources\\standard\\Conventional\\warehouseLayout.json";
        String instance = "src\\test\\resources\\standard\\Conventional\\instances\\basic.json";
        new DefaultSetup().injectAgents();
        ReadFile rf = new ReadFile();
        FileData fd = rf.readInput(warehouseLayout, instance);

        assertEquals(10, fd.agentList.size());
        assertEquals(0, fd.brainList.size());
        assertEquals(70, fd.map.getHeight());
        assertEquals(80, fd.map.getWidth());
        assertEquals(fd.agents.getHeight(), fd.map.getHeight());
        assertEquals(fd.agents.getWidth(), fd.map.getWidth());

        assertEquals(220, fd.pickup.size());
        assertEquals(2, fd.depot.size());
        assertEquals(2, fd.supply.size());
        assertEquals(1, fd.tasksPerStep);
        assertTrue(fd.taskList.isEmpty());
    }
}
