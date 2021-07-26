package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;

public final class VdcEventNotificationUtils {
    private static final Map<EventNotificationEntity, Set<AuditLogType>> eventNotificationTypeMap = new HashMap<>();

    /**
     * Initializes the <see cref="VdcEventNotificationUtils"/> class.
     */
    static {
        // VDC
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.VDC_STOP);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_STARTED);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_COMPLETED);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_BACKUP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_CA_CERTIFICATION_HAS_EXPIRED);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE);
        addEventNotificationEntry(EventNotificationEntity.Engine, AuditLogType.ENGINE_CERTIFICATION_HAS_EXPIRED);

        // VDS GROUP
        addEventNotificationEntry(EventNotificationEntity.Cluster, AuditLogType.CLUSTER_ALERT_HA_RESERVATION);
        addEventNotificationEntry(EventNotificationEntity.Cluster,
                AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM);
        addEventNotificationEntry(EventNotificationEntity.Cluster, AuditLogType.CLUSTER_ALERT_HA_RESERVATION_DOWN);
        // VDS
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_FAILURE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_UPDATES_ARE_AVAILABLE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE_MANUAL_HA);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_ACTIVATE_MANUAL_HA);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_ACTIVATE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_RECOVER_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_APPROVE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_INSTALL_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_TIME_DRIFT_ALERT);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_SET_NONOPERATIONAL_IFACE_DOWN);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_LOW_MEM);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_MEM_USE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_INTERFACE_HIGH_NETWORK_USE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_CPU_USE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_HIGH_SWAP_USE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_LOW_SWAP);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_INTERFACE_STATE_DOWN);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_BOND_SLAVE_STATE_DOWN);
        addEventNotificationEntry(EventNotificationEntity.Host,
                AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_CERTIFICATION_HAS_EXPIRED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.HOST_CERTIFICATE_HAS_INVALID_SAN);
        addEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.VDS_SET_NONOPERATIONAL_DOMAIN);
        addEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM);
        addEventNotificationEntry(EventNotificationEntity.VirtHost, AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN);
        // VM
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_FAILURE);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_START);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_MIGRATION_TO_SERVER_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_NOT_RESPONDING);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_STATUS_RESTORED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_RESTART_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.HA_VM_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_CONSOLE_CONNECTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_CONSOLE_DISCONNECTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_SET_TICKET);

        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_DOWN_ERROR);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VDS_INITIATED_RUN_VM_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_PAUSED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_PAUSED_EIO);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_PAUSED_ENOSPC);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_PAUSED_EPERM);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_PAUSED_ERROR);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.VM_RECOVERED_FROM_PAUSE_ERROR);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.MAC_ADDRESS_IS_EXTERNAL);
        // IRS
        addEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.VDS_SLOW_STORAGE_RESPONSE_TIME);
        addEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_FAILURE);
        addEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW);
        addEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_CONFIRMED_DISK_SPACE_LOW);
        addEventNotificationEntry(EventNotificationEntity.Storage, AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        addEventNotificationEntry(EventNotificationEntity.Storage,
                AuditLogType.NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD);
        // GLUSTER
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CREATE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_ADDED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_MODIFIED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_START_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_STOP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_ALL);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_DELETE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_STOP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_BRICK_REPLACED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_ADD_FAILED);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.GLUSTER_SERVER_REMOVE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_START_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_PROFILE_STOP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ENABLE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DISABLE);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DISABLE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DETECTED_NEW);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_DETECTED_DELETE);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ADDED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_ADD_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_REMOVED);
        addEventNotificationEntry(EventNotificationEntity.GlusterHook, AuditLogType.GLUSTER_HOOK_REMOVE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STARTED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_START_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STOPPED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_STOP_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_RESTARTED);
        addEventNotificationEntry(EventNotificationEntity.GlusterService, AuditLogType.GLUSTER_SERVICE_RESTART_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_BRICK_STATUS_CHANGED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW);

        // DWH
        addEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_STOPPED);
        addEventNotificationEntry(EventNotificationEntity.DWH, AuditLogType.DWH_ERROR);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_REBALANCE_FINISHED);
        addEventNotificationEntry(EventNotificationEntity.GlusterVolume, AuditLogType.GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED);

        //Trusted Service
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.VDS_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        addEventNotificationEntry(EventNotificationEntity.Vm, AuditLogType.USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);

        // Multipath
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.FAULTY_MULTIPATHS_ON_HOST);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.NO_FAULTY_MULTIPATHS_ON_HOST);
        addEventNotificationEntry(EventNotificationEntity.Host, AuditLogType.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST);
    }

    /**
     * Gets all notification events.
     */
    public static Map<EventNotificationEntity, Set<AuditLogType>> getNotificationEvents() {
        return eventNotificationTypeMap;
    }

    /**
     * Adds an event notification entry.
     *
     * @param entity
     *            The entity.
     * @param auditLogType
     *            Type of the audit log.
     */
    private static void addEventNotificationEntry(EventNotificationEntity entity, AuditLogType auditLogType) {
        eventNotificationTypeMap.computeIfAbsent(entity, k -> new HashSet<>()).add(auditLogType);
    }

}
