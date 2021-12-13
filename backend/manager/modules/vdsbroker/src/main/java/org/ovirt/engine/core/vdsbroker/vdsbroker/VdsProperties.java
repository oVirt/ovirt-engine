package org.ovirt.engine.core.vdsbroker.vdsbroker;

@SuppressWarnings("WeakerAccess")
public final class VdsProperties {
    // vds configuration (i.e. VdsStatic)
    // vds runtime (i.e. VdsDynamic req getVdsCapabilities)
    public static final String hostDatetime = "dateTime";
    public static final String utc_diff = "timeOffset";
    public static final String cpu_flags = "cpuFlags";

    public static final String SpiceSecureChannels = "spiceSecureChannels";
    public static final String cpuThreads = "cpuThreads";
    public static final String hwManufacturer = "systemManufacturer";
    public static final String hwProductName = "systemProductName";
    public static final String hwVersion = "systemVersion";
    public static final String hwSerialNumber = "systemSerialNumber";
    public static final String hwUUID = "systemUUID";
    public static final String hwFamily = "systemFamily";
    public static final String cpu_cores = "cpuCores";
    public static final String cpu_sockets = "cpuSockets";
    public static final String cpu_model = "cpuModel";
    public static final String online_cpus = "onlineCpus";
    public static final String cpu_speed_mh = "cpuSpeed";
    public static final String kvm_enabled = "kvmEnabled";
    public static final String physical_mem_mb = "memSize";
    public static final String kernel_args = "kernelArgs";
    public static final String Protocol = "protocol";
    public static final String reservedMem = "reservedMem";
    public static final String bootTime = "bootTime";
    public static final String KDUMP_STATUS = "kdumpStatus";
    public static final String selinux = "selinux";
    public static final String selinux_mode = "mode";
    public static final String numOfIoThreads = "numOfIoThreads";
    public static final String pinToIoThread = "pinToIoThread";
    public static final String ioThreadId = "ioThreadId";
    public static final String hosted_engine_configured = "hostedEngineDeployed";
    public static final String vnc_encryption_enabled = "vncEncrypted";

    // vds runtime (i.e. VdsDynamic req getVdsStats)
    public static final String netConfigDirty = "netConfigDirty";
    public static final String status = "status"; // in vm also
    public static final String notify_time = "notify_time";
    public static final String cpu_idle = "cpuIdle";
    public static final String cpu_load = "cpuLoad";
    public static final String cpu_sys = "cpuSys"; // in vm also
    public static final String cpu_user = "cpuUser"; // in vm also
    public static final String elapsed_time = "elapsedTime"; // in vm also
    public static final String statusTime = "statusTime";
    public static final String guestOverhead = "guestOverhead";
    public static final String rx_dropped = "rxDropped"; // in vm also
    public static final String rx_total = "rx"; // in vm also
    public static final String tx_dropped = "txDropped"; // in vm also
    public static final String tx_total = "tx"; // in vm also
    public static final String iface_status = "state";
    public static final String sample_time = "sampleTime"; // in vm also
    public static final String vm_active = "vmActive";
    public static final String vm_count = "vmCount";
    public static final String vm_migrating = "vmMigrating";
    public static final String vm_migration_progress = "progress";
    public static final String INCOMING_VM_MIGRATIONS = "incomingVmMigrations";
    public static final String OUTGOING_VM_MIGRATIONS = "outgoingVmMigrations";
    public static final String images_last_check = "imagesLastCheck";
    public static final String images_last_delay = "imagesLastDelay";
    public static final String ha_score = "haScore";
    public static final String ha_stats = "haStats";
    public static final String ha_stats_score = "score";
    public static final String ha_stats_is_configured = "configured";
    public static final String ha_stats_is_active = "active";
    public static final String ha_stats_global_maintenance = "globalMaintenance";
    public static final String ha_stats_local_maintenance = "localMaintenance";
    public static final String SERIAL_NUMBER = "serial";
    public static final String kernelFeatures = "kernelFeatures";

