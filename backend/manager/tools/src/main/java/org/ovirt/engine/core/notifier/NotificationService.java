package org.ovirt.engine.core.notifier;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.notifier.methods.EventMethodFiller;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder.NotificationMethodFactoryMapper;
import org.ovirt.engine.core.notifier.utils.NotificationConfigurator;
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

    private static final Log log = LogFactory.getLog(NotificationService.class);

    private DataSource ds;
    private Map<String, String> prop = null;
    private NotificationMethodFactoryMapper methodsMapper = null;
    private boolean shouldDeleteHistory = false;
    private int daysToKeepHistory;

    public NotificationService(NotificationConfigurator notificationConf) throws NotificationServiceException {
        this.prop = notificationConf.getProperties();
        initConnectivity();
        initConfigurationProperties();
    }

    /**
     * Validates the correctness of properties set in the configuration file.<br>
     * If any of the properties is invalid, an error will be sent and service initialization fails.<br>
     * Validated properties are: <li>DAYS_TO_KEEP_HISTORY - property could be omitted, if specified should be a positive
     * <li>INTERVAL_IN_SECONDS - property is mandatory, if specified should be a positive <li>DB Connectivity
     * Credentials - if failed to obtain connection to database, fails
     * @throws NotificationServiceException
     *             configuration setting error
     */
    private void initConfigurationProperties() throws NotificationServiceException {
        String daysHistoryStr = prop.get(NotificationProperties.DAYS_TO_KEEP_HISTORY);
        // verify property of history is well defined
        if (StringUtils.isNotEmpty(daysHistoryStr)) {
            try {
                daysToKeepHistory = Integer.valueOf(daysHistoryStr).intValue();
                if (daysToKeepHistory < 0) {
                    throw new NumberFormatException(NotificationProperties.DAYS_TO_KEEP_HISTORY
                            + " value should be a positive number");
                }
                daysToKeepHistory = daysToKeepHistory * -1;
                shouldDeleteHistory = true;
            } catch (NumberFormatException e) {
                String err =
                        String.format("Invalid format of %s: %s",
                                NotificationProperties.DAYS_TO_KEEP_HISTORY,
                                daysHistoryStr);
                log.error(err, e);
                throw new NotificationServiceException(err,e);
            }
        }
        initMethodMapper();
    }

    /**
     * Executes event notification to subscribers
     */
    public void run() {
        try {
            log.debug("Start event notification service iteration");
            processEvents();
            if (shouldDeleteHistory) {
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
        cal.add(Calendar.DATE, daysToKeepHistory);
        java.sql.Timestamp startDeleteFrom = new java.sql.Timestamp(cal.getTimeInMillis());
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
            log.debug(String.valueOf(deletedRecords) + " records were deleted from event_notification_hist table");
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

    private void processEvents() throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<EventAuditLogSubscriber> eventSubscribers  = new ArrayList<EventAuditLogSubscriber>();
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
                    sendResult = method.send(eventSubscriber, dbUser.getemail());
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
                dbUser.setuser_id(userId);
                dbUser.setemail(rs.getString("email"));
            }
        } finally {
            DbUtils.closeQuietly(rs,ps,connection);
        }
        return dbUser;
    }

    private EventAuditLogSubscriber getEventAuditLogSubscriber(ResultSet rs) throws SQLException {
        EventAuditLogSubscriber eals = new EventAuditLogSubscriber();
        eals.setevent_type(rs.getInt("event_type"));
        eals.setsubscriber_id(Guid.createGuidFromString(rs.getString("subscriber_id")));
        eals.setevent_up_name(rs.getString("event_up_name"));
        eals.setmethod_id(rs.getInt("method_id"));
        eals.setmethod_address(rs.getString("method_address"));
        eals.settag_name(rs.getString("tag_name"));
        eals.setaudit_log_id(rs.getLong("audit_log_id"));
        eals.setuser_id(NGuid.createGuidFromString(rs.getString("user_id")));
        eals.setuser_name(rs.getString("user_name"));
        eals.setvm_id(NGuid.createGuidFromString(rs.getString("vm_id")));
        eals.setvm_name(rs.getString("vm_name"));
        eals.setvm_template_id(NGuid.createGuidFromString(rs.getString("vm_template_id")));
        eals.setvm_template_name(rs.getString("vm_template_name"));
        eals.setvds_id(NGuid.createGuidFromString(rs.getString("vds_id")));
        eals.setvds_name(rs.getString("vds_name"));
        eals.setstorage_pool_id(Guid.createGuidFromString(rs.getString("storage_pool_id")));
        eals.setstorage_pool_name(rs.getString("storage_pool_name"));
        eals.setstorage_domain_id(Guid.createGuidFromString(rs.getString("storage_domain_id")));
        eals.setstorage_domain_name(rs.getString("storage_domain_name"));
        eals.setlog_time(rs.getTimestamp("log_time"));
        eals.setseverity(rs.getInt("severity"));
        eals.setmessage(rs.getString("message"));
        return eals;
    }

}
