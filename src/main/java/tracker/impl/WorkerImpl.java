/**
 * 
 */
package tracker.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tracker.Events.EventType;
import tracker.Events.Event;
import tracker.Events.Result;
import tracker.dao.SimpleMemStorage;
import tracker.dao.StorageDAO;
import tracker.datatypes.Node;
import tracker.interfaces.Manager;
import tracker.interfaces.Worker;

/**
 * 
 * @author fu
 *
 */
public class WorkerImpl implements Worker {
    private final HttpServer server;
    private final String register;
    private final StorageDAO dao = new SimpleMemStorage();
    private final int timeWindowMills;
    private final int port;
    private Manager localManager;
    private boolean registered = false;

    private static Logger logger = LogManager.getLogger();

    public WorkerImpl(int port, String register, int timeWindow) {
        this.port = port;
        this.register = register;
        this.timeWindowMills = 3600000 * timeWindow;
        RequestHandler handler = new RequestHandler(this);
        IOReactorConfig config = IOReactorConfig.custom().setSoTimeout(100).setTcpNoDelay(true).build();
        server = ServerBootstrap.bootstrap().setListenerPort(port).setIOReactorConfig(config)
                .setExceptionLogger(ExceptionLogger.STD_ERR).registerHandler("/event", handler)
                .registerHandler("/register", handler).create();
    }

    /**
     * send a http to register this worker
     */
    @Override
    public boolean register() {
        if (registered)
            return true;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(register);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        try {
            parameters.add(new BasicNameValuePair("port", String.valueOf(port)));
            post.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse response = httpClient.execute(post);
            registered = response.getStatusLine().getStatusCode() == 200;
            return registered;
        } catch (IOException e) {
            logger.error(e);
        }
        return false;
    }

    /**
     * process the given event save click and impression event try to find an
     * referrer for convent
     * 
     * @param event
     *            the track event to be processed
     * @return Result if a validate referrer found
     */
    @Override
    public Optional<Result> processEvt(Event event) {
        Result referrer = null;
        switch (event.getType()) {
        case CLICK:
            dao.saveClick(event);
            break;
        case IMPRESSION:
            dao.saveImpression(event);
            break;
        case CONVERSION:
            referrer = findConventionReferer(event);
            break;
        default:
            break;
        }
        return Optional.ofNullable(referrer);
    }

    /**
     * find a validated referrer event click or impression
     * 
     * @param convention
     *            Event find referrer for this
     * @return Result if found, otherwise null
     */
    private Result findConventionReferer(Event convention) {
        Optional<Event> event = dao.findLastClickWithin(convention, timeWindowMills);
        if (event.isPresent()) {
            Event click = event.get();
            Result.Builder referrer = Result.newBuilder();
            referrer.setConversionEventId(convention.getId());
            referrer.setReferrerEventId(click.getId());
            referrer.setReferrerType(EventType.CLICK);
            return referrer.build();
        }
        event = dao.findLastImpressionWithin(convention, timeWindowMills);
        if (event.isPresent()) {
            Event imps = event.get();
            Result.Builder referrer = Result.newBuilder();
            referrer.setConversionEventId(convention.getId());
            referrer.setReferrerEventId(imps.getId());
            referrer.setReferrerType(EventType.IMPRESSION);
            return referrer.build();
        }
        return null;
    }

    @Override
    public boolean start() {
        try {
            server.start();
            if(null == this.localManager)
                register();
            return true;
        } catch (IOException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public void stop() {
        server.shutdown(0, TimeUnit.MILLISECONDS);
        dao.stop();
    }
    
    public void setManager(Manager manager) {
        localManager = manager;
    }
    
    public void addNode(Node node) {
        localManager.registerWorker(node);
    }
    @Override
    public boolean unregister() {
        // TODO Auto-generated method stub
        return false;
    }

}
