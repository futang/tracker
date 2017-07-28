package tracker.interfaces;

import com.google.protobuf.ByteString;

public interface Partitioner {
    /**
     * map an uuid to a partition
     * 
     * @param uuid
     *            128 bit uuid
     * @param maxPartition
     *            the max number of partitions
     * @param currentPartition
     *            real partitions
     * @return the partition number which the uuid belongs to
     */
    public int simplePartition(ByteString uuid, int maxPartition, int currentPartition);

}
