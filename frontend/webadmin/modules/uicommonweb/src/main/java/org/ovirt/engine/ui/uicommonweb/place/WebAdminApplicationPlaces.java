package org.ovirt.engine.ui.uicommonweb.place;

/**
 * The central location of all application places.
 */
public final class WebAdminApplicationPlaces {

    // Main places (previously known as main tabs)

    public static final String dataCenterMainPlace = "dataCenters"; //$NON-NLS-1$
    public static final String clusterMainPlace = "clusters"; //$NON-NLS-1$
    public static final String hostMainPlace = "hosts"; //$NON-NLS-1$
    public static final String storageMainPlace = "storage"; //$NON-NLS-1$
    public static final String virtualMachineMainPlace = "vms"; //$NON-NLS-1$
    public static final String poolMainPlace = "pools"; //$NON-NLS-1$
    public static final String templateMainPlace = "templates"; //$NON-NLS-1$
    public static final String userMainPlace = "users"; //$NON-NLS-1$
    public static final String eventMainPlace = "events"; //$NON-NLS-1$
    public static final String quotaMainPlace = "quota"; //$NON-NLS-1$
    public static final String volumeMainPlace = "volumes"; //$NON-NLS-1$
    public static final String diskMainPlace = "disks"; //$NON-NLS-1$
    public static final String networkMainPlace = "networks"; //$NON-NLS-1$
    public static final String vnicProfileMainPlace = "vnicProfiles"; //$NON-NLS-1$
    public static final String providerMainPlace = "providers"; //$NON-NLS-1$
    public static final String errataMainPlace = "errata"; //$NON-NLS-1$
    public static final String sessionMainPlace = "sessions"; //$NON-NLS-1$

    // Detail places (previously known as sub tabs)

    // Important: the value after SUB_TAB_PREFIX must correspond to given UiCommon model hashName!
    public static final String SUB_TAB_PREFIX = "-"; //$NON-NLS-1$

    // DataCenter

    public static final String dataCenterStorageSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "storage"; //$NON-NLS-1$
    public static final String dataCenterIscsiBondSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "iscsi_bond"; //$NON-NLS-1$
    public static final String dataCenterNetworkSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "logical_networks"; //$NON-NLS-1$
    public static final String dataCenterClusterSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "clusters"; //$NON-NLS-1$
    public static final String dataCenterQuotaSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "quota"; //$NON-NLS-1$
    public static final String dataCenterPermissionSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String dataCenterEventSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$
    public static final String dataCenterQosSubTabPlace = dataCenterMainPlace + SUB_TAB_PREFIX + "qos"; //$NON-NLS-1$

    // Storage

    public static final String storageGeneralSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String storageDataCenterSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "data_center"; //$NON-NLS-1$
    public static final String storageVmBackupSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "vm_import"; //$NON-NLS-1$
    public static final String storageTemplateBackupSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "template_import"; //$NON-NLS-1$
    public static final String storageVmRegisterSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "vm_register"; //$NON-NLS-1$
    public static final String storageTemplateRegisterSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "template_register"; //$NON-NLS-1$
    public static final String storageDisksImageRegisterSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "disk_image_register"; //$NON-NLS-1$
    public static final String storageVmSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String storageTemplateSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$
    public static final String storageIsoSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "images"; //$NON-NLS-1$
    public static final String storageDiskSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "disks"; //$NON-NLS-1$
    public static final String storageRegisterDiskSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "register_disks"; //$NON-NLS-1$
    public static final String storageSnapshotSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "snapshots"; //$NON-NLS-1$
    public static final String storageDiskProfileSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "disk_profiles"; //$NON-NLS-1$
    public static final String storageDRSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "dr"; //$NON-NLS-1$
    public static final String storageLeaseSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "leases"; //$NON-NLS-1$
    public static final String storagePermissionSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String storageEventSubTabPlace = storageMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    // Cluster

    public static final String clusterGeneralSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String clusterHostSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "hosts"; //$NON-NLS-1$
    public static final String clusterVmSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String clusterNetworkSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "logical_networks"; //$NON-NLS-1$
    public static final String clusterServiceSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "services"; //$NON-NLS-1$
    public static final String clusterGlusterHookSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "gluster_hooks"; //$NON-NLS-1$
    public static final String clusterAffinityGroupsSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "affinity_groups"; //$NON-NLS-1$
    public static final String clusterAffinityLabelsSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "affinity_labels"; //$NON-NLS-1$
    public static final String clusterCpuProfileSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "cpu_profiles"; //$NON-NLS-1$
    public static final String clusterPermissionSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String clusterEventSubTabPlace = clusterMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    // VirtualMachine

    public static final String virtualMachineGeneralSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String virtualMachineNetworkInterfaceSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "network_interfaces"; //$NON-NLS-1$
    public static final String virtualMachineVirtualDiskSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "disks"; //$NON-NLS-1$
    public static final String virtualMachineHostDeviceSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "host_devices"; //$NON-NLS-1$
    public static final String virtualMachineSnapshotSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "snapshots"; //$NON-NLS-1$
    public static final String virtualMachineApplicationSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "applications"; //$NON-NLS-1$
    public static final String virtualMachineContainerSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "guest_containers"; //$NON-NLS-1$
    public static final String virtualMachineVmDeviceSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "vm_devices"; //$NON-NLS-1$
    public static final String virtualMachineAffinityGroupsSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "affinity_groups"; //$NON-NLS-1$
    public static final String virtualMachineAffinityLabelsSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "affinity_labels"; //$NON-NLS-1$
    public static final String virtualMachinePermissionSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String virtualMachineGuestInfoSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "guest_info"; //$NON-NLS-1$
    public static final String virtualMachineEventSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$
    public static final String virtualMachineErrataSubTabPlace = virtualMachineMainPlace + SUB_TAB_PREFIX + "errata"; //$NON-NLS-1$

