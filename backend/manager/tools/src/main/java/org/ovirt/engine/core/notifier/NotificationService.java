package org.ovirt.engine.core.notifier;

import java.net.ConnectException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.utils.NotificationMethodsMapper;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.EventSenderResult;
import org.ovirt.engine.core.notifier.utils.sender.mail.MessageHelper;
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;
import org.ovirt.engine.core.utils.db.DbUtils;

/**
 * Responsible for an execution of the service for the current events in the system which should be notified to the
 * subscribers.
 */
public class NotificationService implements Runnable {

    private static final Logger log = Logger.getLogger(NotificationService.class);

    private DataSource ds;
    private NotificationProperties prop = null;
    private NotificationMethodsMapper notificationMethodsMapper;
    private EventSender failedQueriesEventSender;
    private List<EventAuditLogSubscriber> failedQueriesEventSubscribers = Collections.emptyList();
    private int failedQueries = 0;

    public NotificationService(NotificationProperties prop) throws NotificationServiceException {
        this.prop = prop;
        initConnectivity();
        notificationMethodsMapper = new NotificationMethodsMapper(prop);
        failedQueriesEventSender = notificationMethodsMapper.getEventSender(EventNotificationMethod.EMAIL);
        initEvents();
        initFailedQueriesEventSubscribers();
    }

    /**
     * Executes event notification to subscribers
     */
    @Override
    public void run() {
        try {
            log.debug("Start event notification service iteration");
            processEvents();
            deleteObsoleteHistoryData();
            log.debug("Finish event notification service iteration");
        } catch (Throwable e) {
            if (!Thread.interrupted()) {
                log.error(String.format("Failed to run the service: [%s]", e.getMessage()), e);
            }
        }
    }

    private void deleteObsoleteHistoryData() throws SQLException {
        if (prop.getInteger(NotificationProperties.DAYS_TO_KEEP_HISTORY) > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -prop.getInteger(NotificationProperties.DAYS_TO_KEEP_HISTORY));
            Timestamp startDeleteFrom = new Timestamp(cal.getTimeInMillis());
            Connection connection = null;
            PreparedStatement deleteStmt = null;
            int deletedRecords;
            try {
                connection = ds.getConnection();
                deleteStmt = connection.prepareStatement("delete from event_notification_hist where sent_at < ?");
                deleteStmt.setTimestamp(1, startDeleteFrom);
                deletedRecords = deleteStmt.executeUpdate();
            } finally {
                if (deleteStmt != null) {
                    deleteStmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

            if (deletedRecords > 0) {
                log.debug(deletedRecords + " records were deleted from \"event_notification_hist\" table.");
            }
        }
    }

    private void markOldEventsAsProcessed() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -prop.getInteger(NotificationProperties.DAYS_TO_SEND_ON_STARTUP));
        Timestamp ts = new Timestamp(calendar.getTimeInMillis());
        Connection connection = null;
        PreparedStatement statement = null;
        int updatedRecords;
        try {
            connection = ds.getConnection();
            statement = connection.prepareStatement(
                "update audit_log set " +
                    "processed = 'true' " +
                "where " +
                    "processed = 'false' " +
                    "and log_time < ?"
            );
            statement.setTimestamp(1, ts);
            updatedRecords = statement.executeUpdate();
        } finally {
            DbUtils.closeQuietly(statement, connection);
        }

