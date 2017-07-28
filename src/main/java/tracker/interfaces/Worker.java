/**
 * 
 */
package tracker.interfaces;

import java.util.Optional;

import tracker.Events.Event;
import tracker.Events.Result;
import tracker.datatypes.Node;

/**
 * 
 * @author fu
 *
 */
public interface Worker {
    /**
     * process the given event save click and impression event try to find an
     * referrer for convent
     * 
     * @param event
     *            the track event to be processed
     * @return Result if a validate referrer found
     */
    public Optional<Result> processEvt(Event event);

    /**
     * register worker to manager
     * 
     * @return true if registed success
     */
    public boolean register();

    /**
     * add a remote node to manager
     * 
     * @param node
     *            the node to be added
     */
    public void addNode(Node node);
    public void setManager(Manager manager);
    public boolean unregister();

    public boolean start();

    public void stop();

}
