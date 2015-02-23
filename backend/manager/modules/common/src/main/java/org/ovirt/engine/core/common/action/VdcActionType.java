package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.ActionGroup;

public enum VdcActionType {
    Unknown(0, QuotaDependency.NONE),
    // Vm Commands
    AddVm(1, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    AddVmFromTemplate(2, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    AddVmFromScratch(3, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    RemoveVm(4, ActionGroup.DELETE_VM, QuotaDependency.STORAGE),
    UpdateVm(5, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.VDS_GROUP),
    RebootVm(6, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.NONE),
    StopVm(7, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.BOTH),
    ShutdownVm(8, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.VDS_GROUP),
    ChangeDisk(9, ActionGroup.CHANGE_VM_CD, QuotaDependency.NONE),
    PauseVm(10, QuotaDependency.NONE),
    HibernateVm(11, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.NONE),
    RunVm(12, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.VDS_GROUP),
    RunVmOnce(13, ActionGroup.VM_BASIC_OPERATIONS, QuotaDependency.BOTH),
    MigrateVm(14, ActionGroup.MIGRATE_VM, QuotaDependency.NONE),
    InternalMigrateVm(15, QuotaDependency.NONE),
    MigrateVmToServer(16, ActionGroup.MIGRATE_VM, QuotaDependency.NONE),
    ReorderVmNics(17, ActionGroup.CREATE_VM, false, QuotaDependency.NONE),
    VmLogon(18, ActionGroup.CONNECT_TO_VM, QuotaDependency.NONE),
    SetVmTicket(22, ActionGroup.CONNECT_TO_VM, false, QuotaDependency.NONE),
    ExportVm(23, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.NONE),
    ExportVmTemplate(24, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.NONE),
    RestoreStatelessVm(25, QuotaDependency.NONE),
    AddVmInterface(28, ActionGroup.CONFIGURE_VM_NETWORK, false, QuotaDependency.NONE),
    RemoveVmInterface(29, ActionGroup.CONFIGURE_VM_NETWORK, false, QuotaDependency.NONE),
    UpdateVmInterface(30, ActionGroup.CONFIGURE_VM_NETWORK, false, QuotaDependency.NONE),
    AddDisk(31, ActionGroup.CONFIGURE_VM_STORAGE, QuotaDependency.STORAGE),
    RegisterDisk(32, ActionGroup.CONFIGURE_VM_STORAGE, QuotaDependency.STORAGE),
    @Deprecated
    MoveVm(33, ActionGroup.MOVE_VM, QuotaDependency.NONE),
    UpdateVmDisk(34, ActionGroup.CONFIGURE_VM_STORAGE, false, QuotaDependency.STORAGE),
    AttachDiskToVm(180, ActionGroup.CONFIGURE_VM_STORAGE, false, QuotaDependency.NONE),
    DetachDiskFromVm(181, ActionGroup.CONFIGURE_VM_STORAGE, false, QuotaDependency.NONE),
    HotPlugDiskToVm(182, ActionGroup.CONFIGURE_VM_STORAGE, false, QuotaDependency.NONE),
    HotUnPlugDiskFromVm(183, ActionGroup.CONFIGURE_VM_STORAGE, false, QuotaDependency.NONE),
    HotSetNumberOfCpus(184, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.VDS_GROUP, true),
    VmSlaPolicy(185, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),
    ChangeFloppy(35, QuotaDependency.NONE),
    ImportVm(36, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    RemoveVmFromImportExport(37, ActionGroup.DELETE_VM, QuotaDependency.NONE),
    RemoveVmTemplateFromImportExport(38, ActionGroup.DELETE_TEMPLATE, QuotaDependency.NONE),
    ImportVmTemplate(39, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    ChangeVMCluster(40, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),
    CancelMigrateVm(41, ActionGroup.MIGRATE_VM, false, QuotaDependency.NONE),
    ActivateDeactivateVmNic(42, QuotaDependency.NONE),
    AddVmFromSnapshot(52, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    CloneVm(53, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    ImportVmFromConfiguration(43, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    UpdateVmVersion(44, QuotaDependency.NONE),
    ImportVmTemplateFromConfiguration(45, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    ProcessDownVm(46, QuotaDependency.NONE),
    // VdsCommands
    ProvisionVds(100, ActionGroup.CREATE_HOST, QuotaDependency.NONE),
    AddVds(101, ActionGroup.CREATE_HOST, QuotaDependency.NONE),
    UpdateVds(102, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveVds(103, ActionGroup.DELETE_HOST, false, QuotaDependency.NONE),
    RestartVds(104, ActionGroup.MANIPULATE_HOST, QuotaDependency.NONE),
    VdsNotRespondingTreatment(105, QuotaDependency.NONE),
    MaintenanceVds(106, QuotaDependency.NONE),
    MaintenanceNumberOfVdss(107, ActionGroup.MANIPULATE_HOST, false, QuotaDependency.NONE),
    ActivateVds(108, ActionGroup.MANIPULATE_HOST, QuotaDependency.NONE),
    InstallVdsInternal(109, QuotaDependency.NONE),
    ClearNonResponsiveVdsVms(110, QuotaDependency.NONE),
    ApproveVds(112, ActionGroup.CREATE_HOST, QuotaDependency.NONE),
    HandleVdsCpuFlagsOrClusterChanged(114, QuotaDependency.NONE),
    InitVdsOnUp(115, QuotaDependency.NONE),
    SetNonOperationalVds(117, QuotaDependency.NONE),
    AddVdsSpmId(119, QuotaDependency.NONE),
    ForceSelectSPM(120, QuotaDependency.NONE),
    // Fencing (including RestartVds above)
    StartVds(121, ActionGroup.MANIPULATE_HOST, QuotaDependency.NONE),
    StopVds(122, ActionGroup.MANIPULATE_HOST, QuotaDependency.NONE),
    HandleVdsVersion(124, QuotaDependency.NONE),
    ChangeVDSCluster(125, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
    RefreshHostCapabilities(126, ActionGroup.MANIPULATE_HOST, false, QuotaDependency.NONE),
    SshSoftFencing(127, QuotaDependency.NONE),
    VdsPowerDown(128, ActionGroup.MANIPULATE_HOST, QuotaDependency.NONE),
    UpgradeOvirtNodeInternal(129, QuotaDependency.NONE),
    InstallVds(130, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
    UpgradeOvirtNode(131, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
    VdsKdumpDetection(132, QuotaDependency.NONE),

    // Network
    UpdateNetworkToVdsInterface(149, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    AttachNetworkToVdsInterface(150, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    DetachNetworkFromVdsInterface(151, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    AddBond(152, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    RemoveBond(153, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    AddNetwork(154, ActionGroup.CREATE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    RemoveNetwork(155, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    UpdateNetwork(156, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    CommitNetworkChanges(157, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    SetupNetworks(158, ActionGroup.CONFIGURE_HOST_NETWORK, QuotaDependency.NONE),
    PersistentSetupNetworks(159, QuotaDependency.NONE),

    // VnicProfile Commands
    AddVnicProfile(160, ActionGroup.CREATE_NETWORK_VNIC_PROFILE, false, QuotaDependency.NONE),
    UpdateVnicProfile(161, ActionGroup.CONFIGURE_NETWORK_VNIC_PROFILE, false, QuotaDependency.NONE),
    RemoveVnicProfile(162, ActionGroup.DELETE_NETWORK_VNIC_PROFILE, false, QuotaDependency.NONE),

    // Network labels
    LabelNetwork(163, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    UnlabelNetwork(164, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    LabelNic(165, ActionGroup.CONFIGURE_HOST_NETWORK, false, QuotaDependency.NONE),
    UnlabelNic(166, ActionGroup.CONFIGURE_HOST_NETWORK, false, QuotaDependency.NONE),

    // NUMA
    AddVmNumaNodes(170, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),
    UpdateVmNumaNodes(171, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),
    RemoveVmNumaNodes(172, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),

    // VmTemplatesCommand
    AddVmTemplate(201, ActionGroup.CREATE_TEMPLATE, QuotaDependency.BOTH),
    UpdateVmTemplate(202, ActionGroup.EDIT_TEMPLATE_PROPERTIES, QuotaDependency.VDS_GROUP),
    RemoveVmTemplate(203, ActionGroup.DELETE_TEMPLATE, QuotaDependency.STORAGE),
    MoveOrCopyTemplate(226, ActionGroup.COPY_TEMPLATE, QuotaDependency.STORAGE),
    AddVmTemplateInterface(220, ActionGroup.CONFIGURE_TEMPLATE_NETWORK, false, QuotaDependency.NONE),
    RemoveVmTemplateInterface(221, ActionGroup.CONFIGURE_TEMPLATE_NETWORK, false, QuotaDependency.NONE),
    UpdateVmTemplateInterface(222, ActionGroup.CONFIGURE_TEMPLATE_NETWORK, false, QuotaDependency.NONE),
    // ImagesCommands
    TryBackToSnapshot(204, QuotaDependency.NONE),
    RestoreFromSnapshot(205, QuotaDependency.STORAGE),
    CreateAllSnapshotsFromVm(206, ActionGroup.MANIPULATE_VM_SNAPSHOTS, QuotaDependency.STORAGE),
    CreateSnapshot(207, QuotaDependency.STORAGE),
    CreateSnapshotFromTemplate(208, QuotaDependency.STORAGE),
    CreateImageTemplate(209, QuotaDependency.STORAGE),
    RemoveSnapshot(210, ActionGroup.MANIPULATE_VM_SNAPSHOTS, QuotaDependency.STORAGE),
    RemoveImage(211, QuotaDependency.STORAGE),
    RemoveAllVmImages(212, QuotaDependency.STORAGE),
    AddImageFromScratch(213, QuotaDependency.STORAGE),
    RemoveTemplateSnapshot(215, QuotaDependency.STORAGE),
    RemoveAllVmTemplateImageTemplates(216, QuotaDependency.STORAGE),
    TryBackToAllSnapshotsOfVm(223, ActionGroup.MANIPULATE_VM_SNAPSHOTS, QuotaDependency.NONE),
    RestoreAllSnapshots(224, ActionGroup.MANIPULATE_VM_SNAPSHOTS, QuotaDependency.STORAGE),
    CopyImageGroup(225, QuotaDependency.STORAGE),
    MoveOrCopyDisk(228, QuotaDependency.STORAGE),
    RemoveSnapshotSingleDisk(227, QuotaDependency.STORAGE),
    CreateCloneOfTemplate(229, QuotaDependency.STORAGE),
    RemoveDisk(230, QuotaDependency.STORAGE),
    MoveImageGroup(231, QuotaDependency.STORAGE),
    GetDiskAlignment(232, QuotaDependency.NONE),
    RemoveVmHibernationVolumes(233, QuotaDependency.NONE),
    RemoveMemoryVolumes(234, QuotaDependency.NONE),
    RemoveDiskSnapshots(235, ActionGroup.MANIPULATE_VM_SNAPSHOTS, QuotaDependency.NONE),
    RemoveSnapshotSingleDiskLive(236, QuotaDependency.STORAGE),
    Merge(237, QuotaDependency.STORAGE),
    MergeStatus(238, QuotaDependency.NONE),
    DestroyImage(239, QuotaDependency.STORAGE),
    // VmPoolCommands
    AddVmPool(301, QuotaDependency.NONE),
    AddVmPoolWithVms(304, ActionGroup.CREATE_VM_POOL, QuotaDependency.BOTH),
    UpdateUserVm(303, QuotaDependency.NONE),
    UpdateVmPoolWithVms(305, ActionGroup.EDIT_VM_POOL_CONFIGURATION, QuotaDependency.STORAGE),
    AddVmAndAttachToPool(306, QuotaDependency.NONE),
    RemoveVmPool(307, ActionGroup.DELETE_VM_POOL, QuotaDependency.NONE),
    DetachUserFromVmFromPool(312, QuotaDependency.NONE),
    AddVmToPool(313, QuotaDependency.NONE),
    RemoveVmFromPool(314, ActionGroup.EDIT_VM_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    AttachUserToVmFromPoolAndRun(318, ActionGroup.VM_POOL_BASIC_OPERATIONS, QuotaDependency.VDS_GROUP),
    // UserAndGroupsCommands
    LoginUser(406, ActionGroup.LOGIN, false, QuotaDependency.NONE),
    LogoutUser(408, false, QuotaDependency.NONE),
    LogoutBySession(410, false, QuotaDependency.NONE),
    RemoveUser(409, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    RemoveGroup(415, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    ChangeUserPassword(416, QuotaDependency.NONE),
    LoginAdminUser(418, ActionGroup.LOGIN, false, QuotaDependency.NONE),
    AddUser(419, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    AddGroup(420, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    // Tags
    AddTag(501, false, QuotaDependency.NONE),
    RemoveTag(502, false, QuotaDependency.NONE),
    UpdateTag(503, false, QuotaDependency.NONE),
    MoveTag(504, false, QuotaDependency.NONE),
    AttachUserToTag(505, false, QuotaDependency.NONE),
    DetachUserFromTag(506, false, QuotaDependency.NONE),
    AttachUserGroupToTag(507, false, QuotaDependency.NONE),
    DetachUserGroupFromTag(508, false, QuotaDependency.NONE),
    AttachVmsToTag(509, false, QuotaDependency.NONE),
    DetachVmFromTag(510, false, QuotaDependency.NONE),
    AttachVdsToTag(511, false, QuotaDependency.NONE),
    DetachVdsFromTag(512, false, QuotaDependency.NONE),
    UpdateTagsVmMapDefaultDisplayType(515, false, QuotaDependency.NONE),
    AttachTemplatesToTag(516, false, QuotaDependency.NONE),
    DetachTemplateFromTag(517, false, QuotaDependency.NONE),

    // Quota
    AddQuota(601, ActionGroup.CONFIGURE_QUOTA, false, QuotaDependency.NONE),
    UpdateQuota(602, ActionGroup.CONFIGURE_QUOTA, false, QuotaDependency.NONE),
    RemoveQuota(603, ActionGroup.CONFIGURE_QUOTA, false, QuotaDependency.NONE),
    ChangeQuotaForDisk(604, ActionGroup.CONSUME_QUOTA, false, QuotaDependency.STORAGE),

    // bookmarks
    AddBookmark(701, QuotaDependency.NONE),
    RemoveBookmark(702, QuotaDependency.NONE),
    UpdateBookmark(703, QuotaDependency.NONE),
    // vdsGroups
    AddVdsGroup(704, ActionGroup.CREATE_CLUSTER, false, QuotaDependency.NONE),
    UpdateVdsGroup(705, ActionGroup.EDIT_CLUSTER_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveVdsGroup(706, ActionGroup.DELETE_CLUSTER, false, QuotaDependency.NONE),
    AttachNetworkToVdsGroup(708, ActionGroup.ASSIGN_CLUSTER_NETWORK, false, QuotaDependency.NONE),
    DetachNetworkToVdsGroup(709, ActionGroup.ASSIGN_CLUSTER_NETWORK, false, QuotaDependency.NONE),
    UpdateNetworkOnCluster(711, ActionGroup.CONFIGURE_CLUSTER_NETWORK, false, QuotaDependency.NONE),
    AttachNetworksToCluster(712, false, QuotaDependency.NONE),
    DetachNetworksFromCluster(713, false, QuotaDependency.NONE),

    /**
     * MultiLevelAdministration
     */
    AddPermission(800, ActionGroup.MANIPULATE_PERMISSIONS, false, QuotaDependency.NONE),
    RemovePermission(801, ActionGroup.MANIPULATE_PERMISSIONS, false, QuotaDependency.NONE),
    UpdateRole(803, ActionGroup.MANIPULATE_ROLES, false, QuotaDependency.NONE),
    RemoveRole(804, ActionGroup.MANIPULATE_ROLES, false, QuotaDependency.NONE),
    AttachActionGroupsToRole(805, ActionGroup.MANIPULATE_ROLES, false, QuotaDependency.NONE),
    DetachActionGroupsFromRole(806, ActionGroup.MANIPULATE_ROLES, false, QuotaDependency.NONE),
    AddRoleWithActionGroups(809, ActionGroup.MANIPULATE_ROLES, false, QuotaDependency.NONE),
    AddSystemPermission(811, ActionGroup.MANIPULATE_PERMISSIONS, false, QuotaDependency.NONE),
    RemoveSystemPermission(812, ActionGroup.MANIPULATE_PERMISSIONS, false, QuotaDependency.NONE),

    /**
     * Storages handling
     */
    AddLocalStorageDomain(916, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AddNFSStorageDomain(902, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    UpdateStorageDomain(903, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveStorageDomain(904, ActionGroup.DELETE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ForceRemoveStorageDomain(905, ActionGroup.DELETE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AttachStorageDomainToPool(906, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    DetachStorageDomainFromPool(907, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ActivateStorageDomain(908, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ConnectDomainToStorage(912, QuotaDependency.NONE),
    DeactivateStorageDomain(909, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AddSANStorageDomain(910, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ExtendSANStorageDomain(911, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION, QuotaDependency.NONE),
    ReconstructMasterDomain(913, QuotaDependency.NONE),
    DeactivateStorageDomainWithOvfUpdate(914, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    RecoveryStoragePool(915, ActionGroup.CREATE_STORAGE_POOL, QuotaDependency.NONE),
    AddEmptyStoragePool(950, ActionGroup.CREATE_STORAGE_POOL, false, QuotaDependency.NONE),
    AddStoragePoolWithStorages(951, ActionGroup.CREATE_STORAGE_POOL, QuotaDependency.NONE),
    RemoveStoragePool(957, ActionGroup.DELETE_STORAGE_POOL, QuotaDependency.NONE),
    UpdateStoragePool(958, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, QuotaDependency.NONE),
    FenceVdsManualy(959, ActionGroup.MANIPULATE_HOST, false, QuotaDependency.NONE),
    AddExistingFileStorageDomain(960, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AddExistingBlockStorageDomain(961, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AddStorageServerConnection(1000, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    UpdateStorageServerConnection(1001, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    DisconnectStorageServerConnection(1002, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    RemoveStorageServerConnection(1003, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ConnectHostToStoragePoolServers(1004, QuotaDependency.NONE),
    DisconnectHostFromStoragePoolServers(1005, QuotaDependency.NONE),
    ConnectStorageToVds(1006, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    SetStoragePoolStatus(1007, QuotaDependency.NONE),
    ConnectAllHostsToLun(1008, QuotaDependency.NONE),
    AddPosixFsStorageDomain(1009, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    LiveMigrateDisk(1010, QuotaDependency.NONE),
    LiveMigrateVmDisks(1011, false, QuotaDependency.STORAGE),
    MoveDisks(1012, false, QuotaDependency.NONE),
    ExtendImageSize(1013, false, QuotaDependency.STORAGE),
    ImportRepoImage(1014, ActionGroup.CREATE_DISK, QuotaDependency.STORAGE),
    ExportRepoImage(1015, QuotaDependency.NONE),
    AttachStorageConnectionToStorageDomain(1016, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    DetachStorageConnectionFromStorageDomain(1017, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    SyncLunsInfoForBlockStorageDomain(1018, false, QuotaDependency.NONE),

    // Event Notification
    AddEventSubscription(1100, false, QuotaDependency.NONE),
    RemoveEventSubscription(1101, false, QuotaDependency.NONE),

    // Config
    ReloadConfigurations(1301, ActionGroup.CONFIGURE_ENGINE, false, QuotaDependency.NONE),

    // Gluster
    CreateGlusterVolume(1400, ActionGroup.CREATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    SetGlusterVolumeOption(1401, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StartGlusterVolume(1402, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StopGlusterVolume(1403, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    ResetGlusterVolumeOptions(1404, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    DeleteGlusterVolume(1405, ActionGroup.DELETE_GLUSTER_VOLUME, QuotaDependency.NONE),
    GlusterVolumeRemoveBricks(1406, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StartRebalanceGlusterVolume(1407, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    ReplaceGlusterVolumeBrick(1408, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    AddBricksToGlusterVolume(1409, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StartGlusterVolumeProfile(1410, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StopGlusterVolumeProfile(1411, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    RemoveGlusterServer(1412, ActionGroup.DELETE_HOST, QuotaDependency.NONE),
    AddGlusterFsStorageDomain(1413, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    EnableGlusterHook(1414, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    DisableGlusterHook(1415, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    UpdateGlusterHook(1416, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    AddGlusterHook(1417, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    RemoveGlusterHook(1418, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    RefreshGlusterHooks(1419, ActionGroup.MANIPULATE_GLUSTER_HOOK, QuotaDependency.NONE),
    ManageGlusterService(1420, ActionGroup.MANIPULATE_GLUSTER_SERVICE, QuotaDependency.NONE),
    StopRebalanceGlusterVolume(1421, ActionGroup.MANIPULATE_GLUSTER_VOLUME, false, QuotaDependency.NONE),
    StartRemoveGlusterVolumeBricks(1422, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StopRemoveGlusterVolumeBricks(1423, ActionGroup.MANIPULATE_GLUSTER_VOLUME, false, QuotaDependency.NONE),
    CommitRemoveGlusterVolumeBricks(1424, ActionGroup.MANIPULATE_GLUSTER_VOLUME, false, QuotaDependency.NONE),
    RefreshGlusterVolumeDetails(1425, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    RefreshGeoRepSessions(1426, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StopGeoRepSession(1427, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    DeleteGeoRepSession(1428, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    StartGlusterVolumeGeoRep(1429, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    ResumeGeoRepSession(1430, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    PauseGlusterVolumeGeoRepSession(1431, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    SetGeoRepConfig(1432, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    ResetDefaultGeoRepConfig(1433, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    DeleteGlusterVolumeSnapshot(1434, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    DeleteAllGlusterVolumeSnapshots(1435, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    ActivateGlusterVolumeSnapshot(1436, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    DeactivateGlusterVolumeSnapshot(1437, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),
    RestoreGlusterVolumeSnapshot(1438, ActionGroup.MANIPULATE_GLUSTER_VOLUME, QuotaDependency.NONE),

    // Cluster Policy
    AddClusterPolicy(1450, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    EditClusterPolicy(1451, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveClusterPolicy(1452, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveExternalPolicyUnit(1453, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    // External events
    AddExternalEvent(1500, ActionGroup.INJECT_EXTERNAL_EVENTS, QuotaDependency.NONE),

    // Providers
    AddProvider(1600, false, QuotaDependency.NONE),
    UpdateProvider(1601, false, QuotaDependency.NONE),
    RemoveProvider(1602, false, QuotaDependency.NONE),
    TestProviderConnectivity(1603, false, QuotaDependency.NONE),
    ImportProviderCertificate(1604, false, QuotaDependency.NONE),
    AddNetworkOnProvider(1605, ActionGroup.CREATE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    AddSubnetToProvider(1606, false, QuotaDependency.NONE),
    RemoveSubnetFromProvider(1607, false, QuotaDependency.NONE),

    AddWatchdog(1700, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),
    UpdateWatchdog(1701, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),
    RemoveWatchdog(1702, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),

    AddNetworkQoS(1750, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    UpdateNetworkQoS(1751, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    RemoveNetworkQoS(1752, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, false, QuotaDependency.NONE),
    // qos
    AddStorageQos(1753, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    UpdateStorageQos(1754, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveStorageQos(1755, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    AddCpuQos(1756, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    UpdateCpuQos(1757, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveCpuQos(1758, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    // disk profiles
    AddDiskProfile(1760, ActionGroup.CREATE_STORAGE_DISK_PROFILE, false, QuotaDependency.NONE),
    UpdateDiskProfile(1761, ActionGroup.CONFIGURE_STORAGE_DISK_PROFILE, false, QuotaDependency.NONE),
    RemoveDiskProfile(1762, ActionGroup.DELETE_STORAGE_DISK_PROFILE, false, QuotaDependency.NONE),
    // cpu profiles
    AddCpuProfile(1763, ActionGroup.EDIT_CLUSTER_CONFIGURATION, false, QuotaDependency.NONE),
    UpdateCpuProfile(1764, ActionGroup.EDIT_CLUSTER_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveCpuProfile(1765, ActionGroup.EDIT_CLUSTER_CONFIGURATION, false, QuotaDependency.NONE),

    // External Tasks
    AddExternalJob(1800, ActionGroup.INJECT_EXTERNAL_TASKS, false, QuotaDependency.NONE),
    EndExternalJob(1801, ActionGroup.INJECT_EXTERNAL_TASKS, false, QuotaDependency.NONE),
    ClearExternalJob(1802, ActionGroup.INJECT_EXTERNAL_TASKS, false, QuotaDependency.NONE),
    AddExternalStep(1803, ActionGroup.INJECT_EXTERNAL_TASKS, false, QuotaDependency.NONE),
    EndExternalStep(1804, ActionGroup.INJECT_EXTERNAL_TASKS, false, QuotaDependency.NONE),

    //Internal Tasks
    AddInternalJob(1850, false, QuotaDependency.NONE),
    AddInternalStep(1851, false, QuotaDependency.NONE),

    UpdateMomPolicy(1900, ActionGroup.MANIPULATE_HOST, false, QuotaDependency.NONE),
    UploadStream(1901, QuotaDependency.NONE),
    ProcessOvfUpdateForStorageDomain(1902, QuotaDependency.NONE),
    CreateOvfVolumeForStorageDomain(1903, QuotaDependency.NONE),
    CreateOvfStoresForStorageDomain(1904, QuotaDependency.NONE),
    RetrieveImageData(1905, QuotaDependency.NONE),
    ProcessOvfUpdateForStoragePool(1906, QuotaDependency.NONE),

    // Affinity Groups
    AddAffinityGroup(1950, ActionGroup.MANIPULATE_AFFINITY_GROUPS, false, QuotaDependency.NONE),
    EditAffinityGroup(1951, ActionGroup.MANIPULATE_AFFINITY_GROUPS, false, QuotaDependency.NONE),
    RemoveAffinityGroup(1952, ActionGroup.MANIPULATE_AFFINITY_GROUPS, false, QuotaDependency.NONE),

    // ISCSI Bonds
    AddIscsiBond(2000, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    EditIscsiBond(2001, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveIscsiBond(2002, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, false, QuotaDependency.NONE),

    SetHaMaintenance(2050, ActionGroup.MANIPULATE_HOST, false, QuotaDependency.NONE),

    // Rng crud
    AddRngDevice(2150, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),
    UpdateRngDevice(2151, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),
    RemoveRngDevice(2152, ActionGroup.EDIT_VM_PROPERTIES, QuotaDependency.NONE),

    // Audit Log
    RemoveAuditLogById(2100, false, QuotaDependency.NONE),
    ClearAllDismissedAuditLogs(2101, false, QuotaDependency.NONE),

    SetDataOnSession(3000, false, QuotaDependency.NONE);

    private int intValue;
    private ActionGroup actionGroup;
    private boolean isActionMonitored;
    private static final HashMap<Integer, VdcActionType> mappings = new HashMap<Integer, VdcActionType>();
    private QuotaDependency quotaDependency;
    private boolean quotaDependentAsInternalCommand = false;

    static {
        for (VdcActionType action : values()) {
            mappings.put(action.getValue(), action);
        }
    }

    private VdcActionType(int value , QuotaDependency quotaDependency) {
        this(value, null, quotaDependency);
    }

    private VdcActionType(int value, boolean isActionMonitored, QuotaDependency quotaDependency) {
        this(value, null, isActionMonitored, quotaDependency);
    }

    private VdcActionType(int value, ActionGroup actionGroupValue, QuotaDependency quotaDependency) {
        this(value, actionGroupValue, true, quotaDependency);
    }

    private VdcActionType(int value, ActionGroup actionGroupValue, boolean isActionMonitored, QuotaDependency quotaDependency) {
        this.intValue = value;
        this.actionGroup = actionGroupValue;
        this.isActionMonitored = isActionMonitored;
        this.quotaDependency = quotaDependency;
    }

    private VdcActionType(int value,
            ActionGroup actionGroupValue,
            boolean isActionMonitored,
            QuotaDependency quotaDependency,
            boolean quotaDependentAsInternalCommand) {
        this(value, actionGroupValue, isActionMonitored, quotaDependency);
        this.quotaDependentAsInternalCommand = quotaDependentAsInternalCommand;
    }


    public int getValue() {
        return intValue;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

    public boolean isActionMonitored() {
        return isActionMonitored;
    }

    public static VdcActionType forValue(int value) {
        return mappings.get(value);
    }

    public QuotaDependency getQuotaDependency() {
        return quotaDependency;
    }

    public boolean isQuotaDependentAsInternalCommand() {
        return quotaDependentAsInternalCommand;
    }

    /**
     * The QuotaDependency marks on which kind of quota regulated resources each command is dependant.
     * i.e. - Creating new Disk is dependant of Storage resources. Running a VM is dependant of VDS (cluster) resources.
     *
     * NONE - indicates no dependency of any quota regulated resources.
     *
     * !!! Notice !!! - marking your command with QuotaDependency is not enough. In order to avoid Exceptions and
     * Quota consumption errors, the command must implement the correct interface: STORAGE=>QuotaStorageDependant,
     * VDS=>QuotaVdsDependant, BOTH=>QuotaStorageDependant and QuotaVdsDependant
     */
    public enum QuotaDependency {
        NONE, STORAGE, VDS_GROUP, BOTH
    }
}
