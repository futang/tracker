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

import tracker.datatypes.RunMode;
import tracker.interfaces.Manager;
import tracker.interfaces.EventService;

public class Application {
    private static EventService service;
    private static RunMode appMode;
    private static Logger logger = LogManager.getLogger();

    /**
     * init the app in given mode, if run the app as manager the input/output stream
     * must be present
     * 
     * @param mode
     *            RunMode which mode the app will run
     * @param in
     *            Optional InputStream from which the events come from
     * @param os
     *            Optional OutputStream where the found referrer should go
     */
    public Application(RunMode mode, InputStream in, OutputStream os) {
        try {
            Properties p = new Properties(System.getProperties());
            p.load(Application.class.getResourceAsStream("/app.properties"));
            service = new RoleFactory(p).getRunService(mode);
            if (service instanceof Manager) {
                ((Manager) service).setEventStreams(in, os);
            }
            appMode = mode;
        } catch (IOException e) {
            logger.error(e);
            System.exit(1);
        }
    }

    /**
     * init the app in worker mode
     * 
     */
    public Application() {
        this(RunMode.WORKER, null, null);
    }

    private void start() {
        service.run();
    }

    public static RunMode getRunningMode() {
        return appMode;
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
                app = new Application();
            } else {
                app = new Application(RunMode.MANAGER, System.in, System.out);
            }
            app.start();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("EventApp", options);
            System.exit(1);
        }
        logger.info("Exit");
    }
}
