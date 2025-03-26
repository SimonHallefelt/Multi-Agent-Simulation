package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import simulation.ReadFile.FileData;

public class ReadFileTest {

    @Test void test_1_simpleFormat() {
        ReadFile rf = new ReadFile();
        FileData fd = rf.readInput("src\\test\\resources\\simple\\warehouse_3.json");

        assertEquals(fd.agentList.size(), 11);
        assertEquals(fd.brainList.size(), 0);
        assertEquals(fd.map.getHeight(), 18);
        assertEquals(fd.map.getWidth(), 18);
        assertEquals(fd.agents.getHeight(), fd.map.getHeight());
        assertEquals(fd.agents.getWidth(), fd.map.getWidth());

        assertEquals(fd.pickup.size(), 13);
        assertEquals(fd.delivery.size(), 13);
        assertEquals(fd.tasksPerStep, 1);
        assertTrue(fd.taskList.isEmpty());
    }
}
