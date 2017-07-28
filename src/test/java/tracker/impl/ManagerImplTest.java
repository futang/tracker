package tracker.impl;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;

import tracker.Events.EventType;
import tracker.Events.Event;
import tracker.datatypes.Node;
import tracker.impl.ManagerImpl;

public class ManagerImplTest {
    private ManagerImpl manager;
    private int port;

    @Before
    public void setUp() {
        port = ThreadLocalRandom.current().nextInt(8000, 9000);
        manager = new ManagerImpl(port, "http://localhost:" + port + "/register", "http://localhost:" + port + "/event",
                12, System.out);
        manager.start();
    }
    @Test
    public void testWorkerCreated() {
        assertTrue(1 == manager.getNodeSize());
    }

    @Test
    public void testRegisterWorker() {
        Node node = new Node("http://localhost:9997/event", 9997);
        manager.registerWorker(node);
        assertTrue(2 == manager.getNodeSize());
        assertTrue(9997 == manager.getWorkers()[1].getPort());
    }

    @Test
    public void testProcessEvent() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831743L);
        Event ev = event.build();
        boolean result = manager.processEvent(ev);
        assertTrue(result);
    }

    @Test
    public void testDispatchEvent() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831743L);
        Event ev = event.build();
        boolean result = manager.dispatchEvent(ev, new Node("http://localhost:9999/event", 9999));
        assertTrue(result);
    }
}
