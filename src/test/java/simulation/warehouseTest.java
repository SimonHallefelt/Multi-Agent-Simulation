package simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

public class warehouseTest {

    @Test void test_1_warehouse() {
        int status = -1;
        String[] args = {"-until", "1000"};
        Exception[] threadException = {null};
        
        try {
            // Custom SecurityManager
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(java.security.Permission perm) {}

                @Override
                public void checkExit(int status) {
                    throw new ExitException(status);
                }
            });

            // threadException captures exceptions from threads
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                if (throwable instanceof Exception) {
                    threadException[0] = (Exception) throwable;
                }
            });

            // Function invokes "System.exit(0);"
            Warehouse.doLoop(Warehouse.class, args); 
            fail("Expected System.exit");
        } catch (ExitException e) {
            status = e.getStatus();
        } catch (Exception e){
            fail("Exception in SimState " + e);
        }

        if (threadException[0] != null) {
            Exception e = threadException[0];
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println("Exception during test: " + sw.toString());
            fail("Exception during test: " + sw.toString());
        }
        assertEquals(0, status);
    }

    // Custom exception for exit
    private static class ExitException extends SecurityException {
        private int status;

        public ExitException(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
