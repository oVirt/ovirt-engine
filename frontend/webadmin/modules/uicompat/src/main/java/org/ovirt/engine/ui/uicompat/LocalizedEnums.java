package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface LocalizedEnums extends ConstantsWithLookup {
    String NonOperationalReason___NONE();

    String NonOperationalReason___GENERAL();

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

    String NonOperationalReason___LIBRBD_PACKAGE_NOT_AVAILABLE();

    String NonOperationalReason___VDS_CANNOT_CONNECT_TO_GLUSTERFS();

    String NonOperationalReason___LIBVIRT_SECRETS_REGISTRATION_FAILURE();

    String UsbPolicy___ENABLED_LEGACY();

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

    String VdcActionType___ActivateVds();

    String VdcActionType___RecoveryStoragePool();

    String VdcActionType___UpdateVmInterface();

    String VdcActionType___MigrateVmToServer();

    String VdcActionType___UpdateDisplayToCluster();

    String VdcActionType___ChangeDisk();

    String VdcActionType___MoveOrCopyDisk();

    String VdcActionType___LiveMigrateDisk();

    String VdcActionType___MoveDisk();

    String VdcActionType___ShutdownVm();

    String VdcActionType___ApproveVds();

    String VdcActionType___AddBond();

    String VdcActionType___AddNetwork();

    String VdcActionType___AddPermission();

    String VdcActionType___RemoveRole();

    String VdcActionType___AddRoleWithActions();

    String VdcActionType___AddVds();

    String VdcActionType___AddCluster();

    String VdcActionType___AddVm();

    String VdcActionType___AddVmFromScratch();

    String VdcActionType___AddVmInterface();

    String VdcActionType___AddVmPoolWithVms();

    String VdcActionType___AddVmTemplate();

    String VdcActionType___AddVmTemplateInterface();

    String VdcActionType___AddVmToPool();

    String VdcActionType___AttachActionToRole();

    String VdcActionType___AttachNetworkToCluster();

    String VdcActionType___AttachNetworkToVdsInterface();

    String VdcActionType___AttachVmPoolToAdGroup();

    String VdcActionType___AttachVmPoolToUser();

    String VdcActionType___AttachVmToAdGroup();

    String VdcActionType___AttachVmToUser();

    String VdcActionType___CommitNetworkChanges();

    String VdcActionType___SetupNetworks();

    String VdcActionType___CreateAllSnapshotsFromVm();

    String VdcActionType___DetachActionFromRole();

    String VdcActionType___DetachNetworkFromVdsInterface();

    String VdcActionType___DetachNetworkToCluster();

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

    String VdcActionType___RemoveCluster();

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

    String VdcActionType___RefreshHostCapabilities();

    String VdcActionType___StopVm();

    String VdcActionType___TryBackToAllSnapshotsOfVm();

    String VdcActionType___UpdateNetworkToVdsInterface();

    String VdcActionType___UpdateNetwork();

    String VdcActionType___UpdateRole();

    String VdcActionType___UpdateVds();

    String VdcActionType___UpdateCluster();

    String VdcActionType___UpdateVm();

    String VdcActionType___UpdateVmConsoleData();

    String VdcActionType___UpdateVmPoolWithVms();

    String VdcActionType___UpdateVmTemplate();

    String VdcActionType___UpdateVmTemplateInterface();

    String VdcActionType___AttachVmsToTag();

    String VdcActionType___DetachVmFromTag();

    String VdcActionType___AddDisk();

    String VdcActionType___RemoveDisk();

    String VdcActionType___UpdateVmDisk();

    String VdcActionType___AttachDiskToVm();

    String VdcActionType___DetachDiskFromVm();

    String VdcActionType___AddVmAndAttachToUser();

    String VdcActionType___AttachUserToVmFromPool();

    String VdcActionType___AttachUserToVmFromPoolAndRun();

    String VdcActionType___AddStorageServerConnection();

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

    String VdcActionType___UploadDiskImage();

    String VdcActionType___UploadImageStatus();

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

    String VdcActionType___CreateGlusterVolume();

    String VdcActionType___SetGlusterVolumeOption();

    String VdcActionType___StartGlusterVolume();

    String VdcActionType___StopGlusterVolume();

    String VdcActionType___ResetGlusterVolumeOptions();

    String VdcActionType___DeleteGlusterVolume();

    String VdcActionType___GlusterVolumeRemoveBricks();

    String VdcActionType___AddBricksToGlusterVolume();

    String VdcActionType___StartRebalanceGlusterVolume();

    String VdcActionType___CreateGlusterVolumeSnapshot();

    String VdcActionType___ScheduleGlusterVolumeSnapshot();

    String VdcActionType___RescheduleGlusterVolumeSnapshot();

    String VdcActionType___ReplaceGlusterVolumeBrick();

    String VdcActionType___GlusterHostAdd();

    String vdcActionType___StartGlusterVolumeProfile();

    String vdcActionType___StopGlusterVolumeProfile();

    String VdcActionType___EnableGlusterHook();

    String VdcActionType___DisableGlusterHook();

    String VdcActionType___UpdateGlusterHook();

    String VdcActionType___AddGlusterHook();

    String VdcActionType___RemoveGlusterHook();

    String VdcActionType___RefreshGlusterHook();

    String VdcActionType___ManageGlusterService();

    String VdcActionType___RefreshGeoRepSessions();

    String VdcActionType___CreateGlusterVolumeGeoRepSession();

    String VdcActionType___StopGeoRepSession();

    String VdcActionType___DeleteGeoRepSession();

    String VdcActionType___StartGlusterVolumeGeoRep();

    String VdcActionType___PauseGlusterVolumeGeoRepSession();

    String VdcActionType___ResumeGeoRepSession();

    String VdcActionType___UpdateGlusterVolumeSnapshotConfigCommand();

    String VdcActionType___ConnectStorageToVds();

    String VdcActionType___AddVnicProfile();

    String VdcActionType___UpdateProfile();

    String VdcActionType___RemoveVnicProfile();

    String VdcActionType___AddSubnetToProvider();

    String VdcActionType___RemoveSubnetFromProvider();

    String VdcActionType___DeleteGlusterVolumeSnapshot();

    String VdcActionType___DeleteAllGlusterVolumeSnapshots();

    String VdcActionType___ActivateGlusterVolumeSnapshot();

    String VdcActionType___DeactivateGlusterVolumeSnapshot();

    String VdcActionType___RestoreGlusterVolumeSnapshot();

    String VdcActionType___SyncStorageDevices();

    String VdcActionType___CreateBrick();

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

    String VdcActionType___AddLibvirtSecret();

    String VdcActionType___UpdateLibvirtSecret();

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

    String AuditLogType___HOST_CERTIFICATION_HAS_EXPIRED();

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

    String SerialNumberPolicy___HOST_ID();

    String SerialNumberPolicy___VM_ID();

    String SerialNumberPolicy___CUSTOM();

    String ArchitectureType___undefined();

    String HostedEngineDeployConfiguration$Action___NONE();

    String HostedEngineDeployConfiguration$Action___DEPLOY();

    String HostedEngineDeployConfiguration$Action___UNDEPLOY();

    String AuditLogType___MAC_ADDRESS_IS_EXTERNAL();
}

