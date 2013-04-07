package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface LocalizedEnums extends ConstantsWithLookup {

	String NonOperationalReason___NONE();

	String NonOperationalReason___GENERAL();

	String NonOperationalReason___CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER();

	String NonOperationalReason___STORAGE_DOMAIN_UNREACHABLE();

	String NonOperationalReason___NETWORK_UNREACHABLE();

	String NonOperationalReason___VERSION_INCOMPATIBLE_WITH_CLUSTER();

	String NonOperationalReason___KVM_NOT_RUNNING();

	String NonOperationalReason___TIMEOUT_RECOVERING_FROM_CRASH();

    String NonOperationalReason___GLUSTER_COMMAND_FAILED();

	String VmPauseStatus___NONE();

	String VmPauseStatus___NOERR();

	String VmPauseStatus___EOTHER();

	String VmPauseStatus___EIO();

	String VmPauseStatus___ENOSPC();

	String VmPauseStatus___EPERM();

	String ActionGroup___CREATE_STORAGE_POOL();

	String ActionGroup___DELETE_STORAGE_POOL();

	String ActionGroup___EDIT_STORAGE_POOL_CONFIGURATION();

	String ActionGroup___CREATE_STORAGE_DOMAIN();

	String ActionGroup___DELETE_STORAGE_DOMAIN();

	String ActionGroup___EDIT_STORAGE_DOMAIN_CONFIGURATION();

	String ActionGroup___MANIPULATE_STORAGE_DOMAIN();

	String ActionGroup___CREATE_TEMPLATE();

	String ActionGroup___DELETE_TEMPLATE();

	String ActionGroup___EDIT_TEMPLATE_PROPERTIES();

    String ActionGroup___EDIT_ADMIN_TEMPLATE_PROPERTIES();

	String ActionGroup___CONFIGURE_TEMPLATE_NETWORK();

	String ActionGroup___COPY_TEMPLATE();

	String ActionGroup___CREATE_CLUSTER();

	String ActionGroup___DELETE_CLUSTER();

	String ActionGroup___EDIT_CLUSTER_CONFIGURATION();

	String ActionGroup___CONFIGURE_CLUSTER_NETWORK();

    String ActionGroup___ASSIGN_CLUSTER_NETWORK();

	String ActionGroup___CREATE_HOST();

	String ActionGroup___DELETE_HOST();

	String ActionGroup___EDIT_HOST_CONFIGURATION();

	String ActionGroup___MANIPUTLATE_HOST();

	String ActionGroup___CONFIGURE_HOST_NETWORK();

	String ActionGroup___CREATE_VM();

	String ActionGroup___DELETE_VM();

	String ActionGroup___MOVE_VM();

	String ActionGroup___EDIT_VM_PROPERTIES();

	String ActionGroup___MIGRATE_VM();

	String ActionGroup___CHANGE_VM_CUSTOM_PROPERTIES();

	String ActionGroup___EDIT_ADMIN_VM_PROPERTIES();

	String ActionGroup___IMPORT_EXPORT_VM();

	String ActionGroup___CONFIGURE_VM_NETWORK();

	String ActionGroup___CONFIGURE_VM_STORAGE();

	String ActionGroup___VM_BASIC_OPERATIONS();

	String ActionGroup___CHANGE_VM_CD();

	String ActionGroup___CONNECT_TO_VM();

	String ActionGroup___RECONNECT_TO_VM();

	String ActionGroup___MANIPULATE_VM_SNAPSHOTS();

	String ActionGroup___CREATE_VM_POOL();

	String ActionGroup___DELETE_VM_POOL();

	String ActionGroup___EDIT_VM_POOL_CONFIGURATION();

	String ActionGroup___VM_POOL_BASIC_OPERATIONS();

	String ActionGroup___MANIPULATE_USERS();

	String ActionGroup___MANIPULATE_PERMISSIONS();

	String ActionGroup___LOGIN();

	String ActionGroup___MANIPULATE_ROLES();

    String ActionGroup___CONFIGURE_STORAGE_POOL_NETWORK();

    String ActionGroup___CREATE_STORAGE_POOL_NETWORK();

    String ActionGroup___DELETE_STORAGE_POOL_NETWORK();

	String ActionGroup___CONFIGURE_ENGINE();

	String ActionGroup___CREATE_DISK();

	String ActionGroup___ATTACH_DISK();

	String ActionGroup___EDIT_DISK_PROPERTIES();

	String ActionGroup___PORT_MIRRORING();

	String ActionGroup___CONFIGURE_DISK_STORAGE();

	String ActionGroup___DELETE_DISK();

	// Gluster action groups
	String ActionGroup___CREATE_GLUSTER_VOLUME();

	String ActionGroup___MANIPULATE_GLUSTER_VOLUME();

	String ActionGroup___DELETE_GLUSTER_VOLUME();

	String EventNotificationEntity___Host();

	String EventNotificationEntity___Vm();

	String EventNotificationEntity___Storage();

	String EventNotificationEntity___Engine();

	String EventNotificationEntity___GlusterVolume();

	String EventNotificationEntity___DWH();

	String AuditLogType___VDS_FAILURE();

	String AuditLogType___USER_VDS_MAINTENANCE();

	String AuditLogType___USER_VDS_MAINTENANCE_MIGRATION_FAILED();

	String AuditLogType___VDS_MAINTENANCE();

	String AuditLogType___VDS_MAINTENANCE_FAILED();

	String AuditLogType___VDS_ACTIVATE_FAILED();

	String AuditLogType___VDS_RECOVER_FAILED();

	String AuditLogType___VDS_SLOW_STORAGE_RESPONSE_TIME();

	String AuditLogType___VDS_APPROVE_FAILED();

	String AuditLogType___VDS_INSTALL_FAILED();

	String AuditLogType___VM_FAILURE();

	String AuditLogType___VM_MIGRATION_START();

	String AuditLogType___VM_MIGRATION_FAILED();

	String AuditLogType___VM_NOT_RESPONDING();

	String AuditLogType___IRS_FAILURE();

	String AuditLogType___IRS_DISK_SPACE_LOW();

	String AuditLogType___IRS_DISK_SPACE_LOW_ERROR();

	String AuditLogType___VDC_STOP();

	// Gluster Audit log types
	String AuditLogType___GLUSTER_VOLUME_CREATE();

	String AuditLogType___GLUSTER_VOLUME_CREATE_FAILED();

	String AuditLogType___GLUSTER_VOLUME_OPTION_SET();

	String AuditLogType___GLUSTER_VOLUME_OPTION_SET_FAILED();

	String AuditLogType___GLUSTER_VOLUME_START();

	String AuditLogType___GLUSTER_VOLUME_START_FAILED();

	String AuditLogType___GLUSTER_VOLUME_STOP();

	String AuditLogType___GLUSTER_VOLUME_STOP_FAILED();

	String AuditLogType___GLUSTER_VOLUME_OPTIONS_RESET();

	String AuditLogType___GLUSTER_VOLUME_OPTIONS_RESET_FAILED();

	String AuditLogType___GLUSTER_VOLUME_DELETE();

	String AuditLogType___GLUSTER_VOLUME_DELETE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_FAILED();

    String AuditLogType___GLUSTER_VOLUME_ADD_BRICK();

    String AuditLogType___GLUSTER_VOLUME_ADD_BRICK_FAILED();

	String AuditLogType___GLUSTER_VOLUME_REBALANCE_START();

	String AuditLogType___GLUSTER_VOLUME_REBALANCE_START_FAILED();

	String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_FAILED();

	String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_START();

	String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED();

	String AuditLogType___GLUSTER_SERVER_ADD_FAILED();

	String AuditLogType___GLUSTER_SERVER_REMOVE();

	String AuditLogType___GLUSTER_SERVER_REMOVE_FAILED();

	String AuditLogType___GLUSTER_VOLUME_PROFILE_START();

	String AuditLogType___GLUSTER_VOLUME_PROFILE_START_FAILED();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_STOP();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_STOP_FAILED();

    String VdcActionType___ActivateVds();

	String VdcActionType___RecoveryStoragePool();

	String VdcActionType___UpdateVmInterface();

	String VdcActionType___MigrateVmToServer();

	String VdcActionType___UpdateDisplayToVdsGroup();

	String VdcActionType___ChangeDisk();

    String VdcActionType___MoveOrCopyDisk();

    String VdcActionType___LiveMigrateDisk();

    String VdcActionType___MoveDisk();

	String VdcActionType___ShutdownVm();

	String VdcActionType___ApproveVds();

	String VdcActionType___AddBond();

	String VdcActionType___AddNetwork();

	String VdcActionType___AddPermission();

	String VdcActionType___AddRole();

	String VdcActionType___RemoveRole();

	String VdcActionType___AddRoleWithActions();

	String VdcActionType___AddVds();

	String VdcActionType___AddVdsGroup();

	String VdcActionType___AddVm();

	String VdcActionType___AddVmFromScratch();

	String VdcActionType___AddVmInterface();

	String VdcActionType___AddVmPoolWithVms();

	String VdcActionType___AddVmTemplate();

	String VdcActionType___AddVmTemplateInterface();

	String VdcActionType___AddVmToPool();

	String VdcActionType___AttachActionToRole();

	String VdcActionType___AttachAdGroupTimeLeasedPool();

	String VdcActionType___AttachNetworkToVdsGroup();

	String VdcActionType___AttachNetworkToVdsInterface();

	String VdcActionType___AttachUserToTimeLeasedPool();

	String VdcActionType___AttachVmPoolToAdGroup();

	String VdcActionType___AttachVmPoolToUser();

	String VdcActionType___AttachVmToAdGroup();

	String VdcActionType___AttachVmToUser();

	String VdcActionType___CommitNetworkChanges();

	String VdcActionType___SetupNetworks();

	String VdcActionType___CreateAllSnapshotsFromVm();

	String VdcActionType___DetachActionFromRole();

	String VdcActionType___DetachAdGroupFromTimeLeasedPool();

	String VdcActionType___DetachNetworkFromVdsInterface();

	String VdcActionType___DetachNetworkToVdsGroup();

	String VdcActionType___DetachUserFromTimeLeasedPool();

	String VdcActionType___DetachUserFromVmFromPool();

	String VdcActionType___DetachVmFromAdGroup();

	String VdcActionType___DetachVmFromUser();

	String VdcActionType___DetachVmPoolFromAdGroup();

	String VdcActionType___DetachVmPoolFromUser();

	String VdcActionType___HibernateVm();

	String VdcActionType___ActivateDeactivateVmNic();

	String VdcActionType___LoginAdminUser();

    String VdcActionType___MaintenanceNumberOfVdss();

	String VdcActionType___MergeSnapshot();

	String VdcActionType___MigrateVm();

	String VdcActionType___PauseVm();

	String VdcActionType___RemoveAdGroup();

	String VdcActionType___RemoveUser();

	String VdcActionType___RemoveBond();

	String VdcActionType___RemoveNetwork();

	String VdcActionType___RemovePermission();

	String VdcActionType___RemoveVds();

	String VdcActionType___RemoveVdsGroup();

	String VdcActionType___RemoveVm();

	String VdcActionType___RemoveVmFromPool();

	String VdcActionType___RemoveVmInterface();

	String VdcActionType___RemoveVmPool();

	String VdcActionType___RemoveVmTemplate();

	String VdcActionType___RemoveVmTemplateInterface();

	String VdcActionType___RestartVds();

	String VdcActionType___RestoreAllSnapshots();

	String VdcActionType___RunVm();

	String VdcActionType___StartVds();

	String VdcActionType___StopVds();

	String VdcActionType___RunVmOnce();

	String VdcActionType___StopVm();

	String VdcActionType___TryBackToAllSnapshotsOfVm();

	String VdcActionType___UpdateAdGroupTimeLeasedPool();

	String VdcActionType___UpdateNetworkToVdsInterface();

	String VdcActionType___UpdateNetwork();

	String VdcActionType___UpdateRole();

	String VdcActionType___UpdateUserToTimeLeasedPool();

	String VdcActionType___UpdateVds();

	String VdcActionType___UpdateVdsGroup();

	String VdcActionType___UpdateVm();

	String VdcActionType___UpdateVmConsoleData();

	String VdcActionType___UpdateVmPoolWithVms();

	String VdcActionType___UpdateVmTemplate();

	String VdcActionType___MoveOrCopyTemplate();

	String VdcActionType___UpdateVmTemplateInterface();

	String VdcActionType___AttachVmsToTag();

	String VdcActionType___DetachVmFromTag();

	String VdcActionType___AddDisk();

	String VdcActionType___RemoveDisk();

	String VdcActionType___UpdateVmDisk();

	String VdcActionType___AttachDiskToVm();

	String VdcActionType___DetachDiskFromVm();

	String VdcActionType___MoveVm();

	String VdcActionType___AddVmAndAttachToUser();

	String VdcActionType___AttachUserToVmFromPool();

	String VdcActionType___AttachUserToVmFromPoolAndRun();

	String VdcActionType___AddStorageServerConnection();

	String VdcActionType___DataCenters();

	String VdcActionType___Clusters();

	String VdcActionType___Hosts();

	String VdcActionType___Storage();

	String VdcActionType___VirtualMachines();

	String VdcActionType___Pools();

	String VdcActionType___Templates();

	String VdcActionType___Users();

	String VdcActionType___Events();

	String VdcActionType___Monitor();

	String VdcActionType___ForceRemoveStorageDomain();

	String VdcActionType___ActivateStorageDomain();

	String VdcActionType___FenceVdsManualy();

	String VdcActionType___AddEmptyStoragePool();

	String VdcActionType___AddNFSStorageDomain();

	String VdcActionType___AddSANStorageDomain();

	String VdcActionType___AddLocalStorageDomain();

	String VdcActionType___AddStoragePoolWithStorages();

	String VdcActionType___AttachStorageDomainToPool();

	String VdcActionType___DeactivateStorageDomain();

	String VdcActionType___DetachStorageDomainFromPool();

	String VdcActionType___ExtendSANStorageDomain();

	String VdcActionType___RemoveStorageDomain();

	String VdcActionType___RemoveStoragePool();

	String VdcActionType___UpdateStorageDomain();

	String VdcActionType___UpdateStoragePool();

	String VdcActionType___AddEventSubscription();

	String VdcActionType___RemoveEventSubscription();

	String VdcActionType___ImportVm();

	String VdcActionType___ExportVm();

	String VdcActionType___RemoveVmFromImportExport();

	String VdcActionType___ImportVmTemplate();

	String VdcActionType___ExportVmTemplate();

	String VdcActionType___RemoveVmTemplateFromImportExport();

	String VdcActionType___AddQuota();

	String VdcActionType___UpdateQuota();

	String VdcActionType___RemoveQuota();

	// Gluster Action Types
	String VdcActionType___CreateGlusterVolume();

	String VdcActionType___SetGlusterVolumeOption();

	String VdcActionType___StartGlusterVolume();

	String VdcActionType___StopGlusterVolume();

	String VdcActionType___ResetGlusterVolumeOptions();

	String VdcActionType___DeleteGlusterVolume();

	String VdcActionType___GlusterVolumeRemoveBricks();

	String VdcActionType___AddBricksToGlusterVolume();

	String VdcActionType___StartRebalanceGlusterVolume();

	String VdcActionType___ReplaceGlusterVolumeBrick();

	String VdcActionType___GlusterHostAdd();

	String vdcActionType___StartGlusterVolumeProfile();

	String vdcActionType___StopGlusterVolumeProfile();

	String VdcActionType___ConnectStorageToVds();

	String VdcObjectType___AdElements();

	String VdcObjectType___System();

	String VdcObjectType___StoragePool();

	String VdcObjectType___Bookmarks();

	String VdcObjectType___Storage();

	String VdcObjectType___MultiLevelAdministration();

	String VdcObjectType___UITabs();

	String VdcObjectType___Tags();

	String VdcObjectType___Unknown();

	String VdcObjectType___VDS();

	String VdcObjectType___VdsGroups();

	String VdcObjectType___VM();

	String VdcObjectType___VmPool();

	String VdcObjectType___VmTemplate();

	String VdcObjectType___EventNotification();

	String VdcObjectType___ImportExport();

	String VdsSelectionAlgorithm___None();

	String VdsSelectionAlgorithm___EvenlyDistribute();

	String VdsSelectionAlgorithm___PowerSave();

	String AuditLogType___DWH_STOPPED();

	String AuditLogType___DWH_STARTED();

	String AuditLogType___DWH_ERROR();

    String AuditLogType___VDS_TIME_DRIFT_ALERT();

    String AuditLogType___HA_VM_RESTART_FAILED();

    String AuditLogType___HA_VM_FAILED();

    String AuditLogType___SYSTEM_DEACTIVATED_STORAGE_DOMAIN();

    String AuditLogType___VDS_SET_NONOPERATIONAL();

    String AuditLogType___VDS_SET_NONOPERATIONAL_IFACE_DOWN();

    String AuditLogType___VDS_SET_NONOPERATIONAL_DOMAIN();

    String AuditLogType___SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM();

    String AuditLogType___VDS_HIGH_MEM_USE();

    String AuditLogType___VDS_HIGH_NETWORK_USE();

    String AuditLogType___VDS_HIGH_CPU_USE();

    String AuditLogType___VDS_HIGH_SWAP_USE();

    String AuditLogType___VDS_LOW_SWAP();

    String MigrationSupport___MIGRATABLE();

    String MigrationSupport___IMPLICITLY_NON_MIGRATABLE();

    String MigrationSupport___PINNED_TO_HOST();
}
