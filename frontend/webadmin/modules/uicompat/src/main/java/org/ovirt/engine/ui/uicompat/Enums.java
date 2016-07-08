package org.ovirt.engine.ui.uicompat;


public interface Enums extends LocalizedEnums {
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

    String StorageType___GLUSTERFS();

    String StorageType___GLANCE();

    String StorageType___CINDER();

    String StorageType___UNKNOWN();

    String NfsVersion___V3();

    String NfsVersion___V4();

    String NfsVersion___V4_1();

    String NfsVersion___AUTO();

    String StorageFormatType___V1();

    String StorageFormatType___V2();

    String StorageFormatType___V3();

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

    String OriginType___MANAGED_HOSTED_ENGINE();

    String OriginType___KVM();

    @Deprecated
    String VmInterfaceType___rtl8139_pv();

    String VmInterfaceType___rtl8139();

    String VmInterfaceType___e1000();

    String VmInterfaceType___pv();

    String VmInterfaceType___spaprVlan();

    String VmInterfaceType___pciPassthrough();

    String VDSType___VDS();

    String VDSType___oVirtNode();

    String StorageDomainType___Master();

    String StorageDomainType___Data();

    String StorageDomainType___ISO();

    String StorageDomainType___ImportExport();

    String StorageDomainType___Image();

    String StorageDomainType___Volume();

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

    String ProviderType___OPENSTACK_NETWORK();

    String ProviderType___EXTERNAL_NETWORK();

    String OpenstackNetworkPluginType___LINUX_BRIDGE();

    String OpenstackNetworkPluginType___OPEN_VSWITCH();

    String ProviderType___OPENSTACK_IMAGE();

    String ProviderType___OPENSTACK_VOLUME();

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
}

