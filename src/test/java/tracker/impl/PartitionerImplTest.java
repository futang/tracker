package tracker.impl;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;

import tracker.impl.PartitionerImpl;
import tracker.interfaces.Partitioner;

public class PartitionerImplTest {
    private Partitioner partitioner; 
    @Before
    public void setUp() throws Exception {
        partitioner = new PartitionerImpl(); 
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSimplePartition() {
        ByteString uuid = ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset());
        int index = partitioner.simplePartition(uuid, 2, 1);
        assertEquals(index,0);
    }
    @Test
    public void testSimplePartition2() {
        ByteString uuid = ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset());
        int index = partitioner.simplePartition(uuid, 128, 1);
        assertEquals(index,0);
        
    }

    @Test
    public void testSimplePartition3() {
        ByteString uuid = ByteString.copyFrom("70ef1017-42aa-4699-88a8-16d4cfcb56af", Charset.defaultCharset());
        int index = partitioner.simplePartition(uuid, 256, 1);
        assertEquals(index,0);
    }

}
