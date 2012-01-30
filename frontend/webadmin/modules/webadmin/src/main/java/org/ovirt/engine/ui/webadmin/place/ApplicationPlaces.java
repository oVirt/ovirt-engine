package org.ovirt.engine.ui.webadmin.place;

/**
 * The central location of all application places.
 */
public class ApplicationPlaces {

    // Login section

    public static final String loginPlace = "login";

    // Main section: main tabs

    public static final String dataCenterMainTabPlace = "dataCenters";

    public static String getDataCenterMainTabPlace() {
        return dataCenterMainTabPlace;
    }

    public static final String clusterMainTabPlace = "clusters";

    public static String getClusterMainTabPlace() {
        return clusterMainTabPlace;
    }

    public static final String hostMainTabPlace = "hosts";

    public static String getHostMainTabPlace() {
        return hostMainTabPlace;
    }

    public static final String storageMainTabPlace = "storage";

    public static String getStorageMainTabPlace() {
        return storageMainTabPlace;
    }

    public static final String virtualMachineMainTabPlace = "vms";

    public static String getVirtualMachineMainTabPlace() {
        return virtualMachineMainTabPlace;
    }

    public static final String poolMainTabPlace = "pools";

    public static String getPoolMainTabPlace() {
        return poolMainTabPlace;
    }

    public static final String templateMainTabPlace = "templates";

    public static String getTemplateMainTabPlace() {
        return templateMainTabPlace;
    }

    public static final String userMainTabPlace = "users";

    public static String getUserMainTabPlace() {
        return userMainTabPlace;
    }

    public static final String eventMainTabPlace = "events";

    public static String getEventMainTabPlace() {
        return eventMainTabPlace;
    }

    public static final String reportsMainTabPlace = "reports";

    public static final String quotaMainTabPlace = "quotas";

    public static String getQuotaMainTabPlace() {
        return quotaMainTabPlace;
    }

    // Main section: sub tabs
    public static String getReportsMainTabPlace() {
        return reportsMainTabPlace;
    }

    // Main section: sub tabs

    public static final String SUB_TAB_PREFIX = "-";

    // DataCenter

    public static final String dataCenterStorageSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "storage";

    public static final String dataCenterNetworkSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "logical_networks";

    public static final String dataCenterClusterSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "clusters";

    public static final String dataCenterQuotaSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "quotas";

    public static final String dataCenterPermissionSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "permissions";

    public static final String dataCenterEventSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "events";

    // Storage

    public static final String storageGeneralSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String storageDataCenterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "data_center";

    public static final String storageVmBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "vm_import";

    public static final String storageTemplateBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "template_import";

    public static final String storageVmSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "virtual_machines";

    public static final String storageTemplateSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "templates";

    public static final String storageIsoSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "images";

    public static final String storagePermissionSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "permissions";

    public static final String storageEventSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "events";

    // Cluster

    public static final String clusterGeneralSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String clusterHostSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "hosts";

    public static final String clusterVmSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "virtual_machines";

    public static final String clusterNetworkSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "logical_networks";

    public static final String clusterPermissionSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "permissions";

    // VirtualMachine

    public static final String virtualMachineGeneralSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "general";

    public static final String virtualMachineNetworkInterfaceSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "network_interfaces";

    public static final String virtualMachineVirtualDiskSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "virtual_disks";

    public static final String virtualMachineSnapshotSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "snapshots";

    public static final String virtualMachineApplicationSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "applications";

    public static final String virtualMachinePermissionSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "permissions";

    public static final String virtualMachineEventSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX + "events";

    // Host

    public static final String hostGeneralSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String hostVmSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "virtual_machines";

    public static final String hostInterfaceSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "network_interfaces";

    public static final String hostHookSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "host_hooks";

    public static final String hostPermissionSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "permissions";

    public static final String hostEventSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "events";

    // Pool

    public static final String poolGeneralSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String poolVmSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "virtual_machines";

    public static final String poolPermissionSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "permissions";

    // Template

    public static final String templateGeneralSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String templateVmSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "virtual_machines";

    public static final String templateInterfaceSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "network_interfaces";

    public static final String templateDiskSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "virtual_disks";

    public static final String templateStorageSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "storage";

    public static final String templatePermissionSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "permissions";

    public static final String templateEventSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "events";

    // User

    public static final String userGeneralSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String userQuotaSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "quotas";

    public static final String userGroupSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "directory_groups";

    public static final String userEventNotifierSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "event_notifier";

    public static final String userPermissionSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "permissions";

    public static final String userEventSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "events";

    // Quota

    public static final String quotaClusterSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "clusters";

    public static final String quotaStorageSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "storages";

    public static final String quotaUserSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "users";

    public static final String quotaPermissionSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "permissions";

    public static final String quotaEventSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "events";

    public static final String quotaVmSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX + "vms";;

}
