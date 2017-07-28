package tracker.dao;

import java.util.Optional;

import tracker.Events.Event;

public interface StorageDAO {
    public void saveClick(Event click);

    public void saveImpression(Event imps);

    /**
     * find last validate click that happened with given time
     * 
     * @param conv
     *            the conv need to be checked
     * @param timeWindowMills
     *            the time window in mills seconds
     * @return Event instance if found, otherwise empty
     */
    public Optional<Event> findLastClickWithin(Event conv, int timeWindowMills);

    /**
     * find last validate impression that happened with given time
     * 
     * @param conv
     *            the conv need to be checked
     * @param timeWindowMills
     *            the time window in mills seconds
     * @return Event instance if found, otherwise empty
     */
    public Optional<Event> findLastImpressionWithin(Event conv, int timeWindowMills);

    /**
     * puge old event older than the give time, if delete is true, old data will be
     * deleted, otherwise save to file
     * 
     * @param hours
     * @param delete
     */
    public void purgeOldThan(int hours, boolean delete);
    
    public void enableDataCleaner();

    public void stop();
}