        if (updatedRecords > 0) {
            log.debug(updatedRecords + " old records were marked as processed in the \"audit_log\" table.");
        }
    }

    private void initConnectivity() throws NotificationServiceException {
        try {
            ds = new StandaloneDataSource();
        } catch (SQLException e) {
            throw new NotificationServiceException("Failed to obtain database connectivity", e);
        }
    }

    private void initEvents() throws NotificationServiceException {
        // Mark old events as processed so that during startup we don't send
        // all of them:
        try {
            markOldEventsAsProcessed();
        } catch (SQLException exception) {
            throw new NotificationServiceException("Failed mark old events as processed.", exception);
        }
    }

    private void processEvents() throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EventAuditLogSubscriber> eventSubscribers = new ArrayList<>();
        try {
            connection = ds.getConnection();
            ps =
                    connection
                            .prepareStatement("SELECT * " +
                                    "FROM event_audit_log_subscriber_view " +
                                    "WHERE audit_log_id <= (SELECT MAX(audit_log_id) FROM audit_log) " +
                                    "ORDER BY log_time ASC");
            rs = ps.executeQuery();
            while (rs.next()) {
                eventSubscribers.add(getEventAuditLogSubscriber(rs));
            }

        } catch (SQLException e) {
            if (isConnectionException(e)) {
                handleQueryFailure();
            }
            throw e;
        } finally {
            DbUtils.closeQuietly(rs, ps, connection);
        }
        for (EventAuditLogSubscriber eventSubscriber : eventSubscribers) {
            EventSender method =
                    notificationMethodsMapper.getEventSender(eventSubscriber.getEventNotificationMethod());
            EventSenderResult sendResult = null;
            try {
                sendResult = method.send(eventSubscriber);
            } catch (Exception e) {
                log.error("Failed to dispatch message", e);
                sendResult = new EventSenderResult();
                sendResult.setSent(false);
                sendResult.setReason(e.getMessage());
            }
            addEventNotificationHistory(geteventNotificationHist(eventSubscriber,
                    sendResult.isSent(),
                    sendResult.getReason()));
            updateAuditLogEventProcessed(eventSubscriber);
        }
    }

    private boolean isConnectionException(SQLException e) {
        return e.getCause() instanceof ConnectException;
    }

    private void handleQueryFailure() {
        if (failedQueries == 0) {
            try {
                for (EventAuditLogSubscriber failedQueriesEventSubscriber : failedQueriesEventSubscribers) {
                    failedQueriesEventSubscriber.setlog_time(new Date());
                    failedQueriesEventSender.
                            send(failedQueriesEventSubscriber);
                }
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

    private void updateAuditLogEventProcessed(EventAuditLogSubscriber eventSubscriber) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ds.getConnection();
            ps = connection.prepareStatement("update audit_log set processed = 'true' where audit_log_id = ?");
            ps.setLong(1, eventSubscriber.getaudit_log_id());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                log.error("Failed to mark audit_log entry as processed for audit_log_id: "
                        + eventSubscriber.getaudit_log_id());
            }
        } finally {
            DbUtils.closeQuietly(ps, connection);
        }
    }

    private void addEventNotificationHistory(event_notification_hist eventHistory)
            throws SQLException {

        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = ds.getConnection();
            cs = connection.prepareCall("{call Insertevent_notification_hist(?,?,?,?,?,?,?)}");
            cs.setLong(1, eventHistory.getaudit_log_id());
            cs.setString(2, eventHistory.getevent_name());
            cs.setString(3, eventHistory.getmethod_type());
            cs.setString(4, eventHistory.getreason());
            cs.setTimestamp(5, new java.sql.Timestamp(eventHistory.getsent_at().getTime()));
            cs.setBoolean(6, eventHistory.getstatus());
            cs.setString(7, eventHistory.getsubscriber_id().toString());
            cs.executeUpdate();
        } finally {
            DbUtils.closeQuietly(cs, connection);
        }
    }

    private event_notification_hist geteventNotificationHist(EventAuditLogSubscriber eals,
            boolean isNotified,
            String reason) {
        event_notification_hist eventHistory = new event_notification_hist();
        eventHistory.setaudit_log_id(eals.getaudit_log_id());
        eventHistory.setevent_name(eals.getevent_up_name());
        eventHistory.setmethod_type(eals.getEventNotificationMethod().name());
        eventHistory.setreason(reason);
        eventHistory.setsent_at(new Date());
        eventHistory.setstatus(isNotified);
        eventHistory.setsubscriber_id(eals.getsubscriber_id());
        return eventHistory;
    }

    private EventAuditLogSubscriber getEventAuditLogSubscriber(ResultSet rs) throws SQLException {
        EventAuditLogSubscriber eals = new EventAuditLogSubscriber();
        eals.setevent_type(rs.getInt("event_type"));
        eals.setsubscriber_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("subscriber_id")));
        eals.setevent_up_name(rs.getString("event_up_name"));
        eals.setEventNotificationMethod(EventNotificationMethod.valueOf(rs.getString("notification_method")));
        String methodAddress = rs.getString("method_address");
        if (methodAddress == null) {

        }
        eals.setmethod_address(methodAddress);
        eals.settag_name(rs.getString("tag_name"));
        eals.setaudit_log_id(rs.getLong("audit_log_id"));
        eals.setuser_id(Guid.createGuidFromString(rs.getString("user_id")));
        eals.setuser_name(rs.getString("user_name"));
        eals.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
        eals.setvm_name(rs.getString("vm_name"));
        eals.setvm_template_id(Guid.createGuidFromString(rs.getString("vm_template_id")));
        eals.setvm_template_name(rs.getString("vm_template_name"));
        eals.setvds_id(Guid.createGuidFromString(rs.getString("vds_id")));
        eals.setvds_name(rs.getString("vds_name"));
        eals.setstorage_pool_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_pool_id")));
        eals.setstorage_pool_name(rs.getString("storage_pool_name"));
        eals.setstorage_domain_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_domain_id")));
        eals.setstorage_domain_name(rs.getString("storage_domain_name"));
        eals.setlog_time(rs.getTimestamp("log_time"));
        eals.setseverity(rs.getInt("severity"));
        eals.setmessage(rs.getString("message"));
        return eals;
    }

    private void initFailedQueriesEventSubscribers() {
        String emailRecipients = prop.getProperty(NotificationProperties.FAILED_QUERIES_NOTIFICATION_RECIPIENTS, true);
        if (StringUtils.isEmpty(emailRecipients)) {
            return;
        }
        List<EventAuditLogSubscriber> failedQueriesEventSubscribers = new LinkedList<>();
        for (String email : emailRecipients.split(",")) {
            EventAuditLogSubscriber eals = new EventAuditLogSubscriber();
            eals.setevent_type(MessageHelper.MessageType.alertMessage.getEventType());
            eals.setevent_up_name("DATABASE_UNREACHABLE");
            eals.setEventNotificationMethod(EventNotificationMethod.EMAIL);
            eals.setmethod_address(StringUtils.strip(email));
            eals.setmessage("Failed to query for notifications. Database Connection refused.");
            eals.setseverity(AuditLogSeverity.ERROR.getValue());
            failedQueriesEventSubscribers.add(eals);
        }
        this.failedQueriesEventSubscribers = failedQueriesEventSubscribers;
    }

}
