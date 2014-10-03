package org.ovirt.engine.core.notifier.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class designed to handle a proper shutdown in case of an external signal which was registered was caught by the
 * program.
 */
public class ShutdownHook extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

    private List<ScheduledExecutorService> schedulers = new LinkedList<>();
    private List<ScheduledFuture<?>> serviceHandlers = new LinkedList<>();

    private static volatile ShutdownHook instance;

    public static ShutdownHook getInstance() {
        if (instance == null) {
            synchronized(ShutdownHook.class) {
                if (instance == null) {
                    instance = new ShutdownHook();
                }
            }
        }
        return instance;
    }

    private ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        log.info("Preparing for shutdown after receiving signal " );
        for (ScheduledFuture<?> scheduled : serviceHandlers) {
            scheduled.cancel(true);
        }
        for (ScheduledExecutorService executer : schedulers) {
            executer.shutdown();
        }
        log.info("Event Notification service was shutdown");
    }

    public void addScheduledExecutorService(ScheduledExecutorService scheduler) {
        schedulers.add(scheduler);
    }

    public void addServiceHandler(ScheduledFuture<?> serviceHandler) {
        serviceHandlers.add(serviceHandler);
    }
}