    public static final String INTERFACE = "iface";
    public static final String DISCARD = "discard";

    // NUMA related properties
    public static final String NUMA_NODES = "numaNodes";
    public static final String NUMA_NODE_DISTANCE = "numaNodeDistance";
    public static final String AUTO_NUMA = "autoNumaBalancing";
    public static final String NUMA_NODE_CPU_LIST = "cpus";
    public static final String NUMA_NODE_TOTAL_MEM = "totalMemory";
    public static final String NUMA_NODE_FREE_MEM_STAT = "numaNodeMemFree";
    public static final String NUMA_NODE_HUGEPAGES = "hugepages";
    public static final String NUMA_NODE_HUGEPAGES_FREE = "freePages";
    public static final String CPU_STATS = "cpuStatistics";
    public static final String NUMA_NODE_FREE_MEM = "memFree";
    public static final String NUMA_NODE_MEM_PERCENT = "memPercent";
    public static final String NUMA_NODE_INDEX = "nodeIndex";
    public static final String NUMA_CPU_SYS = "cpuSys";
    public static final String NUMA_CPU_USER = "cpuUser";
    public static final String NUMA_CPU_IDLE = "cpuIdle";
    public static final String NUMA_TUNE_MODE = "mode";
    public static final String NUMA_TUNE_NODESET = "nodeset";
    public static final String NUMA_TUNE_MEMNODES = "memnodes";
    public static final String NUMA_TUNE_VM_NODE_INDEX = "vmNodeIndex";
    public static final String VM_NUMA_NODE_MEM = "memory";
    public static final String NUMA_TUNE = "numaTune";
    public static final String VM_NUMA_NODES = "guestNumaNodes";
    public static final String VM_NUMA_NODES_RUNTIME_INFO = "vNodeRuntimeInfo";

    // Network related properties
    public static final String NETWORK = "network";
    public static final String LINK_ACTIVE = "linkActive";
    public static final String STP = "STP";
    public static final String MTU = "mtu";
    public static final String VLAN_ID = "vlanid";
    public static final String BASE_INTERFACE = "iface";
    public static final String CONNECTIVITY_CHECK = "connectivityCheck";
    public static final String CONNECTIVITY_TIMEOUT = "connectivityTimeout";
    public static final String COMMIT_ON_SUCCESS = "commitOnSuccess";
    public static final String GLOBAL_GATEWAY = "gateway";
    public static final String IPV6_GLOBAL_GATEWAY = "ipv6gateway";
    public static final String IPV4_DEFAULT_ROUTE = "ipv4defaultroute";
    public static final String DISPLAY_NETWORK = "displayNetwork";
    public static final String VM_NETWORK = "network";
    public static final String VM_INTERFACE_NAME = "name";
    public static final String INTERFACE_SPEED = "speed";
    public static final String VM_NETWORK_INTERFACE = "nic";
    public static final String NETWORK_NICS = "nics";
    public static final String NETWORK_VLANS = "vlans";
    public static final String NETWORKS = "networks";
    public static final String NETWORK_BONDINGS = "bondings";
    public static final String NETWORK_BRIDGES = "bridges";
    public static final String VM_NETWORK_INTERFACES = "netIfaces";
    public static final String VM_IPV4_ADDRESSES = "inet";
    public static final String VM_IPV6_ADDRESSES = "inet6";
    public static final String VM_INTERFACE_MAC_ADDRESS = "hw";
    public static final String NIC_TYPE = "nicModel";
    public static final String PORT_MIRRORING = "portMirroring";
    public static final String BRIDGE = "bridge";
    public static final String NW_FILTER = "filter";
    public static final String NETWORK_FILTER_PARAMETERS = "filterParameters";
    public static final String MAC_ADDR = "macAddr";
    public static final String NETWORK_CUSTOM_PROPERTIES = "custom";
    public static final String BOND_XMIT_POLICY = "xmit_hash_policy";
    public static final String NETMASK = "netmask";
    public static final String ADDR = "addr";
    public static final String OPENVSWITCH = "openvswitch";
    public static final String NMSTATE = "nmstate";
    public static final String OVN_CONFIGURED = "ovnConfigured";

