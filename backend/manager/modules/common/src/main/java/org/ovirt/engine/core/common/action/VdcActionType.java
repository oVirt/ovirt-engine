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
    ChangeFloppy(35, QuotaDependency.NONE),
    ImportVm(36, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    RemoveVmFromImportExport(37, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.NONE),
    RemoveVmTemplateFromImportExport(38, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.NONE),
    ImportVmTemplate(39, ActionGroup.IMPORT_EXPORT_VM, QuotaDependency.STORAGE),
    ChangeVMCluster(40, ActionGroup.EDIT_VM_PROPERTIES, false, QuotaDependency.NONE),
    CancelMigrateVm(41, ActionGroup.MIGRATE_VM, false, QuotaDependency.NONE),
    ActivateDeactivateVmNic(42, QuotaDependency.NONE),
    AddVmFromSnapshot(52, ActionGroup.CREATE_VM, QuotaDependency.BOTH),
    // VdsCommands
    AddVds(101, ActionGroup.CREATE_HOST, QuotaDependency.NONE),
    UpdateVds(102, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
    RemoveVds(103, ActionGroup.DELETE_HOST, false, QuotaDependency.NONE),
    RestartVds(104, ActionGroup.MANIPUTLATE_HOST, QuotaDependency.NONE),
    VdsNotRespondingTreatment(105, QuotaDependency.NONE),
    MaintenanceVds(106, QuotaDependency.NONE),
    MaintenanceNumberOfVdss(107, ActionGroup.MANIPUTLATE_HOST, false, QuotaDependency.NONE),
    ActivateVds(108, ActionGroup.MANIPUTLATE_HOST, QuotaDependency.NONE),
    InstallVds(109, QuotaDependency.NONE),
    ClearNonResponsiveVdsVms(110, QuotaDependency.NONE),
    ShutdownVds(111, QuotaDependency.NONE),
    ApproveVds(112, ActionGroup.CREATE_HOST, QuotaDependency.NONE),
    HandleVdsCpuFlagsOrClusterChanged(114, QuotaDependency.NONE),
    InitVdsOnUp(115, QuotaDependency.NONE),
    SetNonOperationalVds(117, QuotaDependency.NONE),
    AddVdsSpmId(119, QuotaDependency.NONE),
    // Fencing (including RestartVds above)
    StartVds(121, ActionGroup.MANIPUTLATE_HOST, QuotaDependency.NONE),
    StopVds(122, ActionGroup.MANIPUTLATE_HOST, QuotaDependency.NONE),
    HandleVdsVersion(124, QuotaDependency.NONE),
    ChangeVDSCluster(125, ActionGroup.EDIT_HOST_CONFIGURATION, false, QuotaDependency.NONE),
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
    MoveOrCopyImageGroup(225, QuotaDependency.STORAGE),
    MoveOrCopyDisk(226, QuotaDependency.STORAGE),
    RemoveSnapshotSingleDisk(227, QuotaDependency.STORAGE),
    CreateCloneOfTemplate(229, QuotaDependency.STORAGE),
    RemoveDisk(230, QuotaDependency.STORAGE),
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
    AttachUserToVmFromPoolAndRun(318, ActionGroup.VM_POOL_BASIC_OPERATIONS, QuotaDependency.NONE),
    // UserAndGroupsCommands
    LoginUser(406, ActionGroup.LOGIN, false, QuotaDependency.NONE),
    LogoutUser(408, false, QuotaDependency.NONE),
    RemoveUser(409, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    RemoveAdGroup(415, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
    ChangeUserPassword(416, QuotaDependency.NONE),
    LoginAdminUser(418, ActionGroup.LOGIN, false, QuotaDependency.NONE),
    AddUser(419, ActionGroup.MANIPULATE_USERS, false, QuotaDependency.NONE),
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
    @Deprecated
    // AttachNetworkToVdsGroup is taking over this functionality
    UpdateDisplayToVdsGroup(710, ActionGroup.EDIT_CLUSTER_CONFIGURATION, false, QuotaDependency.NONE),
    UpdateNetworkOnCluster(711, ActionGroup.CONFIGURE_CLUSTER_NETWORK, false, QuotaDependency.NONE),

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
    ConnectDomainToStorage(916, QuotaDependency.NONE),
    DeactivateStorageDomain(909, ActionGroup.MANIPULATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    AddSANStorageDomain(910, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
    ExtendSANStorageDomain(911, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION, QuotaDependency.NONE),
    ReconstructMasterDomain(913, QuotaDependency.NONE),
    RecoveryStoragePool(915, ActionGroup.CREATE_STORAGE_POOL, QuotaDependency.NONE),
    AddEmptyStoragePool(950, ActionGroup.CREATE_STORAGE_POOL, false, QuotaDependency.NONE),
    AddStoragePoolWithStorages(951, ActionGroup.CREATE_STORAGE_POOL, QuotaDependency.NONE),
    RemoveStoragePool(957, ActionGroup.DELETE_STORAGE_POOL, QuotaDependency.NONE),
    UpdateStoragePool(958, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, QuotaDependency.NONE),
    FenceVdsManualy(959, ActionGroup.MANIPUTLATE_HOST, false, QuotaDependency.NONE),
    AddExistingNFSStorageDomain(960, ActionGroup.CREATE_STORAGE_DOMAIN, QuotaDependency.NONE),
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
    // Event Notification
    AddEventSubscription(1100, QuotaDependency.NONE),
    RemoveEventSubscription(1101, QuotaDependency.NONE),

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

    // External events
    AddExternalEvent(1500, ActionGroup.INJECT_EXTERNAL_EVENTS, QuotaDependency.NONE),
    RemoveExternalEvent(1501, ActionGroup.INJECT_EXTERNAL_EVENTS, QuotaDependency.NONE);

    private int intValue;
    private ActionGroup actionGroup;
    private boolean isActionMonitored = true;
    private static java.util.HashMap<Integer, VdcActionType> mappings = new HashMap<Integer, VdcActionType>();
    private QuotaDependency quotaDependency;

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
