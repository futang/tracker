/**
 * 
 */
package tracker.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tracker.datatypes.Node;
import tracker.datatypes.Events.Event;
import tracker.interfaces.Manager;
import tracker.interfaces.Partitioner;
import tracker.interfaces.Worker;

/**
 * Manager send the same events with the same device id always to to the same
 * worker node by uuid consistent partition. It actively check the worker node
 * health. check if the data should be reorganized.
 * 
 * the manager send the event to worker nodes by posting http requst. then
 * worker will decided if it should save the event or send an referrer back to
 * manager
 * 
 * the envents between worker and manager is dealed in async way
 * 
 * @author fu
 *
 */
public class ManagerImpl implements Manager {
    private static OutputStream outputstream;
    private static InputStream inputstream;
    private final int partitionSize = 128;
    private final Node[] workers = new Node[partitionSize];
    private final Partitioner partitioner = new PartitionerImpl();
    private final CloseableHttpAsyncClient httpClient;
    private final Worker masterWorker;
    private final Node node;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int nodeSize = 0;

    private static Logger logger = LogManager.getLogger();

    public ManagerImpl(int port, String register, String eventUrl, int timeWindow) {
        masterWorker = new WorkerImpl(port, register, timeWindow);
        node = new Node(eventUrl, port);
        httpClient = HttpAsyncClients.createDefault();
        init();
    }

    /**
     * Init health checker, register worker node and start async http client
     */
    private void init() {
        initHealthChecker(600);
        registerWorker(node);
        httpClient.start();
        masterWorker.setManager(this);
        masterWorker.run();
    }

    /**
     * read track event from the give input stream and let manager to process it
     * 
     * @param is
     *            InputStream the event stream to read
     */
    private void readEvtStream(InputStream is) {
        while (true) {
            try {
                Event ev = Event.parseDelimitedFrom(is);
                if (ev == null) {
                    logger.info("Stream end, QUIT");
                    break;
                } else {
                    logger.debug("read event" + ev);
                    if (!processEvent(ev))
                        logger.warn("Failed to process:" + ev);
                }
            } catch (IOException e) {
                logger.error(e);
                break;
            }
        }
    }

    @Override
    public void run() {
        if (null == inputstream || null == outputstream)
            throw new IllegalStateException("InputStream and OutputStream cannot be null");
        readEvtStream(inputstream);
        if (!stop())
            logger.warn("Failed to stop manager");
    }

    /**
     * Process an received event by sending it to a worker node. step 1: find the
     * node number from the partitioner step 2: get node from the managed worker
     * list step 3: if the got worker is not health, find a health one step 4: send
     * the event to the worker
     * 
     * @param event
     *            the event to be processed
     * @return true if send the event correctly
     */
    public boolean processEvent(Event event) {
        int nodeNumber = partitioner.simplePartition(event.getDeviceId(), partitionSize, nodeSize);
        Node node = findWorkerNode(nodeNumber);
        if (!node.isHealth())
            node = findNextHealthWorkerNode(nodeNumber);
        if (null != node && node.isHealth())
            return dispatchEvent(event, node);
        else
            return false;
    }

    /**
     * Send an event to a node asynchronously and write the received result to the
     * OutputStream
     * 
     * @param event
     *            the event to be sent
     * @param node
     *            the node to receive this event
     * @return true if send the event successfully
     */
    public boolean dispatchEvent(Event event, Node node) {
        HttpPost post = new HttpPost(node.getUrl());
        NByteArrayEntity entity = new NByteArrayEntity(event.toByteArray());
        post.setEntity(entity);
        httpClient.execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        response.getEntity().writeTo(outputstream);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                } else
                    logger.warn(post.getRequestLine());
            }

            @Override
            public void failed(final Exception ex) {
                logger.error(ex);
            }

            @Override
            public void cancelled() {
            }
        });
        return true;
    }

    /**
     * find node in worker nodes
     * 
     * @param nodeNumber
     *            the node index in the worker nodes
     * @return the found node
     */
    private Node findWorkerNode(int nodeNumber) {
        return workers[nodeNumber];
    }

    /**
     * find next health worker, unitl lookup all workers
     * 
     * @param now
     *            the current index in the loop
     * @return the found NodeInfo instance, otherwise null
     */
    private Node findNextHealthWorkerNode(int now) {
        int next = now + 1;
        Node node;
        for (int i = 0; i < nodeSize; i++) {
            node = workers[next];
            if (++next == nodeSize)
                next = 0;
            if (null != node && node.isHealth())
                return node;
        }
        return null;
    }

    /**
     * check nodes health in every given time interval
     * 
     * @param period
     *            inteval time in seconds
     */
    private void initHealthChecker(int period) {
        Runnable healthChecker = new Runnable() {
            public void run() {
                boolean allOK = true;
                for (int i = 0; i < partitionSize; i++) {
                    if (null == workers[i]) {
                        boolean health = checkHealth(workers[i]);
                        workers[i].setHealth(health);
                        if (!health) {
                            allOK = false;
                            nodeSize--;
                        }
                    }
                }
                if (!allOK)
                    reorganize();
            }
        };
        scheduler.scheduleAtFixedRate(healthChecker, 1, period, TimeUnit.SECONDS);
    }

    @Override
    public boolean registerWorker(Node node) {
        for (int i = 0; i < partitionSize; i++) {
            if (null == workers[i] || !workers[i].isHealth()) {
                workers[i] = node;
                nodeSize++;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean unregisterWorker(Node node) {

        return false;
    }

    @Override
    public boolean checkHealth(Node node) {
        HttpGet get = new HttpGet(node.getUrl());
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response;
        try {
            response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200)
                return true;
        } catch (IOException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean stop() {
        try {
            int maxSleep = 5000;
            int sleep = 0;
            int inteval = 1000;
            while (httpClient.isRunning()) {
                Thread.sleep(inteval);
                sleep += inteval;
                if (!httpClient.isRunning() || sleep >= maxSleep)
                    httpClient.close();
            }
            masterWorker.stop();
            scheduler.shutdownNow();
            return true;
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public void reorganize() {
        // TODO all nodes should move data to new size partition

    }

    public Node[] getWorkers() {
        return workers;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        outputstream = os;
    }

    @Override
    public void setInputStream(InputStream in) {
        inputstream = in;
    }

    @Override
    public void setEventStreams(InputStream in, OutputStream os) {
        setInputStream(in);
        setOutputStream(os);
    }

}
