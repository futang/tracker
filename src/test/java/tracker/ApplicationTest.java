package tracker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tracker.datatypes.RunMode;

public class ApplicationTest {
    
    @SuppressWarnings("static-access")
    @Test
    public void testRunManager() {
        Application app = new Application(RunMode.MANAGER);
        assertTrue(RunMode.MANAGER == app.getRunningMode());
    }
    
    @SuppressWarnings("static-access")
    @Test
    public void testRunWorker() {
        Application app = new Application(RunMode.WORKER);
        assertTrue(RunMode.WORKER == app.getRunningMode());
    }
}
