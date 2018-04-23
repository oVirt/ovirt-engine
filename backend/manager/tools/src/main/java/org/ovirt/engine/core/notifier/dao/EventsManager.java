package org.ovirt.engine.core.notifier.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.NotificationServiceException;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.filter.AuditLogEventType;
import org.ovirt.engine.core.notifier.filter.FirstMatchSimpleFilter;
import org.ovirt.engine.core.notifier.transport.Observable;
import org.ovirt.engine.core.notifier.transport.Observer;
import org.ovirt.engine.core.utils.db.StandaloneDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsManager implements Observer {

    public static final String DATABASE_UNREACHABLE = "DATABASE_UNREACHABLE";

    private static final Logger log = LoggerFactory.getLogger(EventsManager.class);

    private DataSource ds;

    private Map<String, String> eventMap;

    public EventsManager() throws NotificationServiceException {
        try {
            ds = new StandaloneDataSource();
            eventMap = populateEventMap();
        } catch (SQLException e) {
            throw new NotificationServiceException("Failed to obtain database connectivity", e);
        }
    }

    private Map<String, String> populateEventMap() throws SQLException {
        Map<String, String> eventMap = new HashMap<>();
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT em.event_up_name, em.event_down_name " +
                     "FROM event_map em;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                eventMap.put(rs.getString("event_up_name"), rs.getString("event_down_name"));
            }
        }
        return eventMap;
    }

    public List<FirstMatchSimpleFilter.FilterEntry> getAuditLogEventSubscribers() throws SQLException {
        List<FirstMatchSimpleFilter.FilterEntry> eventSubscribers = new ArrayList<>();
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                            "SELECT event_subscriber.event_up_name, " +
                            "       event_subscriber.method_address, " +
                            "       event_subscriber.notification_method " +
                            "FROM event_subscriber ");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String eventUpName = rs.getString("event_up_name");
                String eventDownName = eventMap.get(eventUpName);
                eventSubscribers.add(
                        new FirstMatchSimpleFilter.FilterEntry(eventUpName,
                                null,
                                false,
                                rs.getString("notification_method"),
                                rs.getString("method_address")));

                if (eventDownName != null) {
                    eventSubscribers.add(
                            new FirstMatchSimpleFilter.FilterEntry(eventDownName,
                                    null,
                                    false,
                                    rs.getString("notification_method"),
                                    rs.getString("method_address")));

                }
            }
        }
        return eventSubscribers;
    }

    public List<AuditLogEvent> getAuditLogEvents() throws SQLException {
        Set<String> downEvents = new HashSet<>(eventMap.values());
        List<AuditLogEvent> auditLogEvents = new ArrayList<>();
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                            "SELECT al.audit_log_id, al.log_type_name, " +
                            "       al.user_id, al.user_name, " +
                            "       al.vm_id, al.vm_name, al.vm_template_id, al.vm_template_name, " +
                            "       al.vds_id, al.vds_name, al.storage_pool_id, al.storage_pool_name, " +
                            "       al.storage_domain_id, al.storage_domain_name, " +
                            "       al.log_time, al.severity, al.message " +
                            "FROM audit_log al " +
                            "WHERE al.processed = FALSE ;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                auditLogEvents.add(extractAuditLogEvent(rs, downEvents));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("{} unprocessed events read from audit_log database table.", auditLogEvents.size());
            for (int i = 0; i < auditLogEvents.size(); i++) {
                log.debug("event {} => {}", i, auditLogEvents.get(i).toString());
            }
        }
        return auditLogEvents;

    }

    private AuditLogEvent extractAuditLogEvent(ResultSet rs, Set<String> downEvents) throws SQLException {
        AuditLogEvent auditLogEvent = new AuditLogEvent();
        auditLogEvent.setId(rs.getLong("audit_log_id"));
        auditLogEvent.setLogTypeName(rs.getString("log_type_name"));
        if (downEvents.contains(auditLogEvent.getLogTypeName())) {
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

    public AuditLogEvent createDBDownEvent() {
        final AuditLogEvent dbDownEvent = new AuditLogEvent();
        dbDownEvent.setLogTime(new Date());
        dbDownEvent.setType(AuditLogEventType.alertMessage);
        dbDownEvent.setLogTypeName(DATABASE_UNREACHABLE);
        dbDownEvent.setMessage("Failed to query for notifications. Database Connection refused.");
        dbDownEvent.setSeverity(AuditLogSeverity.ERROR);
        return dbDownEvent;
    }

    public void markOldEventsAsProcessed(int daysToSendOnStartup) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -daysToSendOnStartup);
        Timestamp ts = new Timestamp(calendar.getTimeInMillis());
        int updatedRecords;
        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                    "UPDATE audit_log " +
                    "SET  processed = 'true' " +
                    "WHERE processed = 'false' AND log_time < ? ;")) {
            statement.setTimestamp(1, ts);
            updatedRecords = statement.executeUpdate();
            if (updatedRecords > 0) {
                log.debug("{} old records were marked as processed in the \"audit_log\" table.", updatedRecords);
            }
        } catch (SQLException e) {
            throw new NotificationServiceException("Failed mark old events as processed.", e);
        }
    }

    public void deleteObsoleteHistoryData(int daysToKeepHistory) throws SQLException {
        if (daysToKeepHistory > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -daysToKeepHistory);
            Timestamp startDeleteFrom = new Timestamp(cal.getTimeInMillis());
            int deletedRecords;
            try (Connection connection = ds.getConnection();
                 PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE " +
                        "FROM event_notification_hist " +
                        "WHERE sent_at < ? ;")) {
                deleteStmt.setTimestamp(1, startDeleteFrom);
                deletedRecords = deleteStmt.executeUpdate();
                if (deletedRecords > 0) {
                    log.debug("{} records were deleted from \"event_notification_hist\" table.", deletedRecords);
                }
            }
        }

    }

    public void updateAuditLogEventProcessed(long auditLogId)
            throws SQLException {
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                    "UPDATE audit_log " +
                    "SET processed = 'true' " +
                    "WHERE audit_log_id = ? ;")) {
            ps.setLong(1, auditLogId);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                log.error("Failed to mark audit_log entry as processed for audit_log_id: {}",
                        auditLogId);
            }
        }
    }

    @Override
    public void update(Observable o, DispatchResult dispatchResult) {
        AuditLogEvent event = dispatchResult.getEvent();
        try (Connection connection = ds.getConnection();
             CallableStatement cs = connection.prepareCall("{call Insertevent_notification_hist(?,?,?,?,?,?)}")) {
            cs.setLong(1, event.getId());
            cs.setString(2, event.getLogTypeName());
            cs.setString(3, dispatchResult.getNotificationMethod().name());
            cs.setString(4, dispatchResult.getErrorMessage());
            cs.setTimestamp(5, new Timestamp(new Date().getTime()));
            cs.setBoolean(6, dispatchResult.isSuccess());
            cs.executeUpdate();
        } catch (SQLException e) {
            log.error("Could not insert event notification history event", e);
        }
    }
}
