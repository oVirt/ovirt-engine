package org.ovirt.engine.ui.uicommonweb.place;

/**
 * The central location of all application places.
 */
public class WebAdminApplicationPlaces {

    // Main section: main tabs

    public static final String dataCenterMainTabPlace = "dataCenters"; //$NON-NLS-1$

    public static final String clusterMainTabPlace = "clusters"; //$NON-NLS-1$

    public static final String hostMainTabPlace = "hosts"; //$NON-NLS-1$

    public static final String storageMainTabPlace = "storage"; //$NON-NLS-1$

    public static final String virtualMachineMainTabPlace = "vms"; //$NON-NLS-1$

    public static final String poolMainTabPlace = "pools"; //$NON-NLS-1$

    public static final String templateMainTabPlace = "templates"; //$NON-NLS-1$

    public static final String userMainTabPlace = "users"; //$NON-NLS-1$

    public static final String eventMainTabPlace = "events"; //$NON-NLS-1$

    public static final String reportsMainTabPlace = "reports"; //$NON-NLS-1$

    public static final String quotaMainTabPlace = "quota"; //$NON-NLS-1$

    public static final String volumeMainTabPlace = "volumes"; //$NON-NLS-1$

    public static final String diskMainTabPlace = "disks"; //$NON-NLS-1$

    public static final String networkMainTabPlace = "networks"; //$NON-NLS-1$

    public static final String vnicProfileMainTabPlace = "vnicProfiles"; //$NON-NLS-1$

    public static final String providerMainTabPlace = "providers"; //$NON-NLS-1$

    public static final String errataMainTabPlace = "errata"; //$NON-NLS-1$

    public static final String sessionMainTabPlace = "sessions"; //$NON-NLS-1$

    // Main section: sub tabs

    // Important: the value after SUB_TAB_PREFIX must correspond to given UiCommon model
    // hashName
    public static final String SUB_TAB_PREFIX = "-"; //$NON-NLS-1$

    // DataCenter

    public static final String dataCenterStorageSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "storage"; //$NON-NLS-1$

    public static final String dataCenterIscsiBondSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "iscsi_bond"; //$NON-NLS-1$

    public static final String dataCenterNetworkSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "logical_networks"; //$NON-NLS-1$

    public static final String dataCenterClusterSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "clusters"; //$NON-NLS-1$

    public static final String dataCenterQuotaSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "quota"; //$NON-NLS-1$

    public static final String dataCenterNetworkQoSSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "network_qos"; //$NON-NLS-1$

    public static final String dataCenterStorageQosSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "storage_qos"; //$NON-NLS-1$

    public static final String dataCenterCpuQosSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "cpu_qos"; //$NON-NLS-1$

    public static final String dataCenterHostNetworkQosSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "host_network_qos"; //$NON-NLS-1$

    public static final String dataCenterPermissionSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String dataCenterEventSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    public static final String dataCenterQosSubTabPlace = dataCenterMainTabPlace + SUB_TAB_PREFIX
            + "qos"; //$NON-NLS-1$

    // Storage

    public static final String storageGeneralSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String storageDataCenterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "data_center"; //$NON-NLS-1$

    public static final String storageVmBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "vm_import"; //$NON-NLS-1$

    public static final String storageTemplateBackupSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "template_import"; //$NON-NLS-1$

    public static final String storageVmRegisterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "vm_register"; //$NON-NLS-1$

    public static final String storageTemplateRegisterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "template_register"; //$NON-NLS-1$

    public static final String storageDisksImageRegisterSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "disk_image_register"; //$NON-NLS-1$

    public static final String storageVmSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String storageTemplateSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "templates"; //$NON-NLS-1$

    public static final String storageIsoSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "images"; //$NON-NLS-1$

    public static final String storageDiskSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "disks"; //$NON-NLS-1$

    public static final String storageRegisterDiskSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "register_disks"; //$NON-NLS-1$

    public static final String storageSnapshotSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "snapshots"; //$NON-NLS-1$

    public static final String storageDiskProfileSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "disk_profiles"; //$NON-NLS-1$

    public static final String storagePermissionSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String storageEventSubTabPlace = storageMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    // Cluster

    public static final String clusterGeneralSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String clusterPolicySubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "policy"; //$NON-NLS-1$

    public static final String clusterHostSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "hosts"; //$NON-NLS-1$

    public static final String clusterVmSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String clusterNetworkSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "logical_networks"; //$NON-NLS-1$

    public static final String clusterServiceSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "services"; //$NON-NLS-1$

    public static final String clusterGlusterHookSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "gluster_hooks"; //$NON-NLS-1$

    public static final String clusterAffinityGroupsSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "affinity_groups"; //$NON-NLS-1$

    public static final String clusterCpuProfileSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "cpu_profiles"; //$NON-NLS-1$

    public static final String clusterPermissionSubTabPlace = clusterMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    // VirtualMachine

    public static final String virtualMachineGeneralSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String virtualMachineNetworkInterfaceSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "network_interfaces"; //$NON-NLS-1$

    public static final String virtualMachineVirtualDiskSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "disks"; //$NON-NLS-1$

    public static final String virtualMachineHostDeviceSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "host_devices"; //$NON-NLS-1$

    public static final String virtualMachineSnapshotSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "snapshots"; //$NON-NLS-1$

    public static final String virtualMachineApplicationSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "applications"; //$NON-NLS-1$

    public static final String virtualMachineContainerSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "containers"; //$NON-NLS-1$

    public static final String virtualMachineVmDeviceSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "vm_devices"; //$NON-NLS-1$

    public static final String virtualMachineAffinityGroupsSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "affinity_groups"; //$NON-NLS-1$

