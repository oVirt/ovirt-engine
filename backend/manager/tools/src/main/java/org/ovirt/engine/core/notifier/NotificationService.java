package org.ovirt.engine.core.notifier;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.AuditLogEvent;
import org.ovirt.engine.core.common.businessentities.AuditLogEventSubscriber;
import org.ovirt.engine.core.common.businessentities.UpDownEventFilter;
import org.ovirt.engine.core.notifier.dao.EventsManager;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.transport.EventSenderResult;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * Responsible for an execution of the service for the current events in the system which should be notified to the
 * subscribers.
 */
public class NotificationService implements Runnable {

    private static final Logger log = Logger.getLogger(NotificationService.class);

    private NotificationProperties prop = null;
    private final EventsManager eventsManager;
    private NotificationMethodsMapper notificationMethodsMapper;
    private List<AuditLogEventSubscriber> failedQueriesEventSubscribers = Collections.emptyList();
    private int failedQueries = 0;

    public NotificationService(NotificationProperties prop) throws NotificationServiceException {
        this.prop = prop;
        notificationMethodsMapper = new NotificationMethodsMapper(prop);
        this.eventsManager = new EventsManager();
        markOldEventsAsProcessed();
        failedQueriesEventSubscribers = initFailedQueriesEventSubscribers();
    }

    private void markOldEventsAsProcessed() {
        log.debug("Processing old events");
        final int updatedEvents =
                eventsManager.
                        markOldEventsAsProcessed(prop.getInteger(NotificationProperties.DAYS_TO_SEND_ON_STARTUP));
        if (updatedEvents > 0) {
            log.debug(updatedEvents + " old records were marked as processed in the \"audit_log\" table.");
        }
    }

    /**
     * Executes event notification to subscribers
     */
    @Override
    public void run() {
        try {
            log.debug("Start event notification service iteration");
            List<AuditLogEventSubscriber> eventSubscribers = eventsManager.getAuditLogEventSubscribers();
            List<AuditLogEvent> events = eventsManager.getAuditLogEvents(true);
            log.debug(String.format("%d unprocessed events read.", events.size()));
            distributeEvents(events, eventSubscribers);
            deleteObsoleteHistoryData();
            log.debug("Finished event notification service iteration");
        } catch (Throwable e) {
            if (isConnectionException(e)) {
                log.info("Connection exception while querying for notifications.", e);
                distributeDbDownEvent();
            }
            if (!Thread.interrupted()) {
                log.error(String.format("Failed to run the service: [%s]", e.getMessage()), e);
            }
        }
    }

    private void deleteObsoleteHistoryData() throws SQLException {
        if (prop.getInteger(NotificationProperties.DAYS_TO_KEEP_HISTORY) > 0) {
            final int deletedRecords =
                    eventsManager.
                            deleteObsoleteHistoryData(prop.getInteger(NotificationProperties.DAYS_TO_KEEP_HISTORY));
            if (deletedRecords > 0) {
                log.debug(deletedRecords + " records were deleted from \"event_notification_hist\" table.");
            }
        }
    }

    private void distributeEvents(List<AuditLogEvent> events, List<AuditLogEventSubscriber> eventSubscribers)
            throws SQLException {
        for (AuditLogEvent event : events) {
            distributeEvent(eventSubscribers, event);
        }
    }

    private void distributeEvent(List<AuditLogEventSubscriber> eventSubscribers, AuditLogEvent event) throws SQLException {
        for (AuditLogEventSubscriber subscriber : eventSubscribers) {
            if (subscriber.isSubscribed(event)) {
                Transport sender = notificationMethodsMapper.getEventSender(subscriber.getEventNotificationMethod());
                EventSenderResult sendResult;
                try {
                    sendResult = sender.send(event, subscriber);
                } catch (Exception e) {
                    log.error("Failed to dispatch message", e);
                    sendResult = new EventSenderResult();
                    sendResult.setSent(false);
                    sendResult.setReason(e.getMessage());
                }
                eventsManager.addEventNotificationHistoryRecord(event, subscriber, sendResult);
                eventsManager.updateAuditLogEventProcessed(event.getId(), log);
            }
        }
    }

    private boolean isConnectionException(Throwable e) {
        return e.getCause() instanceof ConnectException;
    }

    private void distributeDbDownEvent() {
        if (failedQueries == 0) {
            try {
                distributeEvents(eventsManager.getAuditLogEvents(true), failedQueriesEventSubscribers);
            } catch (Exception e) {
                log.error("Failed to dispatch query failure email message", e);
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

    private List<AuditLogEventSubscriber> initFailedQueriesEventSubscribers() {
        List<AuditLogEventSubscriber> failureSubscribers = new LinkedList<>();
        String subscribers = prop.getProperty(NotificationProperties.FAILED_QUERIES_NOTIFICATION_RECIPIENTS);
        if (!StringUtils.isEmpty(subscribers)) {
            for (String email : subscribers.split(",")) {
                AuditLogEventSubscriber dbDownSubscriber = new AuditLogEventSubscriber();
                dbDownSubscriber.setEventNotificationMethod(EventNotificationMethod.EMAIL);
                dbDownSubscriber.setMethodAddress(StringUtils.strip(email));
                final UpDownEventFilter eventFilter = new UpDownEventFilter();
                eventFilter.setEventUpName("DATABASE_UNREACHABLE");
                dbDownSubscriber.setEventFilter(eventFilter);
            }
        }
        return failureSubscribers;
    }

}