    // LLDP related properties
    public static final String LLDP_ENABLED = "enabled";
    public static final String LLDP_TLVS = "tlvs";
    public static final String TLV_NAME = "name";
    public static final String TLV_TYPE = "type";
    public static final String TLV_OUI = "oui";
    public static final String TLV_SUBTYPE = "subtype";
    public static final String TLV_PROPERTIES = "properties";

    public static final String supported_cluster_levels = "clusterLevels";
    public static final String domain_versions = "domain_versions";
    public static final String name_servers = "nameservers";
    public static final String supported_block_size = "supported_block_size";
    public static final String supported_engines = "supportedENGINEs";
    public static final String emulatedMachine = "emulatedMachine";
    public static final String emulatedMachines = "emulatedMachines";
    public static final String rngSources = "rngSources";
    public static final String host_os = "operatingSystem";
    public static final String packages = "packages";
    public static final String packages2 = "packages2";
    public static final String package_name = "name";
    public static final String package_version = "version";
    public static final String package_release = "release";
    public static final String pretty_name = "pretty_name";
    public static final String version_name = "version_name";
    public static final String build_name = "build_name";
    public static final String software_version = "software_version";
    public static final String kvmPackageName = "kvm";
    public static final String libvirtPackageName = "libvirt";
    public static final String spicePackageName = "qspice-libs";
    public static final String kernelPackageName = "kernel";
    public static final String iSCSIInitiatorName = "ISCSIInitiatorName";
    public static final String HBAInventory = "HBAInventory";
    public static final String qemuKvmPackageName = "qemu-kvm";
    public static final String vdsmPackageName = "vdsm";
    public static final String spiceServerPackageName = "spice-server";
    public static final String librbdPackageName = "librbd1";
    public static final String glusterfsCliPackageName = "glusterfs-cli";
    public static final String GLUSTER_PACKAGE_NAME = "glusterfs";
    public static final String VM_INTERFACE_DEVICE_TYPE = "interface";

    // Addressing related strings
    public static final String Controller = "controller";
    public static final String spapr_vio = "spapr-vio";

    public static final String memFree = "memFree";
    public static final String mem_shared = "memShared";
    public static final String mem_usage = "memUsed";
    // swap
    public static final String swap_free = "swapFree";
    public static final String swap_total = "swapTotal";
    // ksm
    public static final String ksm_cpu_percent = "ksmCpu";
    public static final String ksm_pages = "ksmPages";
    public static final String ksm_state = "ksmState";
    public static final String transparent_huge_pages_state = "thpState";
    public static final String anonymous_transparent_huge_pages = "anonHugePages";
    public static final String transparent_huge_pages = "transparentHugePages";
    public static final String hugepages = "hugepages";
    public static final String free_hugepages = "vm.free_hugepages";
    public static final String total_hugepages = "nr_hugepages";

    public static final String exit_code = "exitCode";
    public static final String exit_message = "exitMessage";
    public static final String monitorResponse = "monitorResponse";
    public static final String exit_reason = "exitReason";

    // Disks usage configuration
    public static final String VM_DISKS_USAGE = "disksUsage";

    // Disks configuration
    public static final String vm_disks = "disks";
    public static final String vm_disk_read_rate = "readRate";
    public static final String vm_disk_read_ops = "readOps";
    public static final String vm_disk_write_rate = "writeRate";
    public static final String vm_disk_write_ops = "writeOps";
    public static final String vm_disk_read_latency = "readLatency";
    public static final String vm_disk_write_latency = "writeLatency";
    public static final String vm_disk_flush_latency = "flushLatency";
    public static final String disk_true_size = "truesize";
    public static final String disk_apparent_size = "apparentsize";
    public static final String image_group_id = "imageID";
    public static final String size = "size";
    public static final String lun_guid = "lunGUID";
    public static final String compat_version = "qcow2_compat";
    public static final String drive_spec = "drive_spec";

