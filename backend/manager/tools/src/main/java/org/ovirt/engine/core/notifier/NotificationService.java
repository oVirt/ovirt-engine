package org.ovirt.engine.core.notifier;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.dao.EventsManager;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.filter.FirstMatchSimpleFilter;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for an execution of the service for the current events in the system which should be notified to the
 * subscribers.
 */
public class NotificationService implements Runnable {

    public static final String FILTER = "FILTER";

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationProperties prop;

    private final EventsManager eventsManager;

    private final FirstMatchSimpleFilter firstMatchSimpleFilter;

    private List<FirstMatchSimpleFilter.FilterEntry> configurationFilters;

    private List<Transport> transports = new LinkedList<>();

    private int failedQueries = 0;

    public NotificationService(NotificationProperties prop) throws NotificationServiceException {
        this.prop = prop;
        this.eventsManager = new EventsManager();
        firstMatchSimpleFilter = new FirstMatchSimpleFilter();
        configurationFilters = FirstMatchSimpleFilter.parse(prop.getProperty(FILTER));
    }

    private void markOldEventsAsProcessed() {
        eventsManager.markOldEventsAsProcessed(prop.getInteger(NotificationProperties.DAYS_TO_SEND_ON_STARTUP));
    }

    public void registerTransport(Transport transport){
        if (transport.isActive()) {
            firstMatchSimpleFilter.registerTransport(transport);
            transport.registerObserver(this.eventsManager);
            transports.add(transport);
        }
    }

    public boolean hasTransports() {
        return !transports.isEmpty();
    }

    @Override
    public void run() {
        markOldEventsAsProcessed();
        ShutdownHook shutdownHook = ShutdownHook.getInstance();
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        shutdownHook.addScheduledExecutorService(exec);
        shutdownHook.addServiceHandler(
                exec.scheduleWithFixedDelay(
                        () -> mainLogic(),
                        1,
                        prop.getLong(NotificationProperties.INTERVAL_IN_SECONDS),
                        TimeUnit.SECONDS
                )
        );
        shutdownHook.addServiceHandler(
                exec.scheduleWithFixedDelay(
                        () -> idle(),
                        1,
                        prop.getLong(NotificationProperties.IDLE_INTERVAL),
                        TimeUnit.SECONDS
                )
        );
    }

    /**
     * Executes event notification to subscribers
     */
    private void mainLogic() {
        try {
            try {
                log.debug("Start event notification service iteration");

                // Clear filter chain
                firstMatchSimpleFilter.clearFilterEntries();

                // Read Database subscriptions first
                firstMatchSimpleFilter.addFilterEntries(eventsManager.getAuditLogEventSubscribers());

                // Backward compatibility, aim to remove (can be replaced by "FILTER")
                String dbDownSubscribers =
                        prop.getProperty(NotificationProperties.FAILED_QUERIES_NOTIFICATION_RECIPIENTS, true);
                if (!StringUtils.isEmpty(dbDownSubscribers)) {
                    for (String subscriber : dbDownSubscribers.split(",")) {
                        FirstMatchSimpleFilter.FilterEntry subscriberEntry = new FirstMatchSimpleFilter.FilterEntry(
                                EventsManager.DATABASE_UNREACHABLE,
                                null,
                                false,
                                EventNotificationMethod.SMTP.getAsString(),
                                subscriber);
                        List<FirstMatchSimpleFilter.FilterEntry> subscriberEntries = Collections.singletonList(subscriberEntry);
                        firstMatchSimpleFilter.addFilterEntries(subscriberEntries);
                    }
                }

                // Add configurations subscription
                firstMatchSimpleFilter.addFilterEntries(
                        configurationFilters
                        );

                for (AuditLogEvent event : eventsManager.getAuditLogEvents()) {
                    firstMatchSimpleFilter.processEvent(event);
                    eventsManager.updateAuditLogEventProcessed(event.getId());
                }
                deleteObsoleteHistoryData();
                log.debug("Finished event notification service iteration");
            } catch (SQLException se) {
                distributeDbDownEvent();
                throw se;
            }
        } catch (Throwable t) {
            log.error("Failed to run the service.", t);
        }
    }

    private void idle() {
        log.debug("Begin idle iteration");
        for (Transport transport : transports) {
            transport.idle();
        }
        log.debug("Finished idle iteration");
    }

    private void deleteObsoleteHistoryData() throws SQLException {
        eventsManager.deleteObsoleteHistoryData(prop.getInteger(NotificationProperties.DAYS_TO_KEEP_HISTORY));
    }

    private void distributeDbDownEvent() {
        firstMatchSimpleFilter.clearFilterEntries();
        firstMatchSimpleFilter.addFilterEntries(
                configurationFilters
                );
        if (failedQueries == 0) {
            try {
                firstMatchSimpleFilter.processEvent(eventsManager.createDBDownEvent());
            } catch (Exception e) {
                log.error("Failed to dispatch {} event", EventsManager.DATABASE_UNREACHABLE, e);
                // Don't rethrow. we don't want to mask the original query exception.
            }
        }
        int failedQueriesNotificationThreshold =
                prop.getInteger(NotificationProperties.FAILED_QUERIES_NOTIFICATION_THRESHOLD);
        if (failedQueriesNotificationThreshold == 0) {
            failedQueriesNotificationThreshold = 1;
        }
        failedQueries = (failedQueries + 1) % failedQueriesNotificationThreshold;
    }

}
