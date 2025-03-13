package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import sim.util.Int2D;

public class PathTest {
    @Test void test_addNewPositionPath_1() {
        Int2D pos = new Int2D(20,20);
        ArrayList<Int2D> positionPath = new ArrayList<Int2D>( 
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
        Path path = new Path(pos);
        path.addNewPositionPath(pos, (ArrayList<Int2D>) positionPath.clone());

        for (Int2D newPos : positionPath) {
            pos = pos.add(path.pop());
            assertTrue(pos.equals(newPos));
        }
    }
}