    // Iso/Floppy related properties
    public static final String iso_list = "isolist";
    public static final String file_stats = "fileStats";

    // Video device properties
    public static final String VIDEO_HEADS = "heads";
    public static final String VIDEO_RAM = "ram";
    public static final String VIDEO_VRAM = "vram";
    public static final String VIDEO_VGAMEM = "vgamem";
    public static final String VIDEO_DEVICE = "video";
    public static final String GRAPHICS_DEVICE = "graphics";

    // vm configuration (i.e. VmStatic)
    public static final String mem_size_mb = "memSize";
    public static final String maxMemSize = "maxMemSize";
    public static final String maxMemSlots = "maxMemSlots";
    public static final String mem_guaranteed_size_mb = "memGuaranteedSize";
    public static final String num_of_cpus = "smp";
    public static final String cores_per_socket = "smpCoresPerSocket";
    public static final String threads_per_core = "smpThreadsPerCore";
    public static final String max_number_of_cpus = "maxVCpus";
    public static final String cpuPinning = "cpuPinning";
    public static final String vm_name = "vmName";
    public static final String vm_guid = "vmId";
    public static final String smartcardEnabled = "smartcardEnable";
    public static final String vm_arch = "arch";
    public static final String VmLease = "lease";
    public static final String VmLeaseId = "lease_id";
    public static final String VmLeaseSdId = "sd_id";
    public static final String VmLeasePath = "path";
    public static final String VmLeaseOffset = "offset";
    // vm configuration (i.e. VmDynamic)
    public static final String guest_cur_user_name = "username";
    public static final String VM_FQDN = "guestFQDN";
    public static final String vm_guest_mem_buffered = "mem_buffers";
    public static final String vm_guest_mem_cached = "mem_cached";
    public static final String vm_guest_mem_free = "mem_free";
    public static final String vm_guest_mem_unused = "mem_unused";
    public static final String vm_guest_mem_stats = "memoryStats";
    public static final String vm_usage_mem_percent = "memUsage";
    public static final String vm_migration_progress_percent = "migrationProgress";
    public static final String vm_host = "guestName";
    public static final String app_list = "appsList";
    public static final String guest_os = "guestOs";
    public static final String display = "display";
    public static final String display_port = "displayPort";
    public static final String display_secure_port = "displaySecurePort";
    public static final String displayType = "displayType";
    public static final String displayIp = "displayIp";
    public static final String vm_type = "vmType";
    public static final String launch_paused_param = "launchPaused";
    public static final String session = "session";

    public static final String vm_balloonInfo = "balloonInfo";
    public static final String vm_balloon_cur = "balloon_cur";
    public static final String vm_balloon_max = "balloon_max";
    public static final String vm_balloon_min = "balloon_min";
    public static final String vm_balloon_target = "balloon_target";

    // guest Containers
    public static final String guest_containers = "guestContainers";
    public static final String guest_container_id = "id";
    public static final String guest_container_names = "names";
    public static final String guest_container_image = "image";
    public static final String guest_container_command = "command";
    public static final String guest_container_status = "status";

    // v2v
    public static final String DISK_ALLOCATION = "allocation";
    public static final String DISK_VIRTUAL_SIZE = "capacity";
    public static final String VIRTIO_ISO_PATH = "virtio_iso_path";
    public static final String DISK_TARGET_DEV_NAME = "dev";

    // reported by guest-agent
    public static final String GUEST_CPU_COUNT = "guestCPUCount";
    public static final String GUEST_OS_INFO = "guestOsInfo";

    public static final String GUEST_OS_INFO_VERSION = "version";
    public static final String GUEST_OS_INFO_DISTRIBUTION = "distribution";
    public static final String GUEST_OS_INFO_CODENAME = "codename";
    public static final String GUEST_OS_INFO_ARCH = "arch";
    public static final String GUEST_OS_INFO_TYPE = "type";
    public static final String GUEST_OS_INFO_KERNEL = "kernel";
    public static final String GUEST_TIMEZONE = "guestTimezone";
    public static final String GUEST_TIMEZONE_OFFSET = "offset";
    public static final String GUEST_TIMEZONE_ZONE = "zone";

