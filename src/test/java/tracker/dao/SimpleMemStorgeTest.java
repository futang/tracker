package tracker.dao;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;

import tracker.dao.SimpleMemStorage;
import tracker.datatypes.Events.Event;
import tracker.datatypes.Events.EventType;

public class SimpleMemStorgeTest {
    SimpleMemStorage dao;

    @Before
    public void setUp() throws Exception {
        dao = new SimpleMemStorage();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEmpty() {
        Map<ByteString, Stack<Event>> clicks = dao.getClicks();
        assertTrue(clicks.size() == 0);
    }

    @Test
    public void testSaveClick() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831743L);
        Event ev = event.build();
        dao.saveClick(ev);
        Map<ByteString, Stack<Event>> clicks = dao.getClicks();
        assertTrue(clicks.size() == 1);
        
    }
    
    @Test
    public void testFindLastClickWithin() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56aa", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831748L);
        Event ev = event.build();
        dao.saveClick(ev);
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56ab", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CONVERSION);
        event.setTimestamp(1500237831750L);
        ev = event.build();
        Optional<Event> te = dao.findLastClickWithin(ev, 100);
        assertTrue(te.isPresent());
        assertTrue(te.get().getId().equals(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56aa", Charset.defaultCharset())));
        
    }
    
    @Test
    public void testFindLastClickNOTWithin() {
        Event.Builder event = Event.newBuilder();
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56aa", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CLICK);
        event.setTimestamp(1500237831748L);
        Event ev = event.build();
        dao.saveClick(ev);
        event.setId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56ab", Charset.defaultCharset()));
        event.setDeviceId(ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset()));
        event.setType(EventType.CONVERSION);
        event.setTimestamp(1500328800000L);
        ev = event.build();
        Optional<Event> te = dao.findLastClickWithin(ev, 100);
        assertTrue(!te.isPresent());
        
    }

}
