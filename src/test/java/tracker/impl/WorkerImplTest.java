package tracker.impl;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;

import tracker.Events.Event;
import tracker.Events.EventType;
import tracker.Events.Result;
import tracker.impl.ManagerImpl;
import tracker.impl.WorkerImpl;

public class WorkerImplTest {
    private ManagerImpl manager;
    private WorkerImpl worker;

    @Before
    public void setUp()  {
        manager = new ManagerImpl(9998, "http://localhost:9998/register", "http://localhost:9998/event", 12,
                System.out);
        manager.start();
        worker = new WorkerImpl(9991, "http://localhost:9998/register", 1);
        worker.start();
    }
    
    @Test
    public void testProcessEvt() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831743L);
        Event ev = event.build();

        Optional<Result> r = worker.processEvt(ev);
        assertTrue(!r.isPresent());

        event.setType(EventType.IMPRESSION);
        ev = event.build();
        r = worker.processEvt(ev);
        assertTrue(!r.isPresent());
    }

}
