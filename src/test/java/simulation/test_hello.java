package simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class test_hello {
    @Test void test_hello() {
        Hello hw = new Hello();
        String s = hw.hello();
        System.out.println(s);
        assertTrue(s.equals("Hello World!"));
    }
}
