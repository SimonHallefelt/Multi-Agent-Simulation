package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sim.util.Int2D;

public class PathFindingTests {

    @Test void test_1_trueRandomWalk() {
        int n = 100000;
        Warehouse warehouse = new Warehouse(0);
        HashMap<Int2D, Integer> counter = new HashMap<Int2D, Integer>() {{
            put(new Int2D(1, 0), 0);
            put(new Int2D(-1, 0), 0);
            put(new Int2D(0, 1), 0);
            put(new Int2D(0, -1), 0);
        }};

        for (int i = 0; i < n; i++) {
            Int2D key = PathFinding.trueRandomWalk(warehouse, new Int2D(0,0));
            counter.put(key, counter.get(key)+1);
        }
        
        for (Int2D k : counter.keySet()) {
            System.out.println(k + " - " + counter.get(k));
            assertTrue(counter.get(k) >= n/5);
        }
    }

    @Test void test_1_getAccessiblePoints() {
        String warehouseLayout = "src\\test\\resources\\standard\\Conventional\\warehouseLayout.json";
        String instance = "src\\test\\resources\\standard\\Conventional\\instances\\basic.json";
        Warehouse warehouse = new Warehouse(0, warehouseLayout, instance);
        
        List<Int2D> targets = new ArrayList<>(Arrays.asList(
            new Int2D(1,1),
            new Int2D(1,2),
            new Int2D(20,5), //depot
            new Int2D(50,5), //depot
            new Int2D(21,12), //items
            new Int2D(21,16), //items
            new Int2D(21,44), //items
            new Int2D(31,48), //items
            new Int2D(9,48), //items
            new Int2D(73,40) //items
        ));

        Int2D startPos = new Int2D(1,1);

        assertEquals(10, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(1,1), targets).size());
        assertEquals(10, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(2,2), targets).size());
        assertEquals(10, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(3,3), targets).size());
        assertEquals(6, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(4,4), targets).size());
        assertEquals(6, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(5,5), targets).size());

        assertEquals(10, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(1,4), targets).size());
        assertEquals(6, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(4,1), targets).size());
    }

    @Test void test_2_getAccessiblePoints() {
        String warehouseLayout = "src\\test\\resources\\simple\\warehouse_3.json";
        Warehouse warehouse = new Warehouse(0, warehouseLayout);
        
        List<Int2D> targets = new ArrayList<>(Arrays.asList(
            new Int2D(0,1),
            new Int2D(0,2),
            new Int2D(0,3),
            new Int2D(17,1),
            new Int2D(17,2),
            new Int2D(17,3),
            new Int2D(10,10),
            new Int2D(0,13),
            new Int2D(0,14),
            new Int2D(0,15),
            new Int2D(0,16),
            new Int2D(17,13),
            new Int2D(17,14),
            new Int2D(17,15),
            new Int2D(17,16)
        ));

        Int2D startPos = new Int2D(1,2);
        assertEquals(6, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(1,1), targets).size());
        assertEquals(3, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(2,2), targets).size());
        
        startPos = new Int2D(0,15);
        assertEquals(8, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(1,1), targets).size());
        assertEquals(8, PathFinding.getAccessiblePoints(warehouse, startPos, new Int2D(2,2), targets).size());
    }
}