    public static final String agentChannelName = "agentChannelName";

    public static final String kvmEnable = "kvmEnable"; // Optional
    public static final String acpiEnable = "acpiEnable"; // Optional
    public static final String BOOT_MENU_ENABLE = "bootMenuEnable";
    public static final String spiceFileTransferEnable = "fileTransferEnable";
    public static final String spiceCopyPasteEnable = "copyPasteEnable";
    public static final String hypervEnable = "hypervEnable";

    public static final String BootOrder = "bootOrder";
    public static final String CDRom = "cdrom"; // Optional
    public static final String Snapshot = "snapshotFile"; // Optional
    public static final String cpuType = "cpuType";
    public static final String niceLevel = "nice";
    public static final String cpuShares = "cpuShares";
    public static final String hiberVolHandle = "hiberVolHandle";
    public static final String memoryDumpVolumeInfo = "memoryDumpVolumeInfo";
    public static final String memoryMetadataVolumeInfo = "memoryMetadataVolumeInfo";
    public static final String engineXml = "xml";
    public static final String nvramData = "_X_nvramdata";
    public static final String nvramHash = "nvramHash";
    public static final String tpmData = "_X_tpmdata";
    public static final String tpmHash = "tpmHash";
    public static final String pauseCode = "pauseCode";
    public static final String KeyboardMap = "keyMap";
    public static final String TabletEnable = "tabletEnable";
    public static final String PitReinjection = "pitReinjection";
    public static final String InitrdUrl = "initrd";
    public static final String KernelUrl = "kernel";
    public static final String KernelParams = "kernelArgs";
    public static final String Custom = "custom";
    public static final String Type = "type";
    public static final String DeviceId = "deviceId";
    public static final String Device = "device";
    public static final String DeviceType = "deviceType";
    public static final String Devices = "devices";
    public static final String GuestDiskMapping = "guestDiskMapping";
    public static final String Name = "name";
    public static final String HostDev = "hostdev";

    public static final String Index = "index";
    public static final String PoolId = "poolID";
    public static final String DomainId = "domainID";
    public static final String ImageId = "imageID";
    public static final String VolumeChain = "volumeChain";
    public static final String VolumeId = "volumeID";
    public static final String Format = "format";
    public static final String Shareable = "shared";
    public static final String None = "none";
    public static final String Transient = "transient";
    public static final String Exclusive = "exclusive";
    public static final String SpecParams = "specParams";
    public static final String Address = "address";
    public static final String Alias = "alias";
    public static final String PropagateErrors = "propagateErrors";
    public static final String Optional = "optional";
    public static final String ReadOnly = "readonly";
    public static final String Virtio = "virtio";
    public static final String VirtioScsi = "virtio-scsi";
    public static final String VirtioSerial = "virtio-serial";
    public static final String Scsi = "scsi";
    public static final String Sgio = "sgio";
    public static final String Unit = "unit";
    public static final String Path = "path";
    public static final String BlockPath = "block_path";
    public static final String Ide = "ide";
    public static final String Fdc = "fdc";
    public static final String Guid = "GUID";
    public static final String Disk = "disk";
    public static final String Tcp = "tcp";
    public static final String DiskType = "diskType";
    public static final String NetworkDiskName = "name";
    public static final String NetworkDiskPort = "port";
    public static final String NetworkDiskTransport = "transport";
    public static final String NetworkDiskHosts = "hosts";
    public static final String NetworkDiskAuth = "auth";
    public static final String NetworkDiskAuthUsername = "username";
    public static final String NetworkDiskAuthSecretType = "type";
    public static final String NetworkDiskAuthSecretUuid = "uuid";
    public static final String CinderAuthEnabled = "auth_enabled";
    public static final String CinderSecretType = "secret_type";
    public static final String CinderAuthUsername = "auth_username";
    public static final String CinderSecretUuid = "secret_uuid";
    public static final String Ovirt = "ovirt";

