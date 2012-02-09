package org.ovirt.engine.core.notifier;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_audit_log_subscriber;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.notifier.methods.EventMethodFiller;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder;
import org.ovirt.engine.core.notifier.methods.NotificationMethodMapBuilder.NotificationMethodFactoryMapper;
import org.ovirt.engine.core.notifier.utils.ConnectionHelper;
import org.ovirt.engine.core.notifier.utils.ConnectionHelper.NaiveConnectionHelperException;
import org.ovirt.engine.core.notifier.utils.NotificationConfigurator;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.EventSenderResult;

/**
 * Responsible for an execution of the service for the current events in the system which should be notified to the
 * subscribers.
 */
public class NotificationService implements Runnable {

    private static final Log log = LogFactory.getLog(NotificationService.class);

    private ConnectionHelper connectionHelper = null;
    private Map<String, String> prop = null;
    private NotificationMethodFactoryMapper methodsMapper = null;
    private boolean shouldDeleteHistory = false;
    private int daysToKeepHistory;

    public NotificationService(NotificationConfigurator notificationConf) throws NotificationServiceException {
        this.prop = notificationConf.getProperties();
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
        initConnectivity();
        initMethodMapper();
    }

    /**
     * Executes event notification to subscribers
     */
    public void run() {
        try {
            log.debug("Start event notification service iteration");
            startup();
            processEvents();
            if (shouldDeleteHistory) {
                deleteObseleteHistoryData();
            }
            log.debug("Finish event notification service iteration");
        } catch (Throwable e) {
            if (!Thread.interrupted()) {
                log.error(String.format("Failed to run the service: [%s]", e.getMessage()), e);
            }
            // since notification service uses same instance to recurrent notification process -
            // only upon an exception the DB connection will be closed and will be reopened the next
            // time the service will start to run by calling ConnectionHelper.getConnection()
            // the connection won't be closed in a finally block since the service is planned to work constantly
            // against the DB and same connection could be served for a long period.
            shutdown();
        }
    }

    /**
     * Starts or verified that required resources for the service are available
     * @throws NotificationServiceException
     *             specifies which resource not available and a cause
     */
    private void startup() throws NotificationServiceException {
        try {
            if (connectionHelper == null) {
                connectionHelper = new ConnectionHelper(prop);
            } else {
                connectionHelper.getConnection();
            }
        } catch (NaiveConnectionHelperException e) {
            throw new NotificationServiceException("Failed to initialize the connection helper", e);
        }
    }

    /**
     * Releases any resources which is held by the service:<br>
     * <li>Close open DB connection
     */
    public void shutdown() {
        if (connectionHelper != null) {
            connectionHelper.closeConnection();
        }
    }

