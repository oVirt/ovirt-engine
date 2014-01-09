package org.ovirt.engine.core.notifier.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.AuditLogEvent;
import org.ovirt.engine.core.common.businessentities.AuditLogEventSubscriber;
import org.ovirt.engine.core.common.businessentities.AuditLogEventType;
import org.ovirt.engine.core.common.businessentities.UpDownEventFilter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.NotificationServiceException;
import org.ovirt.engine.core.notifier.transport.EventSenderResult;
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;
import org.ovirt.engine.core.utils.db.DbUtils;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventsManager {

    DataSource ds;

    public EventsManager() throws NotificationServiceException {
        try {
            ds = new StandaloneDataSource();
        } catch (SQLException e) {
            throw new NotificationServiceException("Failed to obtain database connectivity", e);
        }
    }

    public List<AuditLogEventSubscriber> getAuditLogEventSubscribers() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AuditLogEventSubscriber> eventSubscribers = new ArrayList<>();
        try {
            connection = ds.getConnection();
            ps =
                    connection.prepareStatement("" +
                            "SELECT event_subscriber.subscriber_id, event_subscriber.event_up_name, " +
                            "       event_subscriber.method_address, event_subscriber.notification_method, " +
                            "       event_map.event_down_name, users.username " +
                            "FROM event_subscriber " +
                            "INNER JOIN event_map USING ( event_up_name ) " +
                            "INNER JOIN users ON event_subscriber.subscriber_id = users.user_id;");

            rs = ps.executeQuery();
            while (rs.next()) {
                eventSubscribers.add(extractAuditLogEventSubscriber(rs));
            }

        } catch (SQLException e) {
            throw new NotificationServiceException("Failed to query for event subscribers.", e);
        } finally {
            DbUtils.closeQuietly(rs, ps, connection);
        }
        return eventSubscribers;
    }

    public List<AuditLogEvent> getAuditLogEvents(boolean dbDown) {
        List<AuditLogEvent> auditLogEvents = new ArrayList<>();
        if (dbDown) {
            auditLogEvents.add(createDBDownEvent());
        } else {
            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = ds.getConnection();
                ps =
                        connection.prepareStatement("" +
                                "SELECT al.audit_log_id, al.log_type_name, " +
                                "       em_up.event_up_name, em_down.event_down_name," +
                                "       al.user_id, al.user_name, " +
                                "       al.vm_id, al.vm_name, al.vm_template_id, al.vm_template_name, " +
                                "       al.vds_id, al.vds_name,al, al.storage_pool_id, al.storage_pool_name, " +
                                "       al.storage_domain_id, al.storage_domain_name, " +
                                "       al.log_time, al.severity, al.message " +
                                "FROM audit_log al " +
                                "LEFT JOIN event_map em_up   ON al.log_type_name = em_up.event_up_name " +
                                "LEFT JOIN event_map em_down ON al.log_type_name = em_down.event_down_name " +
                                "WHERE al.processed = FALSE ;");

                rs = ps.executeQuery();
                while (rs.next()) {
                    auditLogEvents.add(extractAuditLogEvent(rs));
                }
            } catch (SQLException e) {
                throw new NotificationServiceException("Failed to query for events.", e);
            } finally {
                DbUtils.closeQuietly(rs, ps, connection);
            }
        }
        return auditLogEvents;

    }

    private AuditLogEvent extractAuditLogEvent(ResultSet rs) throws SQLException {
        AuditLogEvent auditLogEvent = new AuditLogEvent();
        auditLogEvent.setId(rs.getLong("audit_log_id"));
        auditLogEvent.setLogTypeName(rs.getString("log_type_name"));
        final String eventDownName = rs.getString("event_down_name");
        if (eventDownName != null) {
            auditLogEvent.setType(AuditLogEventType.resolveMessage);
        } else {
            auditLogEvent.setType(AuditLogEventType.alertMessage);
        }
        auditLogEvent.setUserId(Guid.createGuidFromString(rs.getString("user_id")));
        auditLogEvent.setUserName(rs.getString("user_name"));
        auditLogEvent.setVmId(Guid.createGuidFromString(rs.getString("vm_id")));
        auditLogEvent.setVmName(rs.getString("vm_name"));
        auditLogEvent.setVmTemplateId(Guid.createGuidFromString(rs.getString("vm_template_id")));
        auditLogEvent.setVmTemplateName(rs.getString("vm_template_name"));
        auditLogEvent.setVdsId(Guid.createGuidFromString(rs.getString("vds_id")));
        auditLogEvent.setVdsName(rs.getString("vds_name"));
        auditLogEvent.setStoragePoolId(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_pool_id")));
        auditLogEvent.setStoragePoolName(rs.getString("storage_pool_name"));
        auditLogEvent.setStorageDomainId(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_domain_id")));
        auditLogEvent.setStorageDomainName(rs.getString("storage_domain_name"));
        auditLogEvent.setLogTime(rs.getTimestamp("log_time"));
        auditLogEvent.setSeverity(AuditLogSeverity.forValue(rs.getInt("severity")));
        auditLogEvent.setMessage(rs.getString("message"));
        return auditLogEvent;
    }

    private AuditLogEvent createDBDownEvent() {
        final AuditLogEvent dbDownEvent = new AuditLogEvent();
        dbDownEvent.setLogTime(new Date());
        dbDownEvent.setType(AuditLogEventType.alertMessage);
        dbDownEvent.setLogTypeName("DATABASE_UNREACHABLE");
        dbDownEvent.setMessage("Failed to query for notifications. Database Connection refused.");
        dbDownEvent.setSeverity(AuditLogSeverity.ERROR);
        return dbDownEvent;
    }

    private AuditLogEventSubscriber extractAuditLogEventSubscriber(ResultSet rs) throws SQLException {
        AuditLogEventSubscriber newSubscriber = new AuditLogEventSubscriber();
        newSubscriber.setEventNotificationMethod(EventNotificationMethod.valueOf(rs.getString("notification_method")));
        String email = rs.getString("method_address");
        if (StringUtils.isEmpty(email) &&
                newSubscriber.getEventNotificationMethod().equals(EventNotificationMethod.EMAIL)) {
            email = rs.getString("email");
        }
        newSubscriber.setMethodAddress(email);
        newSubscriber.setSubscriberId(Guid.createGuidFromString(rs.getString("subscriber_id")));
        newSubscriber.setUsername(rs.getString("username"));
        UpDownEventFilter eventFilter = new UpDownEventFilter();
        eventFilter.setEventUpName(rs.getString("event_up_name"));
        eventFilter.setEventDownName(rs.getString("event_down_name"));
        newSubscriber.setEventFilter(eventFilter);
        return newSubscriber;
    }

    public int markOldEventsAsProcessed(int daysToSendOnStartup) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -daysToSendOnStartup);
        Timestamp ts = new Timestamp(calendar.getTimeInMillis());
        Connection connection = null;
        PreparedStatement statement = null;
        int updatedRecords;
        try {
            connection = ds.getConnection();
            statement = connection.prepareStatement("" +
                    "UPDATE audit_log " +
                    "SET  processed = 'true' " +
                    "WHERE processed = 'false' AND log_time < ? ;"
            );
            statement.setTimestamp(1, ts);
            updatedRecords = statement.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationServiceException("Failed mark old events as processed.", e);
        } finally {
            DbUtils.closeQuietly(statement, connection);
        }
        return updatedRecords;
    }

    public int deleteObsoleteHistoryData(int daysToKeepHistory) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -daysToKeepHistory);
        Timestamp startDeleteFrom = new Timestamp(cal.getTimeInMillis());
        Connection connection = null;
        PreparedStatement deleteStmt = null;
        int deletedRecords;
        try {
            connection = ds.getConnection();
            deleteStmt = connection.prepareStatement("" +
                    "DELETE " +
                    "FROM event_notification_hist " +
                    "WHERE sent_at < ? ;");
            deleteStmt.setTimestamp(1, startDeleteFrom);
            deletedRecords = deleteStmt.executeUpdate();
        } finally {
            DbUtils.closeQuietly(deleteStmt, connection);
        }
        return deletedRecords;

    }

    public void updateAuditLogEventProcessed(long auditLogId, Logger errorLog)
            throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ds.getConnection();
            ps = connection.prepareStatement("" +
                    "UPDATE audit_log " +
                    "SET processed = 'true' " +
                    "WHERE audit_log_id = ? ;");
            ps.setLong(1, auditLogId);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                errorLog.error("Failed to mark audit_log entry as processed for audit_log_id: "
                        + auditLogId);
            }
        } finally {
            DbUtils.closeQuietly(ps, connection);
        }

    }

    public void addEventNotificationHistoryRecord(AuditLogEvent event,
                                                  AuditLogEventSubscriber subscriber,
                                                  EventSenderResult sendResult) {
        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = ds.getConnection();
            cs = connection.prepareCall("{call Insertevent_notification_hist(?,?,?,?,?,?,?)}");
            cs.setLong(1, event.getId());
            cs.setString(2, event.getLogTypeName());
            cs.setString(3, subscriber.getEventNotificationMethod().name());
            cs.setString(4, sendResult.getReason());
            cs.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
            cs.setBoolean(6, sendResult.isSent());
            cs.setString(7, subscriber.getSubscriberId() != null ? subscriber.getSubscriberId().toString() : null);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new NotificationServiceException("Could not insert event notification history event", e);
        } finally {
            DbUtils.closeQuietly(cs, connection);
        }

    }
}
