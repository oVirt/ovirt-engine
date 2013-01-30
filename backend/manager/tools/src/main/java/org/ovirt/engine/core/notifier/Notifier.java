package org.ovirt.engine.core.notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.notifier.utils.NotificationConfigurator;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Main class of event notification service. Initiate the service and handles termination signals
 */
@SuppressWarnings("restriction")
public class Notifier {

    private static final Log log = LogFactory.getLog(Notifier.class);
    private static ScheduledExecutorService notifyScheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledExecutorService monitorScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long DEFAULT_NOTIFICATION_INTERVAL_IN_SECONDS = 120;
    private static final long DEFAULT_ENGINE_MONITOR_INTERVAL_IN_SECONDS = 300;

    /**
     * @param args
     *            [0] configuration file absolute path
     */
    public static void main(String[] args) {
        NotifierSignalHandler handler = new NotifierSignalHandler();
        Signal.handle(new Signal("HUP"), handler);
        handler.addScheduledExecutorService(notifyScheduler);
        handler.addScheduledExecutorService(monitorScheduler);

        NotificationService notificationService = null;
        EngineMonitorService engineMonitorService = null;
        NotificationConfigurator notificationConf = null;
        long engineMonitorInterval;
        long notificationInterval;
        try {
            String configurationFile = null;
            if (args != null && args.length == 1) {
                configurationFile = args[0];
            }
            notificationConf = new NotificationConfigurator(configurationFile);

            // This check will be not mandatory when SMS is implemented.
            String mailServer = notificationConf.getProperties().get(NotificationProperties.MAIL_SERVER);
            if ( mailServer == null || mailServer.isEmpty() ) {
                throw new IllegalArgumentException("Check configuration file, " + configurationFile + " MAIL_SERVER is missing");
            }

            notificationService = new NotificationService(notificationConf);
            engineMonitorService = new EngineMonitorService(notificationConf);

            notificationInterval =
                    notificationConf.getTimerInterval(NotificationProperties.INTERVAL_IN_SECONDS,
                            DEFAULT_NOTIFICATION_INTERVAL_IN_SECONDS);
            engineMonitorInterval =
                    notificationConf.getTimerInterval(NotificationProperties.ENGINE_INTERVAL_IN_SECONDS,
                            DEFAULT_ENGINE_MONITOR_INTERVAL_IN_SECONDS);

            // add notification service to scheduler with its configurable interval
            handler.addServiceHandler(notifyScheduler.scheduleWithFixedDelay(notificationService,
                    1,
                    notificationInterval,
                    TimeUnit.SECONDS));

            // add engine monitor service to scheduler with its configurable interval
            handler.addServiceHandler(monitorScheduler.scheduleWithFixedDelay(engineMonitorService,
                    1,
                    engineMonitorInterval,
                    TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("Failed to run the event notification service. ", e);
            if (notifyScheduler != null) {
                notifyScheduler.shutdown();
            }
            if (monitorScheduler != null) {
                monitorScheduler.shutdown();
            }
            // flag exit code to calling script after threads shut down.
            System.exit(1);
        }
    }

    /**
     * Class designed to handle a proper shutdown in case of an external signal which was registered was caught by the
     * program.
     */
    public static class NotifierSignalHandler implements SignalHandler {

        private List<ScheduledFuture<?>> serviceHandler = new ArrayList<ScheduledFuture<?>>();
        private List<ScheduledExecutorService> scheduler = new ArrayList<ScheduledExecutorService>();

        public void handle(Signal sig) {
            log.info("Preparing for shutdown after receiving signal " + sig);
            if (serviceHandler.size() > 0) {
                for (ScheduledFuture<?> scheduled : serviceHandler) {
                    scheduled.cancel(true);
                }
            }
            if (scheduler.size() > 0) {
                for (ScheduledExecutorService executer : scheduler) {
                    executer.shutdown();
                }
            }
            log.info("Event Notification service was shutdown");
            System.exit(0);
        }

        public void addScheduledExecutorService(ScheduledExecutorService scheduler) {
            this.scheduler.add(scheduler);
        }

        public void addServiceHandler(ScheduledFuture<?> serviceHandler) {
            this.serviceHandler.add(serviceHandler);
        }
    }
}

