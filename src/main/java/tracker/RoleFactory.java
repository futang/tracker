package tracker;

import java.util.Properties;

import tracker.datatypes.RunMode;
import tracker.impl.ManagerImpl;
import tracker.impl.WorkerImpl;
import tracker.interfaces.EventService;

public class RoleFactory {
    private static Properties config;
    public RoleFactory(Properties p) {
        config = p;
    }
    
    public EventService getRunService(RunMode mode) {
        if (mode == RunMode.MANAGER) {
            String managerRegister = config.getProperty("manager.register").toString();
            String managerEventUrl = config.getProperty("manager.eventUrl").toString();
            int managerPort = Integer.parseInt(config.getProperty("manager.port"));
            int timeWindow = Integer.parseInt(config.getProperty("timeWindow"));
            return new ManagerImpl(managerPort, managerRegister, managerEventUrl, timeWindow);
        } else {
            String workerRegister = config.getProperty("worker.register").toString();
            int workerPort = Integer.parseInt(config.getProperty("worker.port"));
            int timeWindow = Integer.parseInt(config.getProperty("timeWindow"));
            return new WorkerImpl(workerPort, workerRegister, timeWindow);
        }
        
    }
}
