package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public final class VdcEventNotificationUtils {
    private static final Map<EventNotificationEntity, HashSet<AuditLogType>> _eventNotificationTypeMap =
            new HashMap<EventNotificationEntity, HashSet<AuditLogType>>();

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
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_TIME_DRIFT_ALERT);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_MEM_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_NETWORK_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_CPU_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_SWAP_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_LOW_SWAP);

        // VM
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_START);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_NOT_RESPONDING);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_RESTART_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_FAILED);
        // IRS
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        // GLUSTER
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_SET);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_ADD_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);

        // DWH
        AddEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_STOPPED);
        AddEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_ERROR);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
    }

    /**
     * Gets all notification events.
     *
     * @return
     */
    public static Map<EventNotificationEntity, HashSet<AuditLogType>> GetNotificationEvents() {
        return _eventNotificationTypeMap;
    }

    /**
     * Gets notification events by type.
     *
     * @param type
     *            The type.
     * @return
     */
    public static Map<EventNotificationEntity, HashSet<AuditLogType>> GetNotificationEventsByType(
            EventNotificationEntity type) {
        Map<EventNotificationEntity, HashSet<AuditLogType>> map =
                new HashMap<EventNotificationEntity, HashSet<AuditLogType>>();
        if (_eventNotificationTypeMap.containsKey(type)) {
            map.put(type, _eventNotificationTypeMap.get(type));
        }
        return map;
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
        HashSet<AuditLogType> entry;
        if (!_eventNotificationTypeMap.containsKey(entity)) {
            _eventNotificationTypeMap.put(entity, new HashSet<AuditLogType>());
            entry = _eventNotificationTypeMap.get(entity);
        } else {
            entry = _eventNotificationTypeMap.get(entity);
        }
        if (!entry.contains(auditLogType)) {
            entry.add(auditLogType);
        }
        _eventNotificationTypeMap.put(entity, entry);
    }

}
