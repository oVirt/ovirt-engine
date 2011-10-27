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

    // Main section: sub tabs

    // DataCenter

    public static final String SUB_TAB_PREFIX = "-";

    public static final String dataCenterStorageSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "storage";

    public static final String dataCenterNetworkSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "network";

    public static final String dataCenterClusterSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "cluster";

    public static final String dataCenterPermissionSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "permission";

    public static final String dataCenterEventSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX + "event";

    // Storage

    public static final String storageGeneralSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String storageDataCenterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "dataCenter";

    public static final String storageVmBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "vmBackup";

    public static final String storageTemplateBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "templateBackup";

    public static final String storageVmSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "vm";

    public static final String storageTemplateSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "template";

    public static final String storageIsoSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "iso";

    public static final String storagePermissionSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "permission";

    public static final String storageEventSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX + "event";

    // Cluster

    public static final String clusterGeneralSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String clusterHostSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "host";

    public static final String clusterVmSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "vm";

    public static final String clusterNetworkSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "network";

    public static final String clusterPermissionSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX + "permission";

    // VirtualMachine

    public static final String virtualMachineGeneralSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "general";

    public static final String virtualMachineNetworkInterfaceSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "network_interface";

    public static final String virtualMachineVirtualDiskSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "virtual_disk";

    public static final String virtualMachineSnapshotSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "snapshot";

    public static final String virtualMachineApplicationSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "application";

    public static final String virtualMachinePermissionSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "permission";

    public static final String virtualMachineEventSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX + "event";

    // Host

    public static final String hostGeneralSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String hostVmSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "vm";

    public static final String hostInterfaceSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "interfaces";

    public static final String hostHookSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "hooks";

    public static final String hostPermissionSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "permission";

    public static final String hostEventSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX + "event";

    // Pool

    public static final String poolGeneralSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String poolVmSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "vm";

    public static final String poolPermissionSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX + "permission";

    // Template

    public static final String templateGeneralSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String templateVmSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "vm";

    public static final String templateInterfaceSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "interface";

    public static final String templateDiskSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "disk";

    public static final String templateStorageSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "storage";

    public static final String templatePermissionSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "permission";

    public static final String templateEventSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX + "event";

    // User

    public static final String userGeneralSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "general";

    public static final String userGroupSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "group";

    public static final String userEventNotifierSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "eventNotify";

    public static final String userPermissionSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "permission";

    public static final String userEventSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX + "event";

}