    // cpu qos
    public static final String vCpuLimit = "vcpuLimit";

    // iotune
    public static final String Iotune = "ioTune";
    public static final String IoPolicyMaximum = "maximum";
    public static final String IoPolicyGuarenteed = "guaranteed";
    public static final String TotalBytesSec = "total_bytes_sec";
    public static final String ReadBytesSec = "read_bytes_sec";
    public static final String WriteBytesSec = "write_bytes_sec";
    public static final String TotalIopsSec = "total_iops_sec";
    public static final String ReadIopsSec = "read_iops_sec";
    public static final String WriteIopsSec = "write_iops_sec";
    // USB controller
    public static final String Model = "model";
    // USB slot
    public static final String Bus = "bus";

    public static final String clientIp = "clientIp";
    public static final String hash = "hash";
    // migration
    public static final String src = "src";
    public static final String dst = "dst";
    public static final String method = "method";
    public static final String domains = "storageDomains";
    public static final String hooks = "hooks";
    public static final String TUNNELED = "tunneled";
    public static final String DST_QEMU = "dstqemu";
    public static final String MIGRATION_DOWNTIME = "downtime";
    public static final String AUTO_CONVERGE = "autoConverge";
    public static final String MIGRATE_COMPRESSED = "compressed";
    public static final String MIGRATE_ENCRYPTED = "encrypted";
    public static final String ADDITIONAL_FEATURES = "additionalFeatures";
    public static final String CONSOLE_ADDRESS = "consoleAddress";
    public static final String MIGRATION_CONVERGENCE_SCHEDULE = "convergenceSchedule";
    public static final String ENABLE_GUEST_EVENTS = "enableGuestEvents";
    public static final String MIGRATION_BANDWIDTH = "maxBandwidth";
    public static final String MIGRATION_INIT_STEPS = "init";
    public static final String MIGRATION_STALLING_STEPS = "stalling";
    public static final String MIGRATION_SOURCE = "Migration Source";
    public static final String MIGRATION_DESTINATION = "Migration Destination";
    public static final String MIGRATION_OUTGOING_LIMIT = "outgoingLimit";
    public static final String MIGRATION_INCOMING_LIMIT = "incomingLimit";

    // multipath health
    public static final String MULTIPATH_HEALTH = "multipathHealth";
    public static final String MULTIPATH_VALID_PATHS = "valid_paths";
    public static final String MULTIPATH_FAILED_PATHS = "failed_paths";

    // storage domains
    public static final String code = "code";
    public static final String lastCheck = "lastCheck";
    public static final String delay = "delay";
    public static final String actual = "actual";
    public static final String acquired = "acquired";

    public static final String DISK_STATS = "diskStats";
    public static final String DISK_STATS_FREE = "free";

    // watchdog
    public static final String watchdogEvent = "watchdogEvent";
    public static final String time = "time";
    public static final String action = "action";

    // Network QoS
    public static final String HOST_QOS = "hostQos";
    public static final String HOST_QOS_OUTBOUND = "out";
    public static final String HOST_QOS_LINKSHARE = "ls";
    public static final String HOST_QOS_UPPERLIMIT = "ul";
    public static final String HOST_QOS_REALTIME = "rt";
    public static final String HOST_QOS_AVERAGE = "m2";
    public static final String QOS_INBOUND = "inbound";
    public static final String QOS_OUTBOUND = "outbound";
    public static final String QOS_AVERAGE = "average";
    public static final String QOS_PEAK = "peak";
    public static final String QOS_BURST = "burst";

