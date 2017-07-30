package tracker;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tracker.Application;
import tracker.datatypes.RunMode;

public class TrackEvtAppTest {
    Application worker;
    Application manager;
    @Before
    public void setUp() throws Exception {
        worker = new Application(RunMode.WORKER, System.out);
        manager = new Application(RunMode.MANAGER, System.out);
    }

    @After
    public void tearDown() throws Exception {
    }

}
