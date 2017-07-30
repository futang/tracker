package tracker.interfaces;

import java.io.InputStream;
import java.io.OutputStream;

import tracker.datatypes.Events.Event;
import tracker.datatypes.Node;

public interface Manager extends EventService{

    /**
     * register a worker node to manager
     * 
     * @param node
     *            the worker node to register
     * @return true if register correctly
     */
    public boolean registerWorker(Node node);

    /**
     * remove a node from the manager
     * 
     * @param node
     *            the node to remove
     * @return true if removed correctly
     */
    public boolean unregisterWorker(Node node);

    /**
     * check the health of a node
     * 
     * @param node
     *            the node to be checked
     * @return true if health
     */
    public boolean checkHealth(Node node);

    /**
     * process an tracking event
     * 
     * @param event
     *            the event to be processed
     * @return true if process the event successfully
     */
    public boolean processEvent(Event event);

    /**
     * send an event to a node
     * 
     * @param event
     *            the event to be sent
     * @param node
     *            the node to receive this event
     * @return true if send the event successfully
     */
    public boolean dispatchEvent(Event event, Node node);

    /**
     * reorganize all data if necessary
     */
    public void reorganize();
    public void setOutputStream(OutputStream os);
    public void setInputStream(InputStream in);
    public void setEventStreams(InputStream in, OutputStream os);
}
