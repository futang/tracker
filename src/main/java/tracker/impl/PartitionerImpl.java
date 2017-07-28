package tracker.impl;

import com.google.protobuf.ByteString;

import tracker.interfaces.Partitioner;

public class PartitionerImpl implements Partitioner {

    @Override
    public int simplePartition(ByteString uuid, int maxPartition, int currentPartition) {
        byte firstByte = uuid.byteAt(0);
        int positive = firstByte + maxPartition;
        return positive % currentPartition;
    }
}
