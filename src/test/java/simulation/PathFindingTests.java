package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import sim.util.Int2D;

public class PathFindingTests {

    @Test void test_trueRandomWalk_1() {
        int n = 100000;
        Warehouse warehouse = new Warehouse(0);
        HashMap<Int2D, Integer> counter = new HashMap<Int2D, Integer>() {{
            put(new Int2D(1, 0), 0);
            put(new Int2D(-1, 0), 0);
            put(new Int2D(0, 1), 0);
            put(new Int2D(0, -1), 0);
        }};

        for (int i = 0; i < n; i++) {
            Int2D key = PathFinding.trueRandomWalk(warehouse);
            counter.put(key, counter.get(key)+1);
        }
        
        for (Int2D k : counter.keySet()) {
            System.out.println(k + " - " + counter.get(k));
            assertTrue(counter.get(k) >= n/5);
        }
    }
}
