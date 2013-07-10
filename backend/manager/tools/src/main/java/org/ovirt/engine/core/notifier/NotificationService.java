package org.ovirt.engine.core.notifier;

import java.net.ConnectException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.methods.EventMethodFiller;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder.NotificationMethodFactoryMapper;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.EventSenderResult;
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
    private NotificationMethodFactoryMapper methodsMapper = null;
    private int daysToKeepHistory = 0;
    private int daysToSendOnStartup = 0;
    private EventSender failedQueriesEventSender;
    private List<EventAuditLogSubscriber> failedQueriesEventSubscribers = Collections.emptyList();
    private int failedQueriesNotificationThreshold;
    private int failedQueries = 0;

    public NotificationService(NotificationProperties prop) throws NotificationServiceException {
        this.prop = prop;
        initConnectivity();
        initConfigurationProperties();
        initEvents();
        initFailedQueriesEventSubscribers();
    }

    /**
     * Validates the correctness of properties set in the configuration file.<br>
     * If any of the properties is invalid, an error will be sent and service initialization fails.<br>
     * Validated properties are: <li>DAYS_TO_KEEP_HISTORY - property could be omitted, if specified should be a positive
     * <li>INTERVAL_IN_SECONDS - property is mandatory, if specified should be a positive <li>DB Connectivity
     * Credentials - if failed to obtain connection to database, fails
     * <li>FAILED_QUERIES_NOTIFICATION_THRESHOLD - send db connectivity notification once every x query attempts.
     *
     * @throws NotificationServiceException
     *             configuration setting error
     */
    private void initConfigurationProperties() throws NotificationServiceException {
        daysToKeepHistory = getNonNegativeIntegerProperty(NotificationProperties.DAYS_TO_KEEP_HISTORY);
        daysToSendOnStartup = getNonNegativeIntegerProperty(NotificationProperties.DAYS_TO_SEND_ON_STARTUP);
        failedQueriesNotificationThreshold =
                getNonNegativeIntegerProperty(NotificationProperties.FAILED_QUERIES_NOTIFICATION_THRESHOLD);
        if (failedQueriesNotificationThreshold == 0) {
            failedQueriesNotificationThreshold = 1;
        }
        initMethodMapper();
        failedQueriesEventSender =
                methodsMapper.getMethod(EventNotificationMethods.EMAIL);
    }

    private int getNonNegativeIntegerProperty(final String name) throws NotificationServiceException {
         // Get the text of the property:
         final String text = prop.getProperty(name);

         // Validate it:
         if (StringUtils.isNotEmpty(text)) {
             try {
                 int value = Integer.parseInt(text);
                 if (value < 0) {
                     throw new NumberFormatException(name + " value should be a positive number");
                 }
                 return value;
             }
             catch (NumberFormatException exception) {
                 String err =
                         String.format("Invalid format of %s: %s", name, text);
                 log.error(err, exception);
                 throw new NotificationServiceException(err, exception);
             }
         }

         // If the property can't be found then return 0 as the value:
         return 0;
    }

    /**
     * Executes event notification to subscribers
     */
    @Override
    public void run() {
        try {
            log.debug("Start event notification service iteration");
            processEvents();
            if (daysToKeepHistory > 0) {
                deleteObseleteHistoryData();
            }
            log.debug("Finish event notification service iteration");
        } catch (Throwable e) {
            if (!Thread.interrupted()) {
                log.error(String.format("Failed to run the service: [%s]", e.getMessage()), e);
            }
        }
    }

    // TODO: Consider adding deleteObseleteHistoryData() as a separate scheduled thread run on a daily basis
    private void deleteObseleteHistoryData() throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -daysToKeepHistory);
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

    private void markOldEventsAsProcessed() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -daysToSendOnStartup);
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
        }
        finally {
            DbUtils.closeQuietly(statement, connection);
        }

        if (updatedRecords > 0) {
            log.debug(updatedRecords + " old records were marked as processed in the \"audit_log\" table.");
        }
    }

    private void initMethodMapper() throws NotificationServiceException {
        EventMethodFiller methodFiller = new EventMethodFiller();
        Connection connection = null;
        try {
            connection = ds.getConnection();
            methodFiller.fillEventNotificationMethods(connection);
        } catch (Exception e) {
            throw new NotificationServiceException("Failed to initialize method mapper", e);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException exception) {
                    log.error("Failed to release connection", exception);
                }
            }
        }

        List<EventNotificationMethod> eventNotificationMethods = methodFiller.getEventNotificationMethods();
        methodsMapper = NotificationMethodMapBuilder.instance().createMethodsMapper(eventNotificationMethods, prop);
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
        }
        catch (SQLException exception) {
            throw new NotificationServiceException("Failed mark old events as processed.", exception);
        }
    }

    private void processEvents() throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EventAuditLogSubscriber> eventSubscribers  = new ArrayList<>();
        try {
            connection = ds.getConnection();
            ps =
                    connection
                            .prepareStatement("select * from event_audit_log_subscriber_view " +
                                    "where audit_log_id <= (select max(audit_log_id) from audit_log)");
            rs = ps.executeQuery();
            while (rs.next()) {
                eventSubscribers.add(getEventAuditLogSubscriber(rs));
            }

        } catch (SQLException e) {
            if (isConnectionException(e)){
                handleQueryFailure();
            }
            throw e;
        } finally {
            DbUtils.closeQuietly(rs, ps, connection);
        }
        DbUser dbUser = null;
        for (EventAuditLogSubscriber eventSubscriber:eventSubscribers) {
            dbUser = getUserByUserId(eventSubscriber.getsubscriber_id());
            if (dbUser != null) {
                EventSender method =
                        methodsMapper.getMethod(EventNotificationMethods.forValue(eventSubscriber.getmethod_id()));
                EventSenderResult sendResult = null;
                try {
                    sendResult = method.send(eventSubscriber, dbUser.getEmail());
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
    }

    private boolean isConnectionException(SQLException e) {
        return e.getCause() instanceof ConnectException;
    }

    private void handleQueryFailure() {
        if (failedQueries == 0) {
            try {
                for( EventAuditLogSubscriber failedQueriesEventSubscriber:failedQueriesEventSubscribers){
                    failedQueriesEventSubscriber.setlog_time(new Date());
                    failedQueriesEventSender.
                            send(failedQueriesEventSubscriber, failedQueriesEventSubscriber.getmethod_address());
                }
            } catch (Exception e) {
                log.error("Failed to dispatch query failure email message", e);
                // Don't rethrow. we don't want to mask the original query exception.
            }
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
            DbUtils.closeQuietly(ps,connection);
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
            DbUtils.closeQuietly(cs,connection);
        }
    }

    private event_notification_hist geteventNotificationHist(EventAuditLogSubscriber eals,
            boolean isNotified,
            String reason) {
        event_notification_hist eventHistory = new event_notification_hist();
        eventHistory.setaudit_log_id(eals.getaudit_log_id());
        eventHistory.setevent_name(eals.getevent_up_name());
        eventHistory.setmethod_type(EventNotificationMethods.forValue(eals.getmethod_id()).name());
        eventHistory.setreason(reason);
        eventHistory.setsent_at(new Date());
        eventHistory.setstatus(isNotified);
        eventHistory.setsubscriber_id(eals.getsubscriber_id());
        return eventHistory;
    }

    private DbUser getUserByUserId(Guid userId) throws SQLException {
        // Using preparedStatement instead of STP GetUserByUserId to skip handling supporting dialects
        // for MSSQL and PG. PG doesn't support parameter name which matches a column name. This is supported
        // by the backend, since using a plan JDBC, bypassing this issue by prepared statement.
        // in additional, required only partial email field of the DbUser
        Connection connection = null;
        Statement ps = null;
        ResultSet rs = null;
        DbUser dbUser = null;
        try {
            connection = ds.getConnection();
            ps = connection.createStatement();
            rs = ps.executeQuery(String.format("SELECT email FROM users WHERE user_id = '%s'", userId.toString()));
            if (rs.next()) {
                dbUser = new DbUser();
                dbUser.setId(userId);
                dbUser.setEmail(rs.getString("email"));
            }
        } finally {
            DbUtils.closeQuietly(rs,ps,connection);
        }
        return dbUser;
    }

    private EventAuditLogSubscriber getEventAuditLogSubscriber(ResultSet rs) throws SQLException {
        EventAuditLogSubscriber eals = new EventAuditLogSubscriber();
        eals.setevent_type(rs.getInt("event_type"));
        eals.setsubscriber_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("subscriber_id")));
        eals.setevent_up_name(rs.getString("event_up_name"));
        eals.setmethod_id(rs.getInt("method_id"));
        eals.setmethod_address(rs.getString("method_address"));
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
        for (String email:emailRecipients.split(",")){
            EventAuditLogSubscriber eals = new EventAuditLogSubscriber();
            eals.setevent_type(0);
            eals.setevent_up_name("DATABASE_UNREACHABLE");
            eals.setmethod_id(EventNotificationMethods.EMAIL.getValue());
            eals.setmethod_address(StringUtils.strip(email));
            eals.setmessage("Failed to query for notifications. Database Connection refused.");
            eals.setseverity(AuditLogSeverity.ERROR.getValue());
            failedQueriesEventSubscribers.add(eals);
        }
        this.failedQueriesEventSubscribers = failedQueriesEventSubscribers;
    }

}
