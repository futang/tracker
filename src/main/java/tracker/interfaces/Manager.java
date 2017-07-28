package tracker.interfaces;

import tracker.Events.Event;
import tracker.datatypes.Node;

public interface Manager {

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

    public boolean start();

    public boolean stop();
}