    // TODO: Consider adding deleteObseleteHistoryData() as a separate scheduled thread run on a daily basis
    private void deleteObseleteHistoryData() throws SQLException, NaiveConnectionHelperException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, daysToKeepHistory);
        java.sql.Timestamp startDeleteFrom = new java.sql.Timestamp(cal.getTimeInMillis());
        PreparedStatement deleteStmt = null;
        int deletedRecords;
        try {
            deleteStmt = connectionHelper.getConnection().prepareStatement("delete from event_notification_hist where sent_at < ?");
            deleteStmt.setTimestamp(1, startDeleteFrom);
            deletedRecords = deleteStmt.executeUpdate();
        } finally {
            if (deleteStmt != null) {
                deleteStmt.close();
            }
        }

        if (deletedRecords > 0) {
            log.debug(String.valueOf(deletedRecords) + " records were deleted from event_notification_hist table");
        }
    }

    private void initMethodMapper() throws NotificationServiceException {
        EventMethodFiller methodFiller = new EventMethodFiller();
        try {
            methodFiller.fillEventNotificationMethods(connectionHelper.getConnection());
        } catch (Exception e) {
            throw new NotificationServiceException("Failed to initialize method mapper", e);
        }

        List<event_notification_methods> eventNotificationMethods = methodFiller.getEventNotificationMethods();
        methodsMapper = NotificationMethodMapBuilder.instance().createMethodsMapper(eventNotificationMethods, prop);
    }

    private void initConnectivity() throws NotificationServiceException {
        try {
            connectionHelper = new ConnectionHelper(prop);
        } catch (NaiveConnectionHelperException e) {
            throw new NotificationServiceException("Failed to obtain database connectivity", e);
        }
    }

    private void processEvents() throws SQLException, NaiveConnectionHelperException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps =
                    connectionHelper.getConnection()
                            .prepareStatement("select * from event_audit_log_subscriber_view " +
                                    "where audit_log_id <= (select max(audit_log_id) from audit_log)");
            rs = ps.executeQuery();
            event_audit_log_subscriber eventSubscriber;
            DbUser dbUser = null;

            while (rs.next()) {
                eventSubscriber = getEventAuditLogSubscriber(rs);
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
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Failed to release resultset of event_audit_log_subscriber", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    log.error("Failed to release statement of event_audit_log_subscriber", e);
                    throw e;
                }
            }
        }
    }

    private void updateAuditLogEventProcessed(event_audit_log_subscriber eventSubscriber) throws SQLException,
            NaiveConnectionHelperException {
        PreparedStatement ps = null;
        try {
            ps =
                    connectionHelper.getConnection()
                            .prepareStatement("update audit_log set processed = 'true' where audit_log_id = ?");
            ps.setLong(1, eventSubscriber.getaudit_log_id());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                log.error("Failed to mark audit_log entry as processed for audit_log_id: "
                        + eventSubscriber.getaudit_log_id());
            }
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private void addEventNotificationHistory(event_notification_hist eventHistory)
            throws SQLException, NaiveConnectionHelperException {

        CallableStatement cs = null;
        try {
            cs = connectionHelper.getConnection().prepareCall("{call Insertevent_notification_hist(?,?,?,?,?,?,?)}");
            cs.setLong(1, eventHistory.getaudit_log_id());
            cs.setString(2, eventHistory.getevent_name());
            cs.setString(3, eventHistory.getmethod_type());
            cs.setString(4, eventHistory.getreason());
            cs.setTimestamp(5, new java.sql.Timestamp(eventHistory.getsent_at().getTime()));
            cs.setBoolean(6, eventHistory.getstatus());
            cs.setString(7, eventHistory.getsubscriber_id().toString());
            cs.executeUpdate();
        } finally {
            if (cs != null) {
                cs.close();
            }
        }
    }

    private event_notification_hist geteventNotificationHist(event_audit_log_subscriber eals,
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

    private DbUser getUserByUserId(Guid userId) throws SQLException, NaiveConnectionHelperException {
        // Using preparedStatement instead of STP GetUserByUserId to skip handling supporting dialects
        // for MSSQL and PG. PG doesn't support parameter name which matches a column name. This is supported
        // by the backend, since using a plan JDBC, bypassing this issue by prepared statement.
        // in additional, required only partial email field of the DbUser
        Statement ps = null;
        ResultSet rs = null;
        DbUser dbUser = null;
        try {
            ps = connectionHelper.getConnection().createStatement();
            rs = ps.executeQuery(String.format("SELECT email FROM users WHERE user_id = '%s'", userId.toString()));
            if (rs.next()) {
                dbUser = new DbUser();
                dbUser.setuser_id(userId);
                dbUser.setemail(rs.getString("email"));
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Failed to release resultset of db user query", e);
                }
            }
            if (ps != null) {
                ps.close();
            }
        }
        return dbUser;
    }

    private event_audit_log_subscriber getEventAuditLogSubscriber(ResultSet rs) throws SQLException {
        event_audit_log_subscriber eals = new event_audit_log_subscriber();
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
