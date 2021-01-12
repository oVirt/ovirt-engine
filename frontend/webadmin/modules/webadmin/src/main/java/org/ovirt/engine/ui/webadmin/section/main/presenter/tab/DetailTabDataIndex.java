package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.gwtplatform.mvp.client.TabData;

public final class DetailTabDataIndex {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    // clusters

    public static final TabData CLUSTER_GENERAL = new GroupedTabData(constants.clusterGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData CLUSTER_NETWORK = new GroupedTabData(constants.clusterNetworkSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData CLUSTER_HOST = new GroupedTabData(constants.clusterHostSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData CLUSTER_VM = new GroupedTabData(constants.clusterVmSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData CLUSTER_SERVICE = new GroupedTabData(constants.clusterServiceSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData CLUSTER_GLUSTER_HOOKS = new GroupedTabData(constants.clusterGlusterHooksSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData CLUSTER_AFFINITY_GROUP = new GroupedTabData(constants.affinityGroupSubTabLabel(), 6); //$NON-NLS-1$

    public static final TabData CLUSTER_AFFINITY_LABEL = new GroupedTabData(constants.affinityLabelsSubTabLabel(), 7); //$NON-NLS-1$

    public static final TabData CLUSTER_CPU_PROFILES = new GroupedTabData(constants.cpuProfilesSubTabLabel(), 8); //$NON-NLS-1$

    public static final TabData CLUSTER_PERMISSIONS = new GroupedTabData(constants.clusterPermissionSubTabLabel(), 9); //$NON-NLS-1$

    public static final TabData CLUSTER_EVENTS = new GroupedTabData(constants.clusterEventSubTabLabel(), 10); //$NON-NLS-1$


    // datacenter

    public static final TabData DATACENTER_STORAGE = new GroupedTabData(constants.dataCenterStorageSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData DATACENTER_NETWORKS = new GroupedTabData(constants.dataCenterNetworkSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData DATACENTER_ISCSI_MULTIPATHING = new GroupedTabData(constants.dataCenterIscsiMultipathingSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData DATACENTER_CLUSTERS = new GroupedTabData(constants.dataCenterClusterSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData DATACENTER_QOS = new GroupedTabData(constants.dataCenterQosSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData DATACENTER_QUOTA = new GroupedTabData(constants.dataCenterQuotaSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData DATACENTER_PERMISSIONS = new GroupedTabData(constants.dataCenterPermissionSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData DATACENTER_EVENTS = new GroupedTabData(constants.dataCenterEventSubTabLabel(), 6); //$NON-NLS-1$


    // disks

    public static final TabData DISKS_GENERAL = new GroupedTabData(constants.diskGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData DISKS_VMS = new GroupedTabData(constants.diskVmSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData DISKS_TEMPLATES = new GroupedTabData(constants.diskTemplateSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData DISKS_STORAGE = new GroupedTabData(constants.diskStorageSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData DISKS_PERMISSIONS = new GroupedTabData(constants.diskPermissionSubTabLabel(), 4); //$NON-NLS-1$


    // errata

    public static final TabData ERRATA_DETAILS = new GroupedTabData(constants.errataDetailsSubTabLabel(), 1); //$NON-NLS-1$


    // gluster

    public static final TabData GLUSTER_GENERAL = new GroupedTabData(constants.volumeGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData GLUSTER_BRICKS = new GroupedTabData(constants.volumeBrickSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData GLUSTER_PARAMETERS = new GroupedTabData(constants.volumeParameterSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData GLUSTER_PERMISSIONS = new GroupedTabData(constants.volumePermissionSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData GLUSTER_EVENTS = new GroupedTabData(constants.volumeEventSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData GLUSTER_GEO_REP = new GroupedTabData(constants.volumeGeoRepSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData GLUSTER_SNAPSHOTS = new GroupedTabData(constants.volumeSnapshotSubTabLabel(), 5); //$NON-NLS-1$


    // hosts

    public static final TabData HOSTS_GENERAL = new GroupedTabData(constants.hostGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData HOSTS_VMS = new GroupedTabData(constants.hostVmSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData HOSTS_IFACE = new GroupedTabData(constants.hostIfaceSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData HOSTS_DEVICES = new GroupedTabData(constants.hostDeviceSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData HOSTS_HOOKS = new GroupedTabData(constants.hostHookSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData HOSTS_BRICKS = new GroupedTabData(constants.hostBricksSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData HOSTS_GLUSTER_STORAGE_DEVICES = new GroupedTabData(constants.storageDevices(), 4); //$NON-NLS-1$

    public static final TabData HOSTS_PERMISSIONS = new GroupedTabData(constants.hostPermissionSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData HOSTS_AFFINITY_LABELS = new GroupedTabData(constants.affinityLabelsSubTabLabel(), 6); //$NON-NLS-1$

    public static final TabData HOSTS_ERRATA = new GroupedTabData(constants.hostGeneralErrataSubTabLabel(), 7); //$NON-NLS-1$

    public static final TabData HOSTS_EVENTS = new GroupedTabData(constants.hostEventSubTabLabel(), 7); //$NON-NLS-1$

    public static final TabData HOSTS_GLUSTER_SWIFT = new GroupedTabData(constants.hostGlusterSwiftSubTabLabel(), 8); //$NON-NLS-1$


    // network

    public static final TabData NETWORK_GENERAL = new GroupedTabData(constants.networkGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData NETWORK_PROFILE = new GroupedTabData(constants.vnicProfilesMainViewLabel(), 1); //$NON-NLS-1$

    public static final TabData NETWORK_EXTERNAL_SUBNET = new GroupedTabData(constants.networkExternalSubnetSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData NETWORK_CLUSTERS = new GroupedTabData(constants.networkClusterSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData NETWORK_HOST = new GroupedTabData(constants.networkHostSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData NETWORK_VM = new GroupedTabData(constants.networkVmSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData NETWORK_TEMPLATE = new GroupedTabData(constants.networkTemplateSubTabLabel(), 6); //$NON-NLS-1$

    public static final TabData NETWORK_PERMISSION = new GroupedTabData(constants.networkPermissionSubTabLabel(), 7); //$NON-NLS-1$


    // quota

    public static final TabData QUOTA_CLUSTER = new GroupedTabData(constants.quotaClusterSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData QUOTA_STORAGE = new GroupedTabData(constants.quotaStorageSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData QUOTA_VM = new GroupedTabData(constants.quotaVmSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData QUOTA_TEMPLATE = new GroupedTabData(constants.quotaTemplateSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData QUOTA_USER = new GroupedTabData(constants.quotaUserSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData QUOTA_PERMISSION = new GroupedTabData(constants.quotaPermissionSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData QUOTA_EVENT = new GroupedTabData(constants.quotaEventSubTabLabel(), 6); //$NON-NLS-1$


    // providers

    public static final TabData PROVIDER_GENERAL = new GroupedTabData(constants.providerGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData PROVIDER_NETWORK = new GroupedTabData(constants.providerNetworksSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData PROVIDER_SECRET = new GroupedTabData(constants.providerSecretsSubTabLabel(), 2); //$NON-NLS-1$


    // storage

    public static final TabData STORAGE_GENERAL = new GroupedTabData(constants.storageGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData STORAGE_DATA_CENTER = new GroupedTabData(constants.storageDataCenterSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData STORAGE_REGISTER_VMS = new GroupedTabData(constants.storageVmBackupSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData STORAGE_VM_BACKUP = new GroupedTabData(constants.storageVmBackupSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData STORAGE_REGISTER_TEMPLATES = new GroupedTabData(constants.storageTemplateBackupSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData STORAGE_TEMPLATE_BACKUP = new GroupedTabData(constants.storageTemplateBackupSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData STORAGE_REGISTER_DISK_IMAGE = new GroupedTabData(constants.storageDiskBackupSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData STORAGE_VMS = new GroupedTabData(constants.storageVmSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData STORAGE_TEMPLATES = new GroupedTabData(constants.storageTemplateSubTabLabel(), 6); //$NON-NLS-1$

    public static final TabData STORAGE_IMAGES = new GroupedTabData(constants.storageImagesSubTabLabel(), 7); //$NON-NLS-1$

    public static final TabData STORAGE_DISKS = new GroupedTabData(constants.disksLabel(), 8); //$NON-NLS-1$

    public static final TabData STORAGE_SNAPSHOTS = new GroupedTabData(constants.snapshotsLabel(), 9); //$NON-NLS-1$

    public static final TabData STORAGE_REGISTER_DISKS = new GroupedTabData(constants.registerDisksLabel(), 9); //$NON-NLS-1$

    public static final TabData STORAGE_LEASE = new GroupedTabData(constants.storageLeaseSubTabLabel(), 10); //$NON-NLS-1$

    public static final TabData STORAGE_DISK_PROFILES = new GroupedTabData(constants.diskProfilesSubTabLabel(), 11); //$NON-NLS-1$

    public static final TabData STORAGE_DR = new GroupedTabData(constants.storageDRSubTabLabel(), 12); //$NON-NLS-1$

    public static final TabData STORAGE_EVENTS = new GroupedTabData(constants.storageEventSubTabLabel(), 12); //$NON-NLS-1$

    public static final TabData STORAGE_PERMISSIONS = new GroupedTabData(constants.storagePermissionSubTabLabel(), 13); //$NON-NLS-1$


    // templates

    public static final TabData TEMPLATE_GENERAL = new GroupedTabData(constants.templateGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData TEMPLATE_VM = new GroupedTabData(constants.templateVmSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData TEMPLATE_INTERFACE = new GroupedTabData(constants.templateInterfaceSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData TEMPLATE_DISK = new GroupedTabData(constants.templateDiskSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData TEMPLATE_STORAGE = new GroupedTabData(constants.templateStorageSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData TEMPLATE_PERMISSION = new GroupedTabData(constants.templatePermissionSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData TEMPLATE_EVENT = new GroupedTabData(constants.templateEventSubTabLabel(), 6); //$NON-NLS-1$


    // pools

    public static final TabData POOL_GENERAL = new GroupedTabData(constants.poolGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData POOL_VM = new GroupedTabData(constants.poolVmSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData POOL_PERMISSION = new GroupedTabData(constants.poolPermissionSubTabLabel(), 2); //$NON-NLS-1$


    // virtual machines

    public static final TabData VIRTUALMACHINE_GENERAL = new GroupedTabData(constants.virtualMachineGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_ERRATA = new GroupedTabData(constants.virtualMachineErrataSubTabLabel(), 10); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_NETWORK_INTERFACE = new GroupedTabData(constants.virtualMachineNetworkInterfaceSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_EVENT = new GroupedTabData(constants.virtualMachineEventSubTabLabel(), 11); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_VIRTUAL_DISK = new GroupedTabData(constants.virtualMachineVirtualDiskSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_SNAPSHOT = new GroupedTabData(constants.virtualMachineSnapshotSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_GUEST_CONTAINER = new GroupedTabData(constants.virtualMachineContainerSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_APPLICATION = new GroupedTabData(constants.virtualMachineApplicationSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_HOST_DEVICE = new GroupedTabData(constants.virtualMachineHostDeviceSubTabLabel(), 5); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_VM_DEVICE = new GroupedTabData(constants.virtualMachineVmDevicesSubTabLabel(), 6); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_AFFINITY_GROUP = new GroupedTabData(constants.affinityGroupSubTabLabel(), 7); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_AFFINITY_LABEL = new GroupedTabData(constants.affinityLabelsSubTabLabel(), 8); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_PERMISSION = new GroupedTabData(constants.virtualMachinePermissionSubTabLabel(), 9); //$NON-NLS-1$

    public static final TabData VIRTUALMACHINE_GUEST_INFO = new GroupedTabData(constants.virtualMachineGuestInfoSubTabLabel(), 9); //$NON-NLS-1$


    // vnic profiles

    public static final TabData VNIC_PROFILE_VM = new GroupedTabData(constants.vnicProfileVmSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData VNIC_PROFILE_PERMISSION = new GroupedTabData(constants.vnicProfilePermissionSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData VNIC_PROFILE_TEMPLATE = new GroupedTabData(constants.vnicProfileTemplateSubTabLabel(), 1); //$NON-NLS-1$


    // users

    public static final TabData USER_GENERAL = new GroupedTabData(constants.userGeneralSubTabLabel(), 0); //$NON-NLS-1$

    public static final TabData USER_PERMISSION = new GroupedTabData(constants.userPermissionSubTabLabel(), 1); //$NON-NLS-1$

    public static final TabData USER_QUOTA = new GroupedTabData(constants.userQuotaSubTabLabel(), 2); //$NON-NLS-1$

    public static final TabData USER_GROUP = new GroupedTabData(constants.userGroupsSubTabLabel(), 3); //$NON-NLS-1$

    public static final TabData USER_EVENT_NOTIFIER = new GroupedTabData(constants.userEventNotifierSubTabLabel(), 4); //$NON-NLS-1$

    public static final TabData USER_EVENT = new GroupedTabData(constants.userEventSubTabLabel(), 5); //$NON-NLS-1$

}
