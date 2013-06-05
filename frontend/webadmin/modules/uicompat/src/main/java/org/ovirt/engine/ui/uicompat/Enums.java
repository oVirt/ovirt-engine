package org.ovirt.engine.ui.uicompat;

public interface Enums extends LocalizedEnums {
    String NetworkStatus___NON_OPERATIONAL();

    String NetworkStatus___OPERATIONAL();

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

    String StoragePoolStatus___Maintenance();

    String StoragePoolStatus___Notconfigured();

    String StoragePoolStatus___Uninitialized();

    String StoragePoolStatus___Up();

    String StoragePoolStatus___NotOperational();

    String StoragePoolStatus___Problematic();

    String StoragePoolStatus___Contend();

    String StorageType___ALL();

    String StorageType___FCP();

    String StorageType___ISCSI();

    String StorageType___LOCALFS();

    String StorageType___NFS();

    String StorageType___POSIXFS();

    String StorageType___GLUSTERFS();

    String StorageType___UNKNOWN();

    String StorageFormatType___V1();

    String StorageFormatType___V2();

    String StorageFormatType___V3();

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

    String VmOsType___Windows8();

    String VmOsType___Windows8x64();

    String VmOsType___Windows2012x64();

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

    String DisplayType___qxl();

    String DisplayType___vnc();

    String OriginType___RHEV();

    String OriginType___VMWARE();

    String OriginType___XEN();

    String OriginType___OVIRT();

    /**
     * This needs to cleaned up. We are leaving it in place to support import of
     * VMs created using previous versions.
     * @deprecated
     */
    @Deprecated
    String VmInterfaceType___rtl8139_pv();

    String VmInterfaceType___rtl8139();

    String VmInterfaceType___e1000();

    String VmInterfaceType___pv();

    String VDSType___VDS();

    String VDSType___oVirtNode();

    String StorageDomainType___Master();

    String StorageDomainType___Data();

    String StorageDomainType___ISO();

    String StorageDomainType___ImportExport();

    String VmTemplateStatus___OK();

    String VmTemplateStatus___Locked();

    String VmTemplateStatus___Illegal();

    String UsbPolicy___ENABLED_LEGACY();

    String UsbPolicy___ENABLED_NATIVE();

    String UsbPolicy___DISABLED();

    String VolumeType___Preallocated();

    String VolumeType___Sparse();

    String VolumeType___Unassigned();

    String VmPoolType___Automatic();

    String VmPoolType___Manual();

    String VmPoolType___TimeLease();

    String AdRefStatus___Inactive();

    String AdRefStatus___Active();

    String NetworkBootProtocol___NONE();

    String NetworkBootProtocol___DHCP();

    String NetworkBootProtocol___STATIC_IP();

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

    String Disk$DiskStorageType___LUN();

    String Disk$DiskStorageType___IMAGE();

    String RoleType___ADMIN();

    String RoleType___USER();

    String JobExecutionStatus___STARTED();

    String JobExecutionStatus___FINISHED();

    String JobExecutionStatus___FAILED();

    String JobExecutionStatus___ABORTED();

    String JobExecutionStatus___UNKNOWN();

    String ProviderType___FOREMAN();

    String ProviderType___OPENSTACK_NETWORK();

    String OpenstackNetworkPluginType___LINUX_BRIDGE();

    String OpenstackNetworkPluginType___OPEN_VSWITCH();

    // Gluster enums
    String GlusterVolumeType___DISTRIBUTE();

    String GlusterVolumeType___REPLICATE();

    String GlusterVolumeType___DISTRIBUTED_REPLICATE();

    String GlusterVolumeType___STRIPE();

    String GlusterVolumeType___DISTRIBUTED_STRIPE();

    String GlusterVolumeType___STRIPED_REPLICATE();

    String GlusterVolumeType___DISTRIBUTED_STRIPED_REPLICATE();

    String GlusterStatus___UP();

    String GlusterStatus___DOWN();

    String TransportType___TCP();

    String TransportType___RDMA();

    String ServiceType___NFS();

    String ServiceType___SHD();

    String ServiceType___GLUSTER_SWIFT();
}
