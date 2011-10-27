package org.ovirt.engine.core.common;

import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public final class VdcEventNotificationUtils {
    private static final java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> _eventNotificationTypeMap =
            new java.util.HashMap<EventNotificationEntity, java.util.HashSet<AuditLogType>>();

    /**
     * Initializes the <see cref="VdcEventNotificationUtils"/> class.
     */
    static {
        // VDC
        AddEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.VDC_STOP);
        // VDS
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_ACTIVATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_RECOVER_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_APPROVE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_INSTALL_FAILED);
        // VM
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_START);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_NOT_RESPONDING);
        // IRS
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
    }

    /**
     * Gets all notification events.
     *
     * @return
     */
    public static java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> GetNotificationEvents() {
        return _eventNotificationTypeMap;
    }

    /**
     * Gets notification events by type.
     *
     * @param type
     *            The type.
     * @return
     */
    public static java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> GetNotificationEventsByType(
            EventNotificationEntity type) {
        java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> map =
                new java.util.HashMap<EventNotificationEntity, java.util.HashSet<AuditLogType>>();
        if (_eventNotificationTypeMap.containsKey(type)) {
            map.put(type, _eventNotificationTypeMap.get(type));
        }
        return map;
    }

    /**
     * Gets the event entity.
     *
     * @param auditLogType
     *            Type of the audit log.
     * @return
     */
    public static EventNotificationEntity GetEventEntity(AuditLogType auditLogType) {
        EventNotificationEntity entity = EventNotificationEntity.UNKNOWN;
        String prefix = "";
        try {
            prefix = GetPrefix(auditLogType.name());
            entity = EnumUtils.valueOf(EventNotificationEntity.class, prefix, true);
        } catch (RuntimeException e) {
            log.warnFormat("{0}\nGetEventEntity: Unsupported AuditLogType prefix {1}", e.getMessage(), prefix);
        }
        return entity;
    }

    /**
     * Gets the prefix of a string until the first UNDERLINE character.
     *
     * @param s
     *            The s.
     * @return
     */
    private static String GetPrefix(String s) {
        final char UNDERLINE = '_';
        int i = s.indexOf(UNDERLINE);
        if (i > 0) {
            s = s.substring(0, i - 1);
        }
        return s;
    }

    /**
     * Adds an event notification entry.
     *
     * @param entity
     *            The entity.
     * @param auditLogType
     *            Type of the audit log.
     */
    private static void AddEventNotificationEntry(EventNotificationEntity entity, AuditLogType auditLogType) {
        java.util.HashSet<AuditLogType> entry;
        if (!_eventNotificationTypeMap.containsKey(entity)) {
            _eventNotificationTypeMap.put(entity, new java.util.HashSet<AuditLogType>());
            entry = _eventNotificationTypeMap.get(entity);
        } else {
            entry = _eventNotificationTypeMap.get(entity);
        }
        if (!entry.contains(auditLogType)) {
            entry.add(auditLogType);
        }
        _eventNotificationTypeMap.put(entity, entry);
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdcEventNotificationUtils.class);
}
