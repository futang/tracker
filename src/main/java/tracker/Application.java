package tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tracker.Events.Event;
import tracker.datatypes.RunMode;
import tracker.impl.ManagerImpl;
import tracker.impl.WorkerImpl;
import tracker.interfaces.Manager;
import tracker.interfaces.Worker;

public class Application {
    private static Manager manager;
    private static Worker worker;
    private static RunMode runningMode;
    private static Logger logger = LogManager.getLogger();

    /**
     * init the app in given mode
     * 
     * @param mode
     *            RunMode which mode the app will run
     * @param os
     *            Optional OutputStream where the found referrer should go,when app
     *            is using manager mode
     */
    public Application(RunMode mode, OutputStream os) {
        try {
            Properties p = new Properties(System.getProperties());
            p.load(Application.class.getResourceAsStream("/app.properties"));
            
            if (mode == RunMode.MANAGER) {
                String managerRegister = p.getProperty("manager.register").toString();
                String managerEventUrl = p.getProperty("manager.eventUrl").toString();
                int managerPort = Integer.parseInt(p.getProperty("manager.port"));
                int timeWindow = Integer.parseInt(p.getProperty("timeWindow"));
                manager = new ManagerImpl(managerPort, managerRegister, managerEventUrl, timeWindow, os);
            } else {
                String workerRegister = p.getProperty("worker.register").toString();
                int workerPort = Integer.parseInt(p.getProperty("worker.port"));
                int timeWindow = Integer.parseInt(p.getProperty("timeWindow"));
                worker = new WorkerImpl(workerPort, workerRegister, timeWindow);
            }
            runningMode = mode;
        } catch (IOException e) {
            logger.error(e);
            System.exit(1);
        }
    }

    /**
     * init the app in given mode
     * 
     * @param mode
     *            RunMode which mode the app will run
     */
    public Application(RunMode mode) {
        this(mode, null);
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
                    if (!manager.processEvent(ev))
                        logger.warn("Failed to process:" + ev);
                }
            } catch (IOException e) {
                logger.error(e);
                break;
            }
        }
        if (!manager.stop())
            logger.warn("Failed to stop manager");
    }

    private boolean start() {
        if (null != manager) {
            return manager.start();
        }
        if (null != worker) {
            return worker.start() && worker.register();
        }
        return false;
    }

    public static RunMode getRunningMode() {
        return runningMode;
    }

    /**
     * Application start point. the app can run as a worker node or manager if -w or
     * --worker is given the app runs as worker, otherwise run as manager
     * 
     * @param args
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("w", "worker", false, "Run as worker mode, default mode: manager");
        try {
            CommandLine cmd = parser.parse(options, args);
            Application app;
            if (cmd.hasOption("w")) {
                app = new Application(RunMode.WORKER);
                app.start();
            } else {
                app = new Application(RunMode.MANAGER, System.out);
                app.start();
                app.readEvtStream(System.in);
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("EventApp", options);
            System.exit(1);
        }
        logger.info("Exit");
    }
}
