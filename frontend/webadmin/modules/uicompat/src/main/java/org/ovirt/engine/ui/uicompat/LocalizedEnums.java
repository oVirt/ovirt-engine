package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface LocalizedEnums extends ConstantsWithLookup {
    String NonOperationalReason___NONE();

    String NonOperationalReason___GENERAL();

    String NonOperationalReason___CPU_TYPE_UNSUPPORTED_IN_THIS_CLUSTER_VERSION();

    String NonOperationalReason___CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___STORAGE_DOMAIN_UNREACHABLE();

    String NonOperationalReason___NETWORK_UNREACHABLE();

    String NonOperationalReason___VM_NETWORK_IS_BRIDGELESS();

    String NonOperationalReason___VERSION_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___KVM_NOT_RUNNING();

    String NonOperationalReason___TIMEOUT_RECOVERING_FROM_CRASH();

    String NonOperationalReason___GLUSTER_COMMAND_FAILED();

    String NonOperationalReason___UNTRUSTED();

    String NonOperationalReason___UNINITIALIZED();

    String NonOperationalReason___GLUSTER_HOST_UUID_NOT_FOUND();

    String NonOperationalReason___GLUSTER_HOST_UUID_ALREADY_EXISTS();

    String NonOperationalReason___CLUSTER_VERSION_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER_LEVEL();

    String NonOperationalReason___MIXING_RHEL_VERSIONS_IN_CLUSTER();

    String NonOperationalReason___ARCHITECTURE_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___NETWORK_INTERFACE_IS_DOWN();

    String NonOperationalReason___RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___HOST_FEATURES_INCOMPATIBILE_WITH_CLUSTER();

    String NonOperationalReason___VDS_CANNOT_CONNECT_TO_GLUSTERFS();

    String NonOperationalReason___KUBEVIRT_NOT_SCHEDULABLE();

    String UsbPolicy___ENABLED_NATIVE();

    String UsbPolicy___DISABLED();

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

    String ActionGroup___MANIPULATE_HOST();

    String ActionGroup___CONFIGURE_HOST_NETWORK();

    String ActionGroup___CREATE_VM();

    String ActionGroup___CREATE_INSTANCE();

    String ActionGroup___DELETE_VM();

    String ActionGroup___EDIT_VM_PROPERTIES();

    String ActionGroup___MIGRATE_VM();

    String ActionGroup___CHANGE_VM_CUSTOM_PROPERTIES();

    String ActionGroup___EDIT_ADMIN_VM_PROPERTIES();

    String ActionGroup___CONNECT_TO_SERIAL_CONSOLE();

    String ActionGroup___IMPORT_EXPORT_VM();

    String ActionGroup___CONFIGURE_VM_NETWORK();

    String ActionGroup___CONFIGURE_VM_STORAGE();

    String ActionGroup___REBOOT_VM();

    String ActionGroup___RESET_VM();

    String ActionGroup___STOP_VM();

    String ActionGroup___SHUT_DOWN_VM();

    String ActionGroup___HIBERNATE_VM();

    String ActionGroup___RUN_VM();

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

    String ActionGroup___ADD_USERS_AND_GROUPS_FROM_DIRECTORY();

    String ActionGroup___LOGIN();

    String ActionGroup___TAG_MANAGEMENT();

    String ActionGroup___AUDIT_LOG_MANAGEMENT();

    String ActionGroup___BOOKMARK_MANAGEMENT();

    String ActionGroup___EVENT_NOTIFICATION_MANAGEMENT();

    String ActionGroup___MANIPULATE_ROLES();

    String ActionGroup___CONFIGURE_STORAGE_POOL_NETWORK();

    String ActionGroup___CREATE_STORAGE_POOL_NETWORK();

    String ActionGroup___DELETE_STORAGE_POOL_NETWORK();

    String ActionGroup___CONFIGURE_ENGINE();

    String ActionGroup___CREATE_DISK();

    String ActionGroup___ATTACH_DISK();

    String ActionGroup___CREATE_CPU_PROFILE();

    String ActionGroup___DELETE_CPU_PROFILE();

    String ActionGroup___UPDATE_CPU_PROFILE();

    String ActionGroup___ASSIGN_CPU_PROFILE();

    String ActionGroup___EDIT_DISK_PROPERTIES();

    String ActionGroup___CONFIGURE_SCSI_GENERIC_IO();

    String ActionGroup___ACCESS_IMAGE_STORAGE();

    String ActionGroup___CONFIGURE_DISK_STORAGE();

    String ActionGroup___DISK_LIVE_STORAGE_MIGRATION();

    String ActionGroup___SPARSIFY_DISK();

    String ActionGroup___DELETE_DISK();

    String ActionGroup___CONFIGURE_NETWORK_VNIC_PROFILE();

    String ActionGroup___CREATE_NETWORK_VNIC_PROFILE();

    String ActionGroup___DELETE_NETWORK_VNIC_PROFILE();

    String ActionGroup___MANIPULATE_AFFINITY_GROUPS();

    String ActionGroup___CREATE_GLUSTER_VOLUME();

    String ActionGroup___MANIPULATE_GLUSTER_VOLUME();

    String ActionGroup___DELETE_GLUSTER_VOLUME();

    String ActionGroup___MANIPULATE_GLUSTER_HOOK();

    String ActionGroup___MANIPULATE_GLUSTER_SERVICE();

    String ActionGroup___CONFIGURE_STORAGE_DISK_PROFILE();

    String ActionGroup___CREATE_STORAGE_DISK_PROFILE();

    String ActionGroup___DELETE_STORAGE_DISK_PROFILE();

    String ActionGroup___ATTACH_DISK_PROFILE();

    String EventNotificationEntity___Host();

    String EventNotificationEntity___Cluster();

    String EventNotificationEntity___VirtHost();

    String EventNotificationEntity___Vm();

    String EventNotificationEntity___Storage();

    String EventNotificationEntity___Engine();

    String EventNotificationEntity___GlusterVolume();

    String EventNotificationEntity___DWH();

    String EventNotificationEntity___GlusterHook();

    String EventNotificationEntity___GlusterService();

    String AuditLogType___VDS_FAILURE();

    String AuditLogType___HOST_UPDATES_ARE_AVAILABLE();

    String AuditLogType___HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES();

    String AuditLogType___USER_VDS_MAINTENANCE();

    String AuditLogType___USER_VDS_MAINTENANCE_MANUAL_HA();

    String AuditLogType___USER_VDS_MAINTENANCE_MIGRATION_FAILED();

    String AuditLogType___VDS_MAINTENANCE();

    String AuditLogType___VDS_MAINTENANCE_MANUAL_HA();

    String AuditLogType___VDS_MAINTENANCE_FAILED();

    String AuditLogType___VDS_ACTIVATE_MANUAL_HA();

    String AuditLogType___VDS_ACTIVATE_MANUAL_HA_ASYNC();

    String AuditLogType___VDS_ACTIVATE_FAILED();

    String AuditLogType___VDS_RECOVER_FAILED();

    String AuditLogType___VDS_SLOW_STORAGE_RESPONSE_TIME();

    String AuditLogType___VDS_APPROVE_FAILED();

    String AuditLogType___VDS_INSTALL_FAILED();

    String AuditLogType___VDS_INITIATED_RUN_VM_FAILED();

    String AuditLogType___VM_FAILURE();

    String AuditLogType___VM_MIGRATION_START();

    String AuditLogType___VM_MIGRATION_FAILED();

    String AuditLogType___VM_MIGRATION_TO_SERVER_FAILED();

    String AuditLogType___VM_NOT_RESPONDING();

    String AuditLogType___VM_STATUS_RESTORED();

    String AuditLogType___VM_DOWN_ERROR();

    String AuditLogType___IRS_FAILURE();

    String AuditLogType___IRS_DISK_SPACE_LOW();

    String AuditLogType___IRS_DISK_SPACE_LOW_ERROR();

    String AuditLogType___NUMBER_OF_LVS_ON_STORAGE_DOMAIN_EXCEEDED_THRESHOLD();

    String AuditLogType___VDC_STOP();

    String AuditLogType___ENGINE_BACKUP_STARTED();

    String AuditLogType___ENGINE_BACKUP_COMPLETED();

    String AuditLogType___ENGINE_BACKUP_FAILED();

    String AuditLogType___CLUSTER_ALERT_HA_RESERVATION();

    String AuditLogType___HOST_INTERFACE_STATE_DOWN();

    String AuditLogType___HOST_BOND_SLAVE_STATE_DOWN();

    String AuditLogType___CLUSTER_ALERT_HA_RESERVATION_DOWN();

    String AuditLogType___GLUSTER_VOLUME_CREATE();

    String AuditLogType___GLUSTER_VOLUME_CREATE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_OPTION_ADDED();

    String AuditLogType___GLUSTER_VOLUME_OPTION_MODIFIED();

    String AuditLogType___GLUSTER_VOLUME_OPTION_SET_FAILED();

    String AuditLogType___GLUSTER_VOLUME_START();

    String AuditLogType___GLUSTER_VOLUME_START_FAILED();

    String AuditLogType___GLUSTER_VOLUME_STOP();

    String AuditLogType___GLUSTER_VOLUME_STOP_FAILED();

    String AuditLogType___GLUSTER_VOLUME_OPTIONS_RESET();

    String AuditLogType___GLUSTER_VOLUME_OPTIONS_RESET_ALL();

    String AuditLogType___GLUSTER_VOLUME_OPTIONS_RESET_FAILED();

    String AuditLogType___GLUSTER_VOLUME_DELETE();

    String AuditLogType___GLUSTER_VOLUME_DELETE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_FAILED();

    String AuditLogType___START_REMOVING_GLUSTER_VOLUME_BRICKS();

    String AuditLogType___START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED();

    String AuditLogType___GLUSTER_VOLUME_ADD_BRICK();

    String AuditLogType___GLUSTER_VOLUME_ADD_BRICK_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_START();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_START_FAILED();

    String AuditLogType___GLUSTER_GEO_REP_PUB_KEY_FETCH_FAILED();

    String AuditLogType___SET_UP_PASSWORDLESS_SSH();

    String AuditLogType___SET_UP_PASSWORDLESS_SSH_FAILED();

    String AuditLogType___GLUSTER_GET_PUB_KEY();

    String AuditLogType___GLUSTER_GEOREP_PUBLIC_KEY_WRITE_FAILED();

    String AuditLogType___GLUSTER_WRITE_PUB_KEYS();

    String AuditLogType___GLUSTER_GEOREP_SETUP_MOUNT_BROKER_FAILED();

    String AuditLogType___GLUSTER_SETUP_GEOREP_MOUNT_BROKER();

    String AuditLogType___GLUSTER_GEOREP_SESSION_CREATE_FAILED();

    String AuditLogType___CREATE_GLUSTER_VOLUME_GEOREP_SESSION();

    String AuditLogType___GLUSTER_VOLUME_GEO_REP_RESUME_FAILED();

    String AuditLogType___GLUSTER_VOLUME_GEO_REP_RESUME();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_STOP();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_STOP_FAILED();

    String AuditLogType___GEOREP_SESSION_PAUSE();

    String AuditLogType___GEOREP_SESSION_PAUSE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_FINISHED();

    String AuditLogType___GLUSTER_VOLUME_MIGRATE_BRICK_DATA_FINISHED();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_COMMIT_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_FAILED();

    String AuditLogType___GLUSTER_VOLUME_BRICK_REPLACED();

    String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_START();

    String AuditLogType___GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED();

    String AuditLogType___GLUSTER_SERVER_ADD_FAILED();

    String AuditLogType___GLUSTER_SERVER_REMOVE();

    String AuditLogType___GLUSTER_SERVER_REMOVE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_START();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_START_FAILED();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_STOP();

    String AuditLogType___GLUSTER_VOLUME_PROFILE_STOP_FAILED();

    String AuditLogType___GLUSTER_HOOK_ENABLE();

    String AuditLogType___GLUSTER_HOOK_ENABLE_FAILED();

    String AuditLogType___GLUSTER_HOOK_DISABLE();

    String AuditLogType___GLUSTER_HOOK_DISABLE_FAILED();

    String AuditLogType___GLUSTER_HOOK_UPDATED();

    String AuditLogType___GLUSTER_HOOK_UPDATE_FAILED();

    String AuditLogType___GLUSTER_HOOK_ADDED();

    String AuditLogType___GLUSTER_HOOK_ADD_FAILED();

    String AuditLogType___GLUSTER_HOOK_REMOVED();

    String AuditLogType___GLUSTER_HOOK_REMOVE_FAILED();

    String AuditLogType___GLUSTER_HOOK_REFRESH();

    String AuditLogType___GLUSTER_HOOK_REFRESH_FAILED();

    String AuditLogType___GLUSTER_VOLUME_DETAILS_REFRESH();

    String AuditLogType___GLUSTER_VOLUME_DETAILS_REFRESH_FAILED();

    String AuditLogType___GLUSTER_HOOK_CONFLICT_DETECTED();

    String AuditLogType___GLUSTER_HOOK_DETECTED_NEW();

    String AuditLogType___GLUSTER_HOOK_DETECTED_DELETE();

    String AuditLogType___GLUSTER_SERVICE_STARTED();

    String AuditLogType___GLUSTER_SERVICE_START_FAILED();

    String AuditLogType___GLUSTER_SERVICE_STOPPED();

    String AuditLogType___GLUSTER_SERVICE_STOP_FAILED();

    String AuditLogType___GLUSTER_SERVICE_RESTARTED();

    String AuditLogType___GLUSTER_SERVICE_RESTART_FAILED();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_STOP();

    String AuditLogType___GLUSTER_VOLUME_REMOVE_BRICKS_STOP_FAILED();

    String AuditLogType___GLUSTER_BRICK_STATUS_CHANGED();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_NOT_FOUND_FROM_CLI();

    String AuditLogType___REMOVE_GLUSTER_VOLUME_BRICKS_NOT_FOUND_FROM_CLI();

    String AuditLogType___GLUSTER_GEOREP_SESSION_REFRESH();

    String AuditLogType___GLUSTER_GEOREP_SESSION_REFRESH_FAILED();

    String AuditLogType___GEOREP_SESSION_STOP();

    String AuditLogType___GEOREP_SESSION_STOP_FAILED();

    String AuditLogType___GEOREP_SESSION_DELETED();

    String AuditLogType___GEOREP_SESSION_DELETE_FAILED();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_SET();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_SET_FAILED();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_SET_DEFAULT();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_SET_DEFAULT_FAILED();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_LIST();

    String AuditLogType___GLUSTER_GEOREP_CONFIG_LIST_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_DELETED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED();

    String AuditLogType___GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_ACTIVATED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_ACTIVATE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_DEACTIVATED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_DEACTIVATE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_RESTORED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_CONFIG_UPDATE_FAILED_PARTIALLY();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_CREATED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_CREATE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_SCHEDULED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_RESCHEDULED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_RESCHEDULE_FAILED();

    String AuditLogType___CREATE_GLUSTER_BRICK();

    String AuditLogType___CREATE_GLUSTER_BRICK_FAILED();

    String AuditLogType___GLUSTER_VOLUME_SNAPSHOT_SCHEDULE_DELETED();

    String AuditLogType___GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLE_FAILED();

    String AuditLogType___GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLED();

    String AuditLogType___GLUSTER_STORAGE_DOMAIN_SYNC_FAILED();

    String AuditLogType___GLUSTER_STORAGE_DOMAIN_SYNCED();

    String AuditLogType___GLUSTER_STORAGE_DOMAIN_SYNC_STARTED();

    String AuditLogType__GLUSTER_WEBHOOK_ADD_FAILED();

    String AuditLogType__GLUSTER_WEBHOOK_ADDED();

    String AuditLogType___FAULTY_MULTIPATHS_ON_HOST();

    String AuditLogType___NO_FAULTY_MULTIPATHS_ON_HOST();

    String AuditLogType___MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST();

    String ActionType___ActivateVds();

    String ActionType___RecoveryStoragePool();

    String ActionType___UpdateVmInterface();

    String ActionType___MigrateVmToServer();

    String ActionType___UpdateDisplayToCluster();

    String ActionType___ChangeDisk();

    String ActionType___MoveOrCopyDisk();

    String ActionType___LiveMigrateDisk();

    String ActionType___MoveDisk();

    String ActionType___ShutdownVm();

    String ActionType___ApproveVds();

    String ActionType___AddBond();

    String ActionType___AddNetwork();

    String ActionType___AddPermission();

    String ActionType___RemoveRole();

    String ActionType___AddRoleWithActions();

    String ActionType___AddVds();

    String ActionType___AddCluster();

    String ActionType___AddVm();

    String ActionType___AddVmFromScratch();

    String ActionType___AddVmInterface();

    String ActionType___AddVmPool();

    String ActionType___AddVmTemplate();

    String ActionType___AddVmTemplateInterface();

    String ActionType___AddVmToPool();

    String ActionType___AttachActionToRole();

    String ActionType___AttachNetworkToCluster();

    String ActionType___AttachNetworkToVdsInterface();

    String ActionType___AttachVmPoolToAdGroup();

    String ActionType___AttachVmPoolToUser();

    String ActionType___AttachVmToAdGroup();

    String ActionType___AttachVmToUser();

    String ActionType___CommitNetworkChanges();

    String ActionType___SetupNetworks();

    String ActionType___CreateAllSnapshotsFromVm();

    String ActionType___DetachActionFromRole();

    String ActionType___DetachNetworkFromVdsInterface();

    String ActionType___DetachNetworkToCluster();

    String ActionType___DetachUserFromVmFromPool();

    String ActionType___DetachVmFromAdGroup();

    String ActionType___DetachVmFromUser();

    String ActionType___DetachVmPoolFromAdGroup();

    String ActionType___DetachVmPoolFromUser();

    String ActionType___HibernateVm();

    String ActionType___ActivateDeactivateVmNic();

    String ActionType___LoginAdminUser();

    String ActionType___MaintenanceNumberOfVdss();

    String ActionType___MergeSnapshot();

    String ActionType___MigrateVm();

    String ActionType___PauseVm();

    String ActionType___RemoveAdGroup();

    String ActionType___RemoveUser();

    String ActionType___RemoveBond();

    String ActionType___RemoveNetwork();

    String ActionType___RemovePermission();

    String ActionType___RemoveVds();

    String ActionType___RemoveCluster();

    String ActionType___RemoveVm();

    String ActionType___RemoveVmFromPool();

    String ActionType___RemoveVmInterface();

    String ActionType___RemoveVmPool();

    String ActionType___RemoveVmTemplate();

    String ActionType___RemoveVmTemplateInterface();

    String ActionType___RestartVds();

    String ActionType___RestoreAllSnapshots();

    String ActionType___RunVm();

    String ActionType___StartVds();

    String ActionType___StopVds();

    String ActionType___RunVmOnce();

    String ActionType___RefreshHostCapabilities();

    String ActionType___StopVm();

    String ActionType___TryBackToAllSnapshotsOfVm();

    String ActionType___UpdateNetworkToVdsInterface();

    String ActionType___UpdateNetwork();

    String ActionType___UpdateRole();

    String ActionType___UpdateVds();

    String ActionType___UpdateCluster();

    String ActionType___UpdateVm();

    String ActionType___UpdateVmConsoleData();

    String ActionType___UpdateVmPool();

    String ActionType___UpdateVmTemplate();

    String ActionType___UpdateVmTemplateInterface();

    String ActionType___AttachVmsToTag();

    String ActionType___DetachVmFromTag();

    String ActionType___AddDisk();

    String ActionType___RemoveDisk();

    String ActionType___UpdateDisk();

    String ActionType___AttachDiskToVm();

    String ActionType___DetachDiskFromVm();

    String ActionType___AddVmAndAttachToUser();

    String ActionType___AttachUserToVmFromPool();

    String ActionType___AttachUserToVmFromPoolAndRun();

    String ActionType___AddStorageServerConnection();

    String ActionType___ForceRemoveStorageDomain();

    String ActionType___ActivateStorageDomain();

    String ActionType___FenceVdsManualy();

    String ActionType___AddEmptyStoragePool();

    String ActionType___AddNFSStorageDomain();

    String ActionType___AddSANStorageDomain();

    String ActionType___AddLocalStorageDomain();

    String ActionType___AddStoragePoolWithStorages();

    String ActionType___AttachStorageDomainToPool();

    String ActionType___DeactivateStorageDomain();

    String ActionType___DetachStorageDomainFromPool();

    String ActionType___ExtendSANStorageDomain();

    String ActionType___RemoveStorageDomain();

    String ActionType___RemoveStoragePool();

    String ActionType___UpdateStorageDomain();

    String ActionType___UpdateStoragePool();

    String ActionType___UploadDiskImage();

    String ActionType___UploadImageStatus();

    String ActionType___ProcessOvfUpdateForStorageDomain();

    String ActionType___AddEventSubscription();

    String ActionType___RemoveEventSubscription();

    String ActionType___ImportVm();

    String ActionType___ExportVm();

    String ActionType___RemoveVmFromImportExport();

    String ActionType___ImportVmTemplate();

    String ActionType___ExportVmTemplate();

    String ActionType___RemoveVmTemplateFromImportExport();

    String ActionType___AddQuota();

    String ActionType___UpdateQuota();

    String ActionType___RemoveQuota();

    String ActionType___CreateGlusterVolume();

    String ActionType___SetGlusterVolumeOption();

    String ActionType___StartGlusterVolume();

    String ActionType___StopGlusterVolume();

    String ActionType___ResetGlusterVolumeOptions();

    String ActionType___DeleteGlusterVolume();

    String ActionType___GlusterVolumeRemoveBricks();

    String ActionType___AddBricksToGlusterVolume();

    String ActionType___StartRebalanceGlusterVolume();

    String ActionType___CreateGlusterVolumeSnapshot();

    String ActionType___ScheduleGlusterVolumeSnapshot();

    String ActionType___RescheduleGlusterVolumeSnapshot();

    String ActionType___ReplaceGlusterVolumeBrick();

    String ActionType___GlusterHostAdd();

    String ActionType___StartGlusterVolumeProfile();

    String ActionType___StopGlusterVolumeProfile();

    String ActionType___EnableGlusterHook();

    String ActionType___DisableGlusterHook();

    String ActionType___UpdateGlusterHook();

    String ActionType___AddGlusterHook();

    String ActionType___RemoveGlusterHook();

    String ActionType___RefreshGlusterHook();

    String ActionType___ManageGlusterService();

    String ActionType___RefreshGeoRepSessions();

    String ActionType___CreateGlusterVolumeGeoRepSession();

    String ActionType___StopGeoRepSession();

    String ActionType___DeleteGeoRepSession();

    String ActionType___StartGlusterVolumeGeoRep();

    String ActionType___PauseGlusterVolumeGeoRepSession();

    String ActionType___ResumeGeoRepSession();

    String ActionType___UpdateGlusterVolumeSnapshotConfigCommand();

    String ActionType___ConnectStorageToVds();

    String ActionType___AddVnicProfile();

    String ActionType___UpdateProfile();

    String ActionType___RemoveVnicProfile();

    String ActionType___AddSubnetToProvider();

    String ActionType___RemoveSubnetFromProvider();

    String ActionType___DeleteGlusterVolumeSnapshot();

    String ActionType___DeleteAllGlusterVolumeSnapshots();

    String ActionType___ActivateGlusterVolumeSnapshot();

    String ActionType___DeactivateGlusterVolumeSnapshot();

    String ActionType___RestoreGlusterVolumeSnapshot();

    String ActionType___SyncStorageDevices();

    String ActionType___CreateBrick();

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

    String VdcObjectType___Clusters();

    String VdcObjectType___Network();

    String VdcObjectType___VM();

    String VdcObjectType___VmPool();

    String VdcObjectType___VmTemplate();

    String VdcObjectType___EventNotification();

    String VdcObjectType___ImportExport();

    String VdcObjectType___GlusterHook();

    String VdcObjectType___UpdateWatchdog();

    String VdcObjectType___RemoveWatchdog();

    String VdcObjectType___AddWatchdog();

    String VdcObjectType___GlusterService();

    String VdcObjectType___VnicProfile();

    String VdcObjectType___DiskProfile();

    String ActionType___AddLibvirtSecret();

    String ActionType___UpdateLibvirtSecret();

    String VdsSelectionAlgorithm___None();

    String VdsSelectionAlgorithm___EvenlyDistribute();

    String VdsSelectionAlgorithm___PowerSave();

    String AuditLogType___DWH_STOPPED();

    String AuditLogType___DWH_STARTED();

    String AuditLogType___DWH_ERROR();

    String AuditLogType___VDS_TIME_DRIFT_ALERT();

    String AuditLogType___HA_VM_RESTART_FAILED();

    String AuditLogType___HA_VM_FAILED();

    String AuditLogType___EXCEEDED_MAXIMUM_NUM_OF_RESTART_HA_VM_ATTEMPTS();

    String AuditLogType___COLD_REBOOT_VM_DOWN();

    String AuditLogType___COLD_REBOOT_FAILED();

    String AuditLogType___EXCEEDED_MAXIMUM_NUM_OF_COLD_REBOOT_VM_ATTEMPTS();

    String AuditLogType___SYSTEM_DEACTIVATED_STORAGE_DOMAIN();

    String AuditLogType___VDS_SET_NONOPERATIONAL();

    String AuditLogType___VDS_SET_NONOPERATIONAL_IFACE_DOWN();

    String AuditLogType___VDS_SET_NONOPERATIONAL_DOMAIN();

    String AuditLogType___SYSTEM_CHANGE_STORAGE_POOL_STATUS_NO_HOST_FOR_SPM();

    String AuditLogType___VDS_LOW_MEM();

    String AuditLogType___VDS_HIGH_MEM_USE();

    String AuditLogType___HOST_INTERFACE_HIGH_NETWORK_USE();

    String AuditLogType___VDS_HIGH_CPU_USE();

    String AuditLogType___VDS_HIGH_SWAP_USE();

    String AuditLogType___VDS_LOW_SWAP();

    String MigrationSupport___MIGRATABLE();

    String MigrationSupport___IMPLICITLY_NON_MIGRATABLE();

    String MigrationSupport___PINNED_TO_HOST();

    String QuotaEnforcementTypeEnum___DISABLED();

    String QuotaEnforcementTypeEnum___SOFT_ENFORCEMENT();

    String QuotaEnforcementTypeEnum___HARD_ENFORCEMENT();

    String VdsTransparentHugePagesState___Never();

    String VdsTransparentHugePagesState___MAdvise();

    String VdsTransparentHugePagesState___Always();

    String GlusterHookStage___PRE();

    String GlusterHookStage___POST();

    String GlusterHookStatus___ENABLED();

    String GlusterHookStatus___DISABLED();

    String GlusterHookStatus___MISSING();

    String GlusterHookContentType___TEXT();

    String GlusterHookContentType___BINARY();

    String GlusterServiceStatus___RUNNING();

    String GlusterServiceStatus___STOPPED();

    String GlusterServiceStatus___MIXED();

    String GlusterServiceStatus___NOT_AVAILABLE();

    String GlusterServiceStatus___UNKNOWN();

    String RaidType___NONE();

    String RaidType___RAID0();

    String RaidType___RAID6();

    String RaidType___RAID10();

    String GlusterVolumeSnapshotScheduleRecurrence___INTERVAL();

    String GlusterVolumeSnapshotScheduleRecurrence___HOURLY();

    String GlusterVolumeSnapshotScheduleRecurrence___DAILY();

    String GlusterVolumeSnapshotScheduleRecurrence___WEEKLY();

    String GlusterVolumeSnapshotScheduleRecurrence___MONTHLY();

    String GlusterVolumeSnapshotScheduleRecurrence___UNKNOWN();

    String GlusterGeoRepNonEligibilityReason___SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER();

    String GlusterGeoRepNonEligibilityReason___SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE();

    String GlusterGeoRepNonEligibilityReason___SLAVE_CLUSTER_AND_MASTER_CLUSTER_COMPATIBILITY_VERSIONS_DO_NOT_MATCH();

    String GlusterGeoRepNonEligibilityReason___SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION();

    String GlusterGeoRepNonEligibilityReason___SLAVE_VOLUME_SHOULD_BE_UP();

    String GlusterGeoRepNonEligibilityReason___SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE();

    String GlusterGeoRepNonEligibilityReason___SLAVE_VOLUME_TO_BE_EMPTY();

    String GlusterGeoRepNonEligibilityReason___NO_UP_SLAVE_SERVER();

    String GlusterGeoRepNonEligibilityReason___MASTER_VOLUME_SIZE_TO_BE_AVAILABLE();

    String SizeConverter$SizeUnit___BYTES();

    String SizeConverter$SizeUnit___KiB();

    String SizeConverter$SizeUnit___MiB();

    String SizeConverter$SizeUnit___GiB();

    String SizeConverter$SizeUnit___TiB();

    String SizeConverter$SizeUnit___PiB();

    String SizeConverter$SizeUnit___EiB();

    String SizeConverter$SizeUnit___ZiB();

    String SizeConverter$SizeUnit___YiB();

    String VmWatchdogType___i6300esb();

    String VmWatchdogAction___NONE();

    String VmWatchdogAction___RESET();

    String VmWatchdogAction___POWEROFF();

    String VmWatchdogAction___DUMP();

    String VmWatchdogAction___PAUSE();

    String VmType___Desktop();

    String VmType___Server();

    String AuditLogType___VDS_UNTRUSTED();

    String AuditLogType___USER_UPDATE_VM_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___USER_UPDATE_VM_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___USER_ADD_VM_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___USER_ADD_VM_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___USER_UPDATE_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED();

    String AuditLogType___USER_UPDATE_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED();

    String AuditLogType___VM_CONSOLE_CONNECTED();

    String AuditLogType___VM_CONSOLE_DISCONNECTED();

    String AuditLogType___VM_SET_TICKET();

    String AuditLogType___NETWORK_UPDATE_DISPLAY_FOR_CLUSTER_WITH_ACTIVE_VM();

    String AuditLogType___NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM();

    String AuditLogType___VM_PAUSED();

    String AuditLogType___VM_PAUSED_EIO();

    String AuditLogType___VM_PAUSED_ENOSPC();

    String AuditLogType___VM_PAUSED_EPERM();

    String AuditLogType___VM_PAUSED_ERROR();

    String AuditLogType___VM_RECOVERED_FROM_PAUSE_ERROR();

    String AuditLogType___HOST_CERTIFICATION_HAS_EXPIRED();

    String AuditLogType___HOST_CERTIFICATE_HAS_INVALID_SAN();

    String AuditLogType___HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE();

    String AuditLogType___ENGINE_CERTIFICATION_HAS_EXPIRED();

    String AuditLogType___ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE();

    String AuditLogType___ENGINE_CA_CERTIFICATION_HAS_EXPIRED();

    String AuditLogType___ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE();

    String UnitVmModel$CpuSharesAmount___DISABLED();

    String UnitVmModel$CpuSharesAmount___HIGH();

    String UnitVmModel$CpuSharesAmount___MEDIUM();

    String UnitVmModel$CpuSharesAmount___LOW();

    String UnitVmModel$CpuSharesAmount___CUSTOM();

    String PolicyUnitType___Filter();

    String PolicyUnitType___Weight();

    String PolicyUnitType___LoadBalancing();

    String LdapRefStatus___Inactive();

    String LdapRefStatus___Active();

    String KdumpStatus___UNKNOWN();

    String KdumpStatus___DISABLED();

    String KdumpStatus___ENABLED();

    String SELinuxMode___ENFORCING();

    String SELinuxMode___PERMISSIVE();

    String SELinuxMode___DISABLED();

    String ActionGroup___CREATE_MAC_POOL();

    String ActionGroup___EDIT_MAC_POOL();

    String ActionGroup___DELETE_MAC_POOL();

    String ActionGroup___CONFIGURE_MAC_POOL();

    String NumaTuneMode___STRICT();

    String NumaTuneMode___PREFERRED();

    String NumaTuneMode___INTERLEAVE();

    String ImportSource___EXPORT_DOMAIN();

    String ImportSource___VMWARE();

    String ImportSource___OVA();

    String ImportSource___XEN();

    String ImportSource___KVM();

    String ConsoleDisconnectAction___NONE();

    String ConsoleDisconnectAction___LOCK_SCREEN();

    String ConsoleDisconnectAction___LOGOUT();

    String ConsoleDisconnectAction___SHUTDOWN();

    String ConsoleDisconnectAction___REBOOT();

    String CpuPinningPolicy___NONE();

    String CpuPinningPolicy___MANUAL();

    String CpuPinningPolicy___RESIZE_AND_PIN_NUMA();

    String SerialNumberPolicy___HOST_ID();

    String SerialNumberPolicy___VM_ID();

    String SerialNumberPolicy___CUSTOM();

    String ArchitectureType___undefined();

    String FipsMode___UNDEFINED();

    String FipsMode___DISABLED();

    String FipsMode___ENABLED();

    String HostedEngineDeployConfiguration$Action___NONE();

    String HostedEngineDeployConfiguration$Action___DEPLOY();

    String HostedEngineDeployConfiguration$Action___UNDEPLOY();

    String ReplaceHostConfiguration$Action___NONE();

    String ReplaceHostConfiguration$Action___SAMEFQDN();

    String ReplaceHostConfiguration$Action___DIFFERENTFQDN();

    String AuditLogType___MAC_ADDRESS_IS_EXTERNAL();

    String EntityAffinityRule___DISABLED();

    String EntityAffinityRule___NEGATIVE();

    String EntityAffinityRule___POSITIVE();

    String SwitchType___LEGACY();

    String SwitchType___OVS();

    String FirewallType___IPTABLES();

    String FirewallType___FIREWALLD();

    String LogMaxMemoryUsedThresholdType___PERCENTAGE();

    String LogMaxMemoryUsedThresholdType___ABSOLUTE_VALUE();

    String NetworkStatus___NON_OPERATIONAL();

    String NetworkStatus___OPERATIONAL();

    String StorageDomainStatus___Activating();

    String StorageDomainStatus___Active();

    String StorageDomainStatus___Inactive();

    String StorageDomainStatus___Unattached();

    String StorageDomainStatus___Uninitialized();

    String StorageDomainStatus___Unknown();

    String StorageDomainStatus___Locked();

    String StorageDomainStatus___Maintenance();

    String StorageDomainStatus___PreparingForMaintenance();

    String StorageDomainStatus___Detaching();

    String StorageDomainSharedStatus___Active();

    String StorageDomainSharedStatus___Inactive();

    String StorageDomainSharedStatus___Unattached();

    String StorageDomainSharedStatus___Mixed();

    String StorageDomainSharedStatus___Locked();

    String StoragePoolStatus___Maintenance();

    String StoragePoolStatus___Notconfigured();

    String StoragePoolStatus___Uninitialized();

    String StoragePoolStatus___Up();

    String StoragePoolStatus___NotOperational();

    String StoragePoolStatus___NonResponsive();

    String StoragePoolStatus___Contend();

    String StorageType___ALL();

    String StorageType___FCP();

    String StorageType___ISCSI();

    String StorageType___LOCALFS();

    String StorageType___NFS();

    String StorageType___POSIXFS();

    String StorageType___MANAGED_BLOCK_STORAGE();

    String StorageType___GLUSTERFS();

    String StorageType___GLANCE();

    String StorageType___CINDER();

    String StorageType___UNKNOWN();

    String NfsVersion___V3();

    String NfsVersion___V4();

    String NfsVersion___V4_0();

    String NfsVersion___V4_1();

    String NfsVersion___AUTO();

    String StorageFormatType___V1();

    String StorageFormatType___V2();

    String StorageFormatType___V3();

    String StorageFormatType___V4();

    String StorageFormatType___V5();

    String VolumeFormat___COW();

    String VolumeFormat___RAW();

    String VDSStatus___Down();

    String VDSStatus___Error();

    String VDSStatus___Initializing();

    String VDSStatus___InstallFailed();

    String VDSStatus___Installing();

    String VDSStatus___Maintenance();

    String VDSStatus___NonOperational();

    String VDSStatus___NonResponsive();

    String VDSStatus___PendingApproval();

    String VDSStatus___PreparingForMaintenance();

    String VDSStatus___Reboot();

    String VDSStatus___Unassigned();

    String VDSStatus___Up();

    String VDSStatus___Connecting();

    String VDSStatus___InstallingOS();

    String VdsTransparentHugePages___Never();

    String VdsTransparentHugePages___MAdvise();

    String VdsTransparentHugePages___Always();

    String VMStatus___Down();

    String VMStatus___ImageIllegal();

    String VMStatus___ImageLocked();

    String VMStatus___MigratingFrom();

    String VMStatus___MigratingTo();

    String VMStatus___NotResponding();

    String VMStatus___Paused();

    String VMStatus___PoweringDown();

    String VMStatus___PoweringUp();

    String VMStatus___RebootInProgress();

    String VMStatus___RestoringState();

    String VMStatus___SavingState();

    String VMStatus___Suspended();

    String VMStatus___Unassigned();

    String VMStatus___Unknown();

    String VMStatus___Up();

    String VMStatus___WaitForLaunch();

    String DisplayType___qxl();

    String DisplayType___cirrus();

    String DisplayType___vga();

    String DisplayType___none();

    String GraphicsType___SPICE();

    String GraphicsType___VNC();

    String UnitVmModel$GraphicsTypes___NONE();

    String UnitVmModel$GraphicsTypes___SPICE();

    String UnitVmModel$GraphicsTypes___VNC();

    String UnitVmModel$GraphicsTypes___SPICE_AND_VNC();

    String OriginType___RHEV();

    String OriginType___VMWARE();

    String OriginType___XEN();

    String OriginType___OVIRT();

    String OriginType___HOSTED_ENGINE();

    String OriginType___EXTERNAL();

    String OriginType___PHYSICAL_MACHINE();

    String OriginType___HYPERV();

    String OriginType___MANAGED_HOSTED_ENGINE();

    String OriginType___KVM();

    String OriginType___KUBEVIRT();

    @Deprecated
    String VmInterfaceType___rtl8139_pv();

    String VmInterfaceType___rtl8139();

    String VmInterfaceType___e1000();

    String VmInterfaceType___pv();

    String VmInterfaceType___spaprVlan();

    String VmInterfaceType___pciPassthrough();

    String VmType___HighPerformance();

    String VDSType___VDS();

    String VDSType___oVirtNode();

    String StorageDomainType___Master();

    String StorageDomainType___Data();

    String StorageDomainType___ISO();

    String StorageDomainType___ImportExport();

    String StorageDomainType___Image();

    String StorageDomainType___ManagedBlockStorage();

    String VmTemplateStatus___OK();

    String VmTemplateStatus___Locked();

    String VmTemplateStatus___Illegal();

    String VolumeType___Preallocated();

    String VolumeType___Sparse();

    String VolumeType___Unassigned();

    String VolumeClassification___Volume();

    String VolumeClassification___Snapshot();

    String VolumeClassification___Invalid();

    String VmPoolType___AUTOMATIC();

    String VmPoolType___MANUAL();

    String AdRefStatus___Inactive();

    String AdRefStatus___Active();

    String Ipv4BootProtocol___NONE();

    String Ipv4BootProtocol___DHCP();

    String Ipv4BootProtocol___STATIC_IP();

    String Ipv6BootProtocol___NONE();

    String Ipv6BootProtocol___DHCP();

    String Ipv6BootProtocol___STATIC_IP();

    String Ipv6BootProtocol___AUTOCONF();

    String Ipv6BootProtocol___POLY_DHCP_AUTOCONF();

    String VmEntityType___VM();

    String VmEntityType___TEMPLATE();

    String ImageStatus___Unassigned();

    String ImageStatus___OK();

    String ImageStatus___LOCKED();

    String ImageStatus___ILLEGAL();

    String DiskInterface___IDE();

    String DiskInterface___SCSI();

    String DiskInterface___VirtIO();

    String DiskInterface___VirtIO_SCSI();

    String DiskInterface___SPAPR_VSCSI();

    String Snapshot$SnapshotStatus___OK();

    String Snapshot$SnapshotStatus___LOCKED();

    String Snapshot$SnapshotStatus___IN_PREVIEW();

    String Snapshot$SnapshotType___REGULAR();

    String Snapshot$SnapshotType___ACTIVE();

    String Snapshot$SnapshotType___STATELESS();

    String Snapshot$SnapshotType___PREVIEW();

    String VdsSpmStatus___None();

    String VdsSpmStatus___Contending();

    String VdsSpmStatus___SPM();

    String LunStatus___Free();

    String LunStatus___Used();

    String LunStatus___Unusable();

    String DiskStorageType___LUN();

    String DiskStorageType___IMAGE();

    String DiskStorageType___CINDER();

    String RoleType___ADMIN();

    String RoleType___USER();

    String JobExecutionStatus___STARTED();

    String JobExecutionStatus___FINISHED();

    String JobExecutionStatus___FAILED();

    String JobExecutionStatus___ABORTED();

    String JobExecutionStatus___UNKNOWN();

    String ProviderType___FOREMAN();

    String ProviderType___VMWARE();

    String ProviderType___KUBEVIRT();

    String ProviderType___OPENSTACK_NETWORK();

    String ProviderType___EXTERNAL_NETWORK();

    String OpenstackNetworkPluginType___LINUX_BRIDGE();

    String OpenstackNetworkPluginType___OPEN_VSWITCH();

    String ExternalNetworkPluginType___OVIRT_PROVIDER_OVN();

    String ProviderType___OPENSTACK_IMAGE();

    String GlusterVolumeType___DISTRIBUTE();

    String GlusterVolumeType___REPLICATE();

    String GlusterVolumeType___DISTRIBUTED_REPLICATE();

    String GlusterVolumeType___STRIPE();

    String GlusterVolumeType___DISTRIBUTED_STRIPE();

    String GlusterVolumeType___STRIPED_REPLICATE();

    String GlusterVolumeType___DISTRIBUTED_STRIPED_REPLICATE();

    String GlusterVolumeType___DISPERSE();

    String GlusterVolumeType___DISTRIBUTED_DISPERSE();

    String GlusterVolumeType___TIER();

    String GlusterVolumeType___UNKNOWN();

    String GlusterStatus___UP();

    String GlusterStatus___DOWN();

    String TransportType___TCP();

    String TransportType___RDMA();

    String ServiceType___NFS();

    String ServiceType___SHD();

    String ServiceType___GLUSTER_SWIFT();

    String ExternalSubnet$IpVersion___IPV4();

    String ExternalSubnet$IpVersion___IPV6();

    String OpenstackNetworkProviderProperties$BrokerType___QPID();

    String OpenstackNetworkProviderProperties$BrokerType___RABBIT_MQ();

    String LibvirtSecretUsageType___CEPH();

    String MigrationBandwidthLimitType___AUTO();

    String MigrationBandwidthLimitType___VDSM_CONFIG();

    String MigrationBandwidthLimitType___CUSTOM();

    String ImageInfoModel$QemuCompat___V2();

    String ImageInfoModel$QemuCompat___V3();

    String DiskContentType___DATA();

    String DiskContentType___OVF_STORE();

    String DiskContentType___MEMORY_DUMP_VOLUME();

    String DiskContentType___MEMORY_METADATA_VOLUME();

    String DiskContentType___ISO();

    String DiskContentType___HOSTED_ENGINE();

    String DiskContentType___HOSTED_ENGINE_SANLOCK();

    String DiskContentType___HOSTED_ENGINE_METADATA();

    String DiskContentType___HOSTED_ENGINE_CONFIGURATION();

    String DiskContentType___BACKUP_SCRATCH();

    String VmResumeBehavior___AUTO_RESUME();

    String VmResumeBehavior___LEAVE_PAUSED();

    String VmResumeBehavior___KILL();

    String AuditLogType___GLUSTER_VOLUME_REBALANCE_START_DETECTED_FROM_CLI();

    String AuditLogType___START_REMOVING_GLUSTER_VOLUME_BRICKS_DETECTED_FROM_CLI();

    String ActionType___ResetGlusterVolumeBrick();

    String BiosType___I440FX_SEA_BIOS();

    String BiosType___Q35_OVMF();

    String BiosType___Q35_SEA_BIOS();

    String BiosType___Q35_SECURE_BOOT();

    String CloudInitNetworkProtocol___ENI();

    String CloudInitNetworkProtocol___OPENSTACK_METADATA();

    String VirtioMultiQueueType___DISABLED();

    String VirtioMultiQueueType___AUTOMATIC();

    String VirtioMultiQueueType___CUSTOM();
}