    // Host

    public static final String hostGeneralSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String hostGeneralErrataSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "errata"; //$NON-NLS-1$
    public static final String hostVmSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String hostInterfaceSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "network_interfaces"; //$NON-NLS-1$
    public static final String hostDeviceSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "devices"; // $NON-NLS-1$
    public static final String hostHookSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "host_hooks"; //$NON-NLS-1$
    public static final String hostGlusterSwiftSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "gluster_swift"; //$NON-NLS-1$
    public static final String hostBricksSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "gluster_bricks"; //$NON-NLS-1$
    public static final String hostGlusterStorageDevicesSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "gluster_storage_devices"; //$NON-NLS-1$
    public static final String hostPermissionSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String hostEventSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$
    public static final String hostAffinityLabelsSubTabPlace = hostMainPlace + SUB_TAB_PREFIX + "affinity_labels"; //$NON-NLS-1$

    // Pool

    public static final String poolGeneralSubTabPlace = poolMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String poolVmSubTabPlace = poolMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String poolPermissionSubTabPlace = poolMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    // Template

    public static final String templateGeneralSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String templateVmSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String templateInterfaceSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "network_interfaces"; //$NON-NLS-1$
    public static final String templateDiskSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "disks"; //$NON-NLS-1$
    public static final String templateStorageSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "storage"; //$NON-NLS-1$
    public static final String templatePermissionSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String templateEventSubTabPlace = templateMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    // User

    public static final String userGeneralSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String userQuotaSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "quota"; //$NON-NLS-1$
    public static final String userGroupSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "directory_groups"; //$NON-NLS-1$
    public static final String userEventNotifierSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "event_notifier"; //$NON-NLS-1$
    public static final String userPermissionSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String userEventSubTabPlace = userMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    // Quota

    public static final String quotaClusterSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "clusters"; //$NON-NLS-1$
    public static final String quotaStorageSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "storage"; //$NON-NLS-1$
    public static final String quotaUserSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "users"; //$NON-NLS-1$
    public static final String quotaPermissionSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String quotaEventSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$
    public static final String quotaVmSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "vms"; //$NON-NLS-1$
    public static final String quotaTemplateSubTabPlace = quotaMainPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$

    // Volumes

    public static final String volumeGeneralSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String volumeParameterSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "parameters"; //$NON-NLS-1$
    public static final String volumeBrickSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "bricks"; //$NON-NLS-1$
    public static final String volumePermissionSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String volumeEventSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$
    public static final String volumeGeoRepSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "geo_replication"; //$NON-NLS-1$
    public static final String volumeSnapshotSubTabPlace = volumeMainPlace + SUB_TAB_PREFIX + "volume_snapshots"; //$NON-NLS-1$

    // Disk

    public static final String diskGeneralSubTabPlace = diskMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String diskVmSubTabPlace = diskMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String diskTemplateSubTabPlace = diskMainPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$
    public static final String diskStorageSubTabPlace = diskMainPlace + SUB_TAB_PREFIX + "storage"; //$NON-NLS-1$
    public static final String diskPermissionSubTabPlace = diskMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    // Network

    public static final String networkGeneralSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String networkProfileSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "profiles"; //$NON-NLS-1$
    public static final String networkExternalSubnetSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "external_subnets"; //$NON-NLS-1$
    public static final String networkClusterSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "clusters"; //$NON-NLS-1$
    public static final String networkHostSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "hosts"; //$NON-NLS-1$
    public static final String networkVmSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String networkTemplateSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$
    public static final String networkPermissionSubTabPlace = networkMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    // Provider

    public static final String providerGeneralSubTabPlace = providerMainPlace + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$
    public static final String providerNetworkSubTabPlace = providerMainPlace + SUB_TAB_PREFIX + "networks"; //$NON-NLS-1$
    public static final String providerSecretSubTabPlace = providerMainPlace + SUB_TAB_PREFIX + "secrets"; //$NON-NLS-1$

    // VnicProfile

    public static final String vnicProfilePermissionSubTabPlace = vnicProfileMainPlace + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$
    public static final String vnicProfileVmSubTabPlace = vnicProfileMainPlace + SUB_TAB_PREFIX + "virtual_machines"; //$NON-NLS-1$
    public static final String vnicProfileTemplateSubTabPlace = vnicProfileMainPlace + SUB_TAB_PREFIX + "templates"; //$NON-NLS-1$

    // Errata

    public static final String errataDetailsSubTabPlace = errataMainPlace + SUB_TAB_PREFIX + "details"; //$NON-NLS-1$

    // Default places

    public static final String DEFAULT_MAIN_SECTION_PLACE = virtualMachineMainPlace;

}
