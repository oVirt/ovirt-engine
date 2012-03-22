package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface Enums extends ConstantsWithLookup {
    String NetworkStatus___NonOperational();

    String NetworkStatus___Operational();

    String StorageDomainStatus___Active();

    String StorageDomainStatus___InActive();

    String StorageDomainStatus___Unattached();

    String StorageDomainStatus___Uninitialized();

    String StorageDomainStatus___Unknown();

    String StorageDomainStatus___Locked();

    String StorageDomainStatus___Maintenance();

    String StorageDomainSharedStatus___Active();

    String StorageDomainSharedStatus___InActive();

    String StorageDomainSharedStatus___Unattached();

    String StorageDomainSharedStatus___Mixed();

    String StorageDomainSharedStatus___Locked();

    String StoragePoolStatus___Maintanance();

    String StoragePoolStatus___Notconfigured();

    String StoragePoolStatus___Uninitialized();

    String StoragePoolStatus___Up();

    String StoragePoolStatus___NotOperational();

    String StoragePoolStatus___Problematic();

    String StorageType___ALL();

    String StorageType___FCP();

    String StorageType___ISCSI();

    String StorageType___LOCALFS();

    String StorageType___NFS();

    String StorageType___UNKNOWN();

    String StorageFormatType___V1();

    String StorageFormatType___V2();

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

    String VDSStatus___Problematic();

    String VdsTransparentHugePages___Never();

    String VdsTransparentHugePages___MAdvise();

    String VdsTransparentHugePages___Always();

    String VmOsType___Unassigned();

    String VmOsType___WindowsXP();

    String VmOsType___Windows2003();

    String VmOsType___Windows2003x64();

    String VmOsType___Windows2008();

    String VmOsType___Windows2008x64();

    String VmOsType___Windows2008R2x64();

    String VmOsType___Windows7();

    String VmOsType___Windows7x64();

    String VmOsType___OtherLinux();

    String VmOsType___Other();

    String VmOsType___RHEL6();

    String VmOsType___RHEL6x64();

    String VmOsType___RHEL5();

    String VmOsType___RHEL5x64();

    String VmOsType___RHEL4();

    String VmOsType___RHEL4x64();

    String VmOsType___RHEL3();

    String VmOsType___RHEL3x64();

    String VMStatus___Down();

    String VMStatus___ImageIllegal();

    String VMStatus___ImageLocked();

    String VMStatus___MigratingFrom();

    String VMStatus___MigratingTo();

    String VMStatus___NotResponding();

    String VMStatus___Paused();

    String VMStatus___PoweredDown();

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

    String VdcActionType___ActivateVds();

    String VdcActionType___RecoveryStoragePool();

    String VdcActionType___UpdateVmInterface();

    String VdcActionType___MigrateVmToServer();

    String VdcActionType___UpdateDisplayToVdsGroup();

    String VdcActionType___ChangeDisk();

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

    String VdcActionType___LoginAdminUser();

    String VdcActionType___MaintananceNumberOfVdss();

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

    String VdcActionType___UpdateNetwork();

    String VdcActionType___UpdateRole();

    String VdcActionType___UpdateUserToTimeLeasedPool();

    String VdcActionType___UpdateVds();

    String VdcActionType___UpdateVdsGroup();

    String VdcActionType___UpdateVm();

    String VdcActionType___UpdateVmPoolWithVms();

    String VdcActionType___UpdateVmTemplate();

    String VdcActionType___MoveOrCopyTemplate();

    String VdcActionType___UpdateVmTemplateInterface();

    String VdcActionType___AttachVmsToTag();

    String VdcActionType___DetachVmFromTag();

    String VdcActionType___AddDiskToVm();

    String VdcActionType___RemoveDiskFromVm();

    String VdcActionType___UpdateVmDisk();

    String VdcActionType___MoveVm();

    String VdcActionType___AddVmAndAttachToUser();

    String VdcActionType___AttachUserToVmFromPool();

    String VdcActionType___AttachUserToVmFromPoolAndRun();

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

    String VdcActionType___RemoveDisksFromVm();

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

    String DisplayType___qxl();

    String DisplayType___vnc();

    String OriginType___RHEV();

    String OriginType___VMWARE();

    String OriginType___XEN();

    String OriginType___OVIRT();

    String VmInterfaceType___rtl8139_pv();

    String VmInterfaceType___rtl8139();

    String VmInterfaceType___e1000();

    String VmInterfaceType___pv();

    String TabType___Hosts();

    String TabType___Vms();

    String TabType___Users();

    String TabType___Templates();

    String TabType___Events();

    String TabType___DataCenters();

    String TabType___Clusters();

    String TabType___Storage();

    String TabType___Pools();

    String VDSType___VDS();

    String VDSType___PowerClient();

    String VDSType___oVirtNode();

    String VdsSelectionAlgorithm___None();

    String VdsSelectionAlgorithm___EvenlyDistribute();

    String VdsSelectionAlgorithm___PowerSave();

    String VdcActionType___ActivateStorageDomain();

    String VdcActionType___FenceVdsManualy();

    String VdcActionType___AddEmptyStoragePool();

    String VdcActionType___AddNFSStorageDomain();

    String VdcActionType___AddSANStorageDomain();

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

    String EventNotificationEntity___VDS();

    String EventNotificationEntity___VM();

    String EventNotificationEntity___IRS();

    String EventNotificationEntity___VDC();

    String StorageDomainType___Master();

    String StorageDomainType___Data();

    String StorageDomainType___ISO();

    String StorageDomainType___ImportExport();

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

    String VmTemplateStatus___OK();

    String VmTemplateStatus___Locked();

    String VmTemplateStatus___Illegal();

    String UsbPolicy___Enabled();

    String UsbPolicy___Disabled();

    String VolumeType___Preallocated();

    String VolumeType___Sparse();

    String VolumeType___Unassigned();

    String VmPoolType___Automatic();

    String VmPoolType___Manual();

    String VmPoolType___TimeLease();

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

    String ActionGroup___CONFIGURE_TEMPLATE_NETWORK();

    String ActionGroup___COPY_TEMPLATE();

    String ActionGroup___CREATE_CLUSTER();

    String ActionGroup___DELETE_CLUSTER();

    String ActionGroup___EDIT_CLUSTER_CONFIGURATION();

    String ActionGroup___CONFIGURE_CLUSTER_NETWORK();

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

    String ActionGroup___IMPORT_EXPORT_VM();

    String ActionGroup___CONFIGURE_VM_NETWORK();

    String ActionGroup___CONFIGURE_VM_STORAGE();

    String ActionGroup___VM_BASIC_OPERATIONS();

    String ActionGroup___CHANGE_VM_CD();

    String ActionGroup___CONNECT_TO_VM();

    String ActionGroup___MANIPULATE_VM_SNAPSHOTS();

    String ActionGroup___CREATE_VM_POOL();

    String ActionGroup___DELETE_VM_POOL();

    String ActionGroup___EDIT_VM_POOL_CONFIGURATION();

    String ActionGroup___VM_POOL_BASIC_OPERATIONS();

    String ActionGroup___MANIPULATE_USERS();

    String ActionGroup___MANIPULATE_PERMISSIONS();

    String ActionGroup___MANIPULATE_ROLES();

    String ActionGroup___CONFIGURE_STORAGE_POOL_NETWORK();

    String ActionGroup___CONFIGURE_ENGINE();

    String NonOperationalReason___NONE();

    String NonOperationalReason___GENERAL();

    String NonOperationalReason___CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___STORAGE_DOMAIN_UNREACHABLE();

    String NonOperationalReason___NETWORK_UNREACHABLE();

    String NonOperationalReason___VERSION_INCOMPATIBLE_WITH_CLUSTER();

    String NonOperationalReason___KVM_NOT_RUNNING();

    String NonOperationalReason___TIMEOUT_RECOVERING_FROM_CRASH();

    String AdRefStatus___Inactive();

    String AdRefStatus___Active();

    String NetworkBootProtocol___None();

    String NetworkBootProtocol___Dhcp();

    String NetworkBootProtocol___StaticIp();

    String VmEntityType___VM();

    String VmEntityType___TEMPLATE();
    
    String ImageStatus___Unassigned();
    
    String ImageStatus___OK();
    
    String ImageStatus___LOCKED();
    
    String ImageStatus___INVALID();
    
    String ImageStatus___ILLEGAL();

    String QuotaEnforcmentTypeEnum___DISABLED();

    String QuotaEnforcmentTypeEnum___SOFT_ENFORCEMENT();

    String QuotaEnforcmentTypeEnum___HARD_ENFORCEMENT();

    String DiskInterface___IDE();

    String DiskInterface___SCSI();

    String DiskInterface___VirtIO();

    String Snapshot$SnapshotStatus___OK();

    String Snapshot$SnapshotStatus___LOCKED();

    String Snapshot$SnapshotStatus___IN_PREVIEW();

    String Snapshot$SnapshotType___REGULAR();

    String Snapshot$SnapshotType___ACTIVE();

    String Snapshot$SnapshotType___STATELESS();

    String Snapshot$SnapshotType___PREVIEW();
}