    public static final String virtualMachinePermissionSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String virtualMachineGuestInfoSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "guest_info"; //$NON-NLS-1$

    public static final String virtualMachineEventSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    public static final String virtualMachineErrataSubTabPlace = virtualMachineMainTabPlace + SUB_TAB_PREFIX
            + "errata"; //$NON-NLS-1$

    // Host

    public static final String hostGeneralSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String hostGeneralSoftwareSubTabPlace = hostGeneralSubTabPlace
            + "_software"; //$NON-NLS-1$

    public static final String hostGeneralHardwareSubTabPlace = hostGeneralSubTabPlace
            + "_hardware"; //$NON-NLS-1$

    public static final String hostGeneralInfoSubTabPlace = hostGeneralSubTabPlace
            + "_info"; //$NON-NLS-1$

    public static final String hostGeneralErrataSubTabPlace = hostGeneralSubTabPlace
            + "_errata"; //$NON-NLS-1$

    public static final String hostVmSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String hostInterfaceSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "network_interfaces"; //$NON-NLS-1$

    public static final String hostDeviceSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "devices"; // $NON-NLS-1$

    public static final String hostHookSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "host_hooks"; //$NON-NLS-1$

    public static final String hostGlusterSwiftSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "gluster_swift"; //$NON-NLS-1$

    public static final String hostBricksSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "gluster_bricks"; //$NON-NLS-1$

    public static final String hostGlusterStorageDevicesSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "gluster_storage_devices"; //$NON-NLS-1$

    public static final String hostPermissionSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String hostEventSubTabPlace = hostMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    // Pool

    public static final String poolGeneralSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String poolVmSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String poolPermissionSubTabPlace = poolMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    // Template

    public static final String templateGeneralSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String templateVmSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String templateInterfaceSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "network_interfaces"; //$NON-NLS-1$

    public static final String templateDiskSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "disks"; //$NON-NLS-1$

    public static final String templateStorageSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "storage"; //$NON-NLS-1$

    public static final String templatePermissionSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String templateEventSubTabPlace = templateMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    // User

    public static final String userGeneralSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "general"; //$NON-NLS-1$

    public static final String userQuotaSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "quota"; //$NON-NLS-1$

    public static final String userGroupSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "directory_groups"; //$NON-NLS-1$

    public static final String userEventNotifierSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "event_notifier"; //$NON-NLS-1$

    public static final String userPermissionSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String userEventSubTabPlace = userMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    // Quota

    public static final String quotaClusterSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "clusters"; //$NON-NLS-1$

    public static final String quotaStorageSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "storage"; //$NON-NLS-1$

    public static final String quotaUserSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "users"; //$NON-NLS-1$

    public static final String quotaPermissionSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String quotaEventSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "events"; //$NON-NLS-1$

    public static final String quotaVmSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "vms"; //$NON-NLS-1$

    public static final String quotaTemplateSubTabPlace = quotaMainTabPlace + SUB_TAB_PREFIX
            + "templates"; //$NON-NLS-1$

    // Volumes

    public static final String volumeGeneralSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String volumeParameterSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "parameters"; //$NON-NLS-1$

    public static final String volumeBrickSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "bricks"; //$NON-NLS-1$

    public static final String volumePermissionSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    public static final String volumeEventSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    public static final String volumeGeoRepSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "geo_replication"; //$NON-NLS-1$

    public static final String volumeSnapshotSubTabPlace = volumeMainTabPlace + SUB_TAB_PREFIX + "volume_snapshots"; //$NON-NLS-1$

    // Disk

    public static final String diskGeneralSubTabPlace = diskMainTabPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String diskVmSubTabPlace = diskMainTabPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$

    public static final String diskTemplateSubTabPlace = diskMainTabPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$

    public static final String diskStorageSubTabPlace = diskMainTabPlace + SUB_TAB_PREFIX + "storage"; //$NON-NLS-1$

    public static final String diskPermissionSubTabPlace = diskMainTabPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    // Network
    public static final String networkGeneralSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String networkProfileSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "profiles"; //$NON-NLS-1$

    public static final String networkExternalSubnetSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "external_subnets"; //$NON-NLS-1$

    public static final String networkClusterSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "clusters"; //$NON-NLS-1$

    public static final String networkHostSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "hosts"; //$NON-NLS-1$

    public static final String networkVmSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String networkTemplateSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "templates"; //$NON-NLS-1$

    public static final String networkPermissionSubTabPlace = networkMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    // Provider
    public static final String providerGeneralSubTabPlace = providerMainTabPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String providerNetworkSubTabPlace = providerMainTabPlace + SUB_TAB_PREFIX
            + "networks"; //$NON-NLS-1$

    public static final String providerSecretSubTabPlace = providerMainTabPlace + SUB_TAB_PREFIX
            + "secrets"; //$NON-NLS-1$

    // Vnic Profile
    public static final String vnicProfilePermissionSubTabPlace = vnicProfileMainTabPlace + SUB_TAB_PREFIX
            + "permissions"; //$NON-NLS-1$

    public static final String vnicProfileVmSubTabPlace = vnicProfileMainTabPlace + SUB_TAB_PREFIX
            + "virtual_machines"; //$NON-NLS-1$

    public static final String vnicProfileTemplateSubTabPlace = vnicProfileMainTabPlace + SUB_TAB_PREFIX
            + "templates"; //$NON-NLS-1$

    // Errata
    public static final String errataDetailsSubTabPlace = errataMainTabPlace + SUB_TAB_PREFIX + "details"; //$NON-NLS-1$



    // Default places

    public static final String DEFAULT_MAIN_SECTION_PLACE = virtualMachineMainTabPlace;

}
