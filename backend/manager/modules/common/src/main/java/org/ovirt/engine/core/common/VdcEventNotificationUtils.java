package org.ovirt.engine.core.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public final class VdcEventNotificationUtils {
    private static final Map<EventNotificationEntity, HashSet<AuditLogType>> eventNotificationTypeMap =
            new HashMap<EventNotificationEntity, HashSet<AuditLogType>>();

    /**
     * Initializes the <see cref="VdcEventNotificationUtils"/> class.
     */
    static {
        // VDC
        AddEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.VDC_STOP);
        AddEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_STARTED);
        AddEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_COMPLETED);
        AddEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_FAILED);

        // VDS GROUP
        AddEventNotificationEntry(EventNotificationEntity.VdsGroup, AuditLogType.CLUSTER_ALERT_HA_RESERVATION);
        AddEventNotificationEntry(EventNotificationEntity.VdsGroup,
                AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM);
        AddEventNotificationEntry(EventNotificationEntity.VdsGroup, AuditLogType.CLUSTER_ALERT_HA_RESERVATION_DOWN);
        // VDS
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE_MANUAL_HA);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_ACTIVATE_MANUAL_HA);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_ACTIVATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_RECOVER_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_APPROVE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_INSTALL_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_TIME_DRIFT_ALERT);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_MEM_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_INTERFACE_HIGH_NETWORK_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_CPU_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_SWAP_USE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_LOW_SWAP);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_INTERFACE_STATE_DOWN);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_BOND_SLAVE_STATE_DOWN);
        AddEventNotificationEntry(EventNotificationEntity.Host,
                AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM);
        AddEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN);
        AddEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM);
        AddEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN);
        // VM
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_START);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_TO_SERVER_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_NOT_RESPONDING);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_STATUS_RESTORED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_RESTART_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_CONSOLE_CONNECTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_CONSOLE_DISCONNECTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_SET_TICKET);

        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_DOWN_ERROR);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VDS_INITIATED_RUN_VM_FAILED);
        // IRS
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_FAILURE);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW);
        AddEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        // GLUSTER
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_ADDED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_MODIFIED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_ALL);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_ADD_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE);
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DISABLE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DISABLE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DETECTED_NEW);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DETECTED_DELETE);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ADDED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ADD_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_REMOVED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_REMOVE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STARTED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_START_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STOPPED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STOP_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_RESTARTED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_RESTART_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_BRICK_STATUS_CHANGED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED);

        // DWH
        AddEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_STOPPED);
        AddEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_ERROR);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_FINISHED);
        AddEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED);

        //Trusted Service
        AddEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        AddEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
    }

    /**
     * Gets all notification events.
     *
     * @return
     */
    public static Map<EventNotificationEntity, HashSet<AuditLogType>> getNotificationEvents() {
        return eventNotificationTypeMap;
    }

    /**
     * Gets notification events by type.
     *
     * @param type
     *            The type.
     * @return
     */
    public static Map<EventNotificationEntity, HashSet<AuditLogType>> getNotificationEventsByType(
            EventNotificationEntity type) {
        Map<EventNotificationEntity, HashSet<AuditLogType>> map =
                new HashMap<EventNotificationEntity, HashSet<AuditLogType>>();
        if (eventNotificationTypeMap.containsKey(type)) {
            map.put(type, eventNotificationTypeMap.get(type));
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
        HashSet<AuditLogType> entry = eventNotificationTypeMap.get(entity);
        if (entry == null) {
            entry = new HashSet<AuditLogType>();
            eventNotificationTypeMap.put(entity, entry);
        }
        entry.add(auditLogType);
    }

}
