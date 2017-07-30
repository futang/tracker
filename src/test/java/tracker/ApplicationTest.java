package tracker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tracker.datatypes.RunMode;

public class ApplicationTest {

    @Test
    public void testRunManager() {
        Application app = new Application(RunMode.MANAGER, System.in, System.out);
        assertTrue(RunMode.MANAGER == app.getMode());
    }
    
    @Test
    public void testRunWorker() {
        Application app = new Application();
        assertTrue(RunMode.WORKER == app.getMode());
    }
}
