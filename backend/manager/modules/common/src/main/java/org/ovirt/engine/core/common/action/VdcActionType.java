package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.ActionGroup;

@XmlType(name = "VdcActionType")
public enum VdcActionType {
    Unknown(0),
    // Vm Commands
    AddVm(1, ActionGroup.CREATE_VM),
    AddVmFromTemplate(2, ActionGroup.CREATE_VM),
    AddVmFromScratch(3, ActionGroup.CREATE_VM),
    RemoveVm(4, ActionGroup.DELETE_VM),
    UpdateVm(5, ActionGroup.EDIT_VM_PROPERTIES),
    StopVm(7, ActionGroup.VM_BASIC_OPERATIONS),
    ShutdownVm(8, ActionGroup.VM_BASIC_OPERATIONS),
    ChangeDisk(9, ActionGroup.CHANGE_VM_CD),
    PauseVm(10),
    HibernateVm(11, ActionGroup.VM_BASIC_OPERATIONS),
    RunVm(12, ActionGroup.VM_BASIC_OPERATIONS),
    RunVmOnce(13, ActionGroup.VM_BASIC_OPERATIONS),
    MigrateVm(14, ActionGroup.MIGRATE_VM),
    InternalMigrateVm(15),
    MigrateVmToServer(16, ActionGroup.MIGRATE_VM),
    VmLogon(18, ActionGroup.CONNECT_TO_VM),
    VmLogoff(19),
    VmLock(20),
    SetVmTicket(22, ActionGroup.CONNECT_TO_VM),
    ExportVm(23, ActionGroup.IMPORT_EXPORT_VM),
    ExportVmTemplate(24, ActionGroup.IMPORT_EXPORT_VM),
    RestoreStatelessVm(25),
    RunVmOnPowerClient(26),
    RunVmOnDedicatedVds(27),
    AddVmInterface(28, ActionGroup.CONFIGURE_VM_NETWORK),
    RemoveVmInterface(29, ActionGroup.CONFIGURE_VM_NETWORK),
    UpdateVmInterface(30, ActionGroup.CONFIGURE_VM_NETWORK),
    AddDiskToVm(31, ActionGroup.CONFIGURE_VM_STORAGE),
    RemoveDisksFromVm(32, ActionGroup.CONFIGURE_VM_STORAGE),
    MoveVm(33, ActionGroup.MOVE_VM),
    UpdateVmDisk(34, ActionGroup.CONFIGURE_VM_STORAGE),
    ChangeFloppy(35),
    ImportVm(36, ActionGroup.IMPORT_EXPORT_VM),
    RemoveVmFromImportExport(37, ActionGroup.IMPORT_EXPORT_VM),
    RemoveVmTemplateFromImportExport(38, ActionGroup.IMPORT_EXPORT_VM),
    ImportVmTemplate(39, ActionGroup.IMPORT_EXPORT_VM),
    ChangeVMCluster(40, ActionGroup.EDIT_VM_PROPERTIES),
    CancelMigrateVm(41, ActionGroup.MIGRATE_VM),
    // powerclient 4.2
    PowerClientMigrateOnConnectCheck(50),
    SetDedicatedVm(51),
    // VdsCommands
    AddVds(101, ActionGroup.CREATE_HOST),
    UpdateVds(102, ActionGroup.EDIT_HOST_CONFIGURATION),
    RemoveVds(103, ActionGroup.DELETE_HOST),
    RestartVds(104, ActionGroup.MANIPUTLATE_HOST),
    VdsNotRespondingTreatment(105),
    MaintananceVds(106),
    MaintananceNumberOfVdss(107, ActionGroup.MANIPUTLATE_HOST),
    ActivateVds(108, ActionGroup.MANIPUTLATE_HOST),
    InstallVds(109),
    ClearNonResponsiveVdsVms(110),
    ShutdownVds(111),
    ApproveVds(112, ActionGroup.CREATE_HOST),
    HandleVdsCpuFlagsOrClusterChanged(114),
    InitVdsOnUp(115),
    SetNonOperationalVds(117),
    AddVdsSpmId(119),
    RemoveVdsSpmId(120),
    // Fencing (including RestartVds above)
    StartVds(121, ActionGroup.MANIPUTLATE_HOST),
    StopVds(122, ActionGroup.MANIPUTLATE_HOST),
    HandleVdsVersion(124),
    ChangeVDSCluster(125, ActionGroup.EDIT_HOST_CONFIGURATION),
    // Network
    UpdateNetworkToVdsInterface(149, ActionGroup.CONFIGURE_HOST_NETWORK),
    AttachNetworkToVdsInterface(150, ActionGroup.CONFIGURE_HOST_NETWORK),
    DetachNetworkFromVdsInterface(151, ActionGroup.CONFIGURE_HOST_NETWORK),
    AddBond(152, ActionGroup.CONFIGURE_HOST_NETWORK),
    RemoveBond(153, ActionGroup.CONFIGURE_HOST_NETWORK),
    AddNetwork(154, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK),
    RemoveNetwork(155, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK),
    UpdateNetwork(156, ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK),
    CommitNetworkChanges(157, ActionGroup.CONFIGURE_HOST_NETWORK),
    SetupNetworks(158, ActionGroup.CONFIGURE_HOST_NETWORK),
    // VmTemplatesCommand
    AddVmTemplate(201, ActionGroup.CREATE_TEMPLATE),
    UpdateVmTemplate(202, ActionGroup.EDIT_TEMPLATE_PROPERTIES),
    RemoveVmTemplate(203, ActionGroup.DELETE_TEMPLATE),
    MoveOrCopyTemplate(226, ActionGroup.COPY_TEMPLATE),
    AddVmTemplateInterface(220, ActionGroup.CONFIGURE_TEMPLATE_NETWORK),
    RemoveVmTemplateInterface(221, ActionGroup.CONFIGURE_TEMPLATE_NETWORK),
    UpdateVmTemplateInterface(222, ActionGroup.CONFIGURE_TEMPLATE_NETWORK),
    // ImagesCommands
    TryBackToSnapshot(204),
    RestoreFromSnapshot(205),
    CreateAllSnapshotsFromVm(206, ActionGroup.MANIPULATE_VM_SNAPSHOTS),
    CreateSnapshot(207),
    CreateSnapshotFromTemplate(208),
    CreateImageTemplate(209),
    MergeSnapshot(210, ActionGroup.MANIPULATE_VM_SNAPSHOTS),
    RemoveImage(211),
    RemoveAllVmImages(212),
    AddImageFromScratch(213),
    AddImageFromImport(214),
    RemoveTemplateSnapshot(215),
    RemoveAllVmTemplateImageTemplates(216),
    AddImagesFromImport(217),
    AddTemplateImagesFromImport(218),
    AddVmTemplateFromImport(219),
    TryBackToAllSnapshotsOfVm(223, ActionGroup.MANIPULATE_VM_SNAPSHOTS),
    RestoreAllSnapshots(224, ActionGroup.MANIPULATE_VM_SNAPSHOTS),
    MoveOrCopyImageGroup(225),
    MergeSnapshotSingleDisk(227),
    MoveMultipleImageGroups(228),
    CreateCloneOfTemplate(229),
    // VmPoolCommands
    AddVmPool(301),
    UpdateUserVm(303),
    AddVmPoolWithVms(304, ActionGroup.CREATE_VM_POOL),
    UpdateVmPoolWithVms(305, ActionGroup.EDIT_VM_POOL_CONFIGURATION),
    AddVmAndAttachToPool(306),
    RemoveVmPool(307, ActionGroup.DELETE_VM_POOL),
    AttachUserToVmFromPool(310),
    DetachUserFromVmFromPool(312),
    AddVmToPool(313),
    RemoveVmFromPool(314, ActionGroup.EDIT_VM_POOL_CONFIGURATION),
    // TODO: old implementation of TimeLeasedPools
    // AttachUserToTimeLeasedPool(315),
    // DetachUserFromTimeLeasedPool(316),
    // UpdateUserToTimeLeasedPool(317),
    AttachUserToVmFromPoolAndRun(318, ActionGroup.VM_POOL_BASIC_OPERATIONS),
    // UserAndGroupsCommands
    SetUserRole(405),
    LoginUser(406),
    AutoLogin(407),
    LogoutUser(408),
    RemoveUser(409, ActionGroup.MANIPULATE_USERS),
    SetAdGroupRole(410),
    // TODO: old implementation of TimeLeasedPools
    // AttachAdGroupTimeLeasedPool(412),
    // UpdateAdGroupTimeLeasedPool(413),
    // DetachAdGroupFromTimeLeasedPool(414),
    RemoveAdGroup(415, ActionGroup.MANIPULATE_USERS),
    ChangeUserPassword(416),
    CreateComputerAccount(417),
    LoginAdminUser(418),
    AddUser(419, ActionGroup.MANIPULATE_USERS),
    // Tags
    AddTag(501),
    RemoveTag(502),
    UpdateTag(503),
    MoveTag(504),
    AttachUserToTag(505),
    DetachUserFromTag(506),
    AttachUserGroupToTag(507),
    DetachUserGroupFromTag(508),
    AttachVmsToTag(509),
    DetachVmFromTag(510),
    AttachVdsToTag(511),
    DetachVdsFromTag(512),
    UpdateTagsVmMapDefaultDisplayType(515),
    // bookmarks
    AddBookmark(701),
    RemoveBookmark(702),
    UpdateBookmark(703),
    // vdsGroups
    AddVdsGroup(704, ActionGroup.CREATE_CLUSTER),
    UpdateVdsGroup(705, ActionGroup.EDIT_CLUSTER_CONFIGURATION),
    RemoveVdsGroup(706, ActionGroup.DELETE_CLUSTER),
    AttachNetworkToVdsGroup(708, ActionGroup.CONFIGURE_CLUSTER_NETWORK),
    DetachNetworkToVdsGroup(709, ActionGroup.CONFIGURE_CLUSTER_NETWORK),
    UpdateDisplayToVdsGroup(710, ActionGroup.EDIT_CLUSTER_CONFIGURATION),
    /**
     * 4.0
     */
    MigrateIrsSnapshotsToVdc(707),
    /**
     * MultiLevelAdministration
     */
    AddPermission(800, ActionGroup.MANIPULATE_PERMISSIONS),
    RemovePermission(801, ActionGroup.MANIPULATE_PERMISSIONS),
    UpdateRole(803, ActionGroup.MANIPULATE_ROLES),
    RemoveRole(804, ActionGroup.MANIPULATE_ROLES),
    AttachActionGroupsToRole(805, ActionGroup.MANIPULATE_ROLES),
    DetachActionGroupsFromRole(806, ActionGroup.MANIPULATE_ROLES),
    AddRoleWithActionGroups(809, ActionGroup.MANIPULATE_ROLES),
    AddSelfPermission(810),
    AddSystemPermission(811, ActionGroup.MANIPULATE_PERMISSIONS),

