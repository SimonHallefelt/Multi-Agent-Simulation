package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sim.util.Int2D;

public class PathTest {
    @Test void test_1_addNewPositionPath() {
        Int2D pos = new Int2D(20,20);
        List<Int2D> positionPath = new ArrayList<Int2D>( 
            Arrays.asList(
                new Int2D(21,20),
                new Int2D(22,20),
                new Int2D(22,21),
                new Int2D(22,22),
                new Int2D(21,22),
                new Int2D(20,22),
                new Int2D(20,21),
                new Int2D(20,20)
            )
        );
        Path path = new Path(new ArrayList<Int2D>(positionPath));

        for (Int2D newPos : positionPath) {
            pos = path.pop();
            assertTrue(pos.equals(newPos));
        }
    }
}
