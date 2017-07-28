package tracker.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

import tracker.Events.Event;

public class SimpleMemStorage implements StorageDAO {
    private final Map<ByteString, Stack<Event>> clicks = new HashMap<>(1000);
    private final Map<ByteString, Stack<Event>> impressions = new HashMap<>(1000);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SimpleMemStorage() {
    }

    /**
     * scheduler to clean old data
     * 
     * @param period
     *            time in hours
     */
    private void scheduleDataCleaner(int period) {
        Runnable healthChecker = new Runnable() {
            public void run() {
                purgeOldThan(1, true);
            }
        };
        scheduler.scheduleAtFixedRate(healthChecker, 1, period, TimeUnit.HOURS);
    }

    /**
     * validate event happened with the given timeWindow
     * 
     * @param convention
     *            the contion event
     * @param event
     *            the envent to be validated
     * @param timeWindowMills
     *            time window in mills
     * @return ture if the event happened within the time window
     */
    public boolean validateEvent(Event convention, Event event, int timeWindowMills) {
        return event.getTimestamp() + timeWindowMills > convention.getTimestamp();
    }

    /**
     * find last validate event that happened with given time
     * 
     * @param event
     *            the conv need to be checked
     * @param store
     *            the event store to use
     * @param timeWindowMills
     *            time window in mills
     * @return Event or null
     */
    private Event findLastEventWithin(Event conv, Map<ByteString, Stack<Event>> store,
            int timeWindowMills) {
        Stack<Event> stack = store.get(conv.getDeviceId());
        Event last = null;
        if (null != stack) {
            last = stack.pop();
            if (!validateEvent(conv, last, timeWindowMills))
                last = null;
        }
        return last;
    }

    @Override
    public Optional<Event> findLastClickWithin(Event conv, int timeWindowMills) {
        Event event = findLastEventWithin(conv, clicks, timeWindowMills);
        return Optional.ofNullable(event);
    }

    @Override
    public Optional<Event> findLastImpressionWithin(Event conv, int timeWindowMills) {
        Event event = findLastEventWithin(conv, impressions, timeWindowMills);
        return Optional.of(event);
    }

    private void saveTo(Event event, Map<ByteString, Stack<Event>> store) {
        ByteString deviceId = event.getDeviceId();
        Stack<Event> stack = store.get(deviceId);
        if (null == stack) {
            stack = new Stack<>();
        }
        stack.push(event);
        store.put(event.getDeviceId(), stack);
    }

    @Override
    public void saveClick(Event click) {
        saveTo(click, clicks);
    }

    @Override
    public void saveImpression(Event imps) {
        saveTo(imps, impressions);
    }

    @Override
    public void purgeOldThan(int hours, boolean delete) {

    }

    @Override
    public void stop() {
        scheduler.shutdownNow();
    }

    @Override
    public void enableDataCleaner() {
        scheduleDataCleaner(1);
    }

    public Map<ByteString, Stack<Event>> getClicks() {
        return clicks;
    }

    public Map<ByteString, Stack<Event>> getImpressions() {
        return impressions;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
    

}