    // host devices
    public static final String ROOT_HOST_DEVICE = "computer";
    public static final String DEVICE_LIST = "deviceList";
    public static final String PARAMS = "params";
    public static final String CAPABILITY = "capability";
    public static final String IOMMU_GROUP = "iommu_group";
    public static final String MDEV = "mdev";
    public static final String MDEV_AVAILABLE_INSTANCES = "available_instances";
    public static final String MDEV_DESCRIPTION = "description";
    public static final String MDEV_NAME = "name";
    public static final String PRODUCT_NAME = "product";
    public static final String PRODUCT_ID = "product_id";
    public static final String VENDOR_NAME = "vendor";
    public static final String VENDOR_ID = "vendor_id";
    public static final String PARENT_NAME = "parent";
    public static final String PHYSICAL_FUNCTION = "physfn";
    public static final String DRIVER = "driver";
    public static final String TOTAL_VFS = "totalvfs";
    public static final String NET_INTERFACE_NAME = "interface";
    public static final String HOST_DEVICE_PASSTHROUGH = "hostdevPassthrough";
    public static final String IS_ASSIGNABLE = "is_assignable";
    public static final String DEVICE_PATH = "device_path";
    public static final String NUMA_NODE = "numa_node";
    public static final String MODE = "mode";
    public static final String DEVICE_SIZE = "device_size";
    public static final String ALIGN_SIZE = "align_size";

    // fencing policy parameters
    public static final String STORAGE_DOMAIN_HOST_ID_MAP = "storageDomainHostIdMap";
    public static final String SKIP_FENCING_IF_GLUSTER_BRICKS_ARE_UP = "skipFencingIfGlusterBricksUp";
    public static final String SKIP_FENCING_IF_GLUSTER_QUORUM_NOT_MET = "skipFencingIfGlusterQuorumNotMet";
    public static final String GLUSTER_SERVER_UUID = "glusterServerUuid";

    // legacy display types
    public static final String QXL = "qxl";
    public static final String VNC = "vnc";

    // Display info
    public static final String displayInfo = "displayInfo";
    public static final String type = "type";
    public static final String port = "port";
    public static final String tlsPort = "tlsPort";
    public static final String ipAddress = "ipAddress";

    // Host jobs
    public static final String jobId = "id";
    public static final String jobDescription = "description";
    public static final String jobType = "job_type";
    public static final String jobStatus = "status";
    public static final String jobProgress = "progress";
    public static final String jobError = "error";
    public static final String jobErrorCode = "code";
    public static final String jobErrorMessage = "message";

    // MoM policy tuning
    public static final String balloonEnabled = "balloonEnabled";
    public static final String ksmEnabled = "ksmEnabled";
    public static final String ksmMergeAcrossNodes = "ksmMergeAcrossNodes";

    // properties for ServerConnectionListReturn
    public static final String target = "target";

    // V2V Jobs
    public static final String v2vJobs = "v2vJobs";
    public static final String v2vJobStatus = "status";
    public static final String v2vDescription = "description";
    public static final String v2vProgress = "progress";

    // VM Jobs
    public static final String vmJobs = "vmJobs";
    public static final String vmJobId = "id";
    public static final String vmJobType = "jobType";
    public static final String vmBlockJobType = "blockJobType";
    public static final String vmJobCursorCur = "cur";
    public static final String vmJobCursorEnd = "end";
    public static final String vmJobBandwidth = "bandwidth";
    public static final String vmJobImageUUID = "imgUUID";

    public static final String SWITCH_KEY = "switch";

    // Network provider agent id
    public static final String OPENSTACK_BINDING_HOST_IDS = "openstack_binding_host_ids";

    // cinderlib
    public static final String CONNECTOR_INFO = "connector_info";

    // incremental backup
    public static final String BACKUP_ENABLED = "backupEnabled";
    public static final String COLD_BACKUP_ENABLED = "coldBackupEnabled";
    public static final String CLEAR_BITMAPS_ENABLED = "clearBitmapsEnabled";
    public static final String CHECKPOINT = "checkpoint";
    public static final String CHECKPOINT_IDS = "checkpoint_ids";
    public static final String BACKUP_MODE = "backup_mode";
    public static final String SCRATCH_DISK = "scratch_disk";

    public static final String TSC_FREQUENCY = "tscFrequency";

    public static final String FIPS_MODE = "fipsEnabled";

    public static final String TSC_SCALING = "tscScaling";

    public static final String BOOT_UUID = "boot_uuid";

    public static final String CD_CHANGE_PDIV = "cd_change_pdiv";
}