    /**
     * Storages handling
     */
    AddLocalStorageDomain(916, ActionGroup.CREATE_STORAGE_DOMAIN),
    AddNFSStorageDomain(902, ActionGroup.CREATE_STORAGE_DOMAIN),
    UpdateStorageDomain(903, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION),
    RemoveStorageDomain(904, ActionGroup.DELETE_STORAGE_DOMAIN),
    ForceRemoveStorageDomain(905, ActionGroup.DELETE_STORAGE_DOMAIN),
    AttachStorageDomainToPool(906, ActionGroup.MANIPULATE_STORAGE_DOMAIN),
    DetachStorageDomainFromPool(907, ActionGroup.MANIPULATE_STORAGE_DOMAIN),
    ActivateStorageDomain(908, ActionGroup.MANIPULATE_STORAGE_DOMAIN),
    DeactivateStorageDomain(909, ActionGroup.MANIPULATE_STORAGE_DOMAIN),
    AddSANStorageDomain(910, ActionGroup.CREATE_STORAGE_DOMAIN),
    ExtendSANStorageDomain(911, ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION),
    ReconstructMasterDomain(913),
    HandleFailedStorageDomain(914),
    RecoveryStoragePool(915, ActionGroup.CREATE_STORAGE_POOL),
    AddEmptyStoragePool(950, ActionGroup.CREATE_STORAGE_POOL),
    AddStoragePoolWithStorages(951, ActionGroup.CREATE_STORAGE_POOL),
    RemoveStoragePool(957, ActionGroup.DELETE_STORAGE_POOL),
    UpdateStoragePool(958, ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION),
    FenceVdsManualy(959, ActionGroup.MANIPUTLATE_HOST),
    AddExistingNFSStorageDomain(960, ActionGroup.CREATE_STORAGE_DOMAIN),
    AddExistingSANStorageDomain(961, ActionGroup.CREATE_STORAGE_DOMAIN),
    AddStorageServerConnection(1000, ActionGroup.CREATE_STORAGE_DOMAIN),
    RemoveStorageServerConnection(1002, ActionGroup.CREATE_STORAGE_DOMAIN),
    ConnectHostToStoragePoolServers(1004),
    DisconnectHostFromStoragePoolServers(1005),
    ConnectStorageToVds(1006, ActionGroup.CREATE_STORAGE_DOMAIN),
    SetStoragePoolStatus(1007),
    ConnectAllHostsToLun(1008),
    // Event Notification
    AddEventSubscription(1100),
    RemoveEventSubscription(1101),

    // UI User Tabs
    DataCenters(1200),
    Clusters(1201),
    Hosts(1202),
    Storage(1203),
    VirtualMachines(1204),
    Pools(1205),
    Templates(1206),
    Users(1207),
    Events(1208),
    Monitor(1209),

    // Config
    SetConfigurationValue(1300, ActionGroup.CONFIGURE_ENGINE);

    private int intValue;
    private ActionGroup actionGroup;
    private static java.util.HashMap<Integer, VdcActionType> mappings = new HashMap<Integer, VdcActionType>();

    static {
        for (VdcActionType action : values()) {
            mappings.put(action.getValue(), action);
        }
    }

    private VdcActionType(int value) {
        this(value, null);
    }

    private VdcActionType(int value, ActionGroup actionGroupValue) {
        intValue = value;
        actionGroup = actionGroupValue;
    }

    public int getValue() {
        return intValue;
    }

    public ActionGroup getActionGroup() {
        return actionGroup;
    }

    public static VdcActionType forValue(int value) {
        return mappings.get(value);
    }

}
