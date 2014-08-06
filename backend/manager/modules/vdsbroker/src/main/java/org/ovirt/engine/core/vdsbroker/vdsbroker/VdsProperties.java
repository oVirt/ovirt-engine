package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;

//-----------------------------------------------------
//
//-----------------------------------------------------
//TODO: BrokerFactory, StatusForXmlRpc can be consolidated for all brokers
//      (currently in vdsBroker and irsBroker)
//public static class VdsBrokerFactory
//{
//    //TODO: messed up because could not make real factory here
//    public static IVdsBroker create(string host, uint port)
//    {
//        return new VdsBroker(host, port);
//    }
//}

public final class VdsProperties {
    // vds configuration (i.e. VdsStatic)
    // vds runtime (i.e. VdsDynamic req getVdsCapabilities)
    public static final String hostDatetime = "dateTime";
    public static final String time_zone = "timeZone";
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
    public static final String cpu_speed_mh = "cpuSpeed";
    public static final String if_total_speed = "eth0Speed";
    public static final String kvm_enabled = "kvmEnabled";
    public static final String physical_mem_mb = "memSize";
    public static final String Protocol = "protocol";
    public static final String vm_types = "vmTypes"; // Currently not in use
    public static final String reservedMem = "reservedMem";
    public static final String bootTime = "bootTime";
    public static final String KDUMP_STATUS = "kdumpStatus";
    public static final String selinux = "selinux";
    public static final String selinux_mode = "mode";

    // vds runtime (i.e. VdsDynamic req getVdsStats)
    public static final String netConfigDirty = "netConfigDirty";
    public static final String status = "status"; // in vm also
    public static final String cpu_idle = "cpuIdle";
    public static final String cpu_load = "cpuLoad";
    public static final String cpu_sys = "cpuSys"; // in vm also
    public static final String cpu_user = "cpuUser"; // in vm also
    public static final String destroy_rate = "destroyRate";
    public static final String destroy_total = "destroyTotal";
    public static final String elapsed_time = "elapsedTime"; // in vm also
    public static final String launch_rate = "launchRate";
    public static final String launch_total = "launchTotal";
    public static final String vds_usage_mem_percent = "memUsed";
    public static final String rx_dropped = "rxDropped"; // in vm also
    public static final String guestOverhead = "guestOverhead";
    public static final String rx_rate = "rxRate"; // in vm also
    public static final String tx_dropped = "txDropped"; // in vm also
    public static final String tx_rate = "txRate"; // in vm also
    public static final String iface_status = "state";
    public static final String vm_active = "vmActive";
    public static final String vm_count = "vmCount";
    public static final String vm_migrating = "vmMigrating";
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

    public static final String INTERFACE = "iface";

    // NUMA related properties
    public static final String NUMA_NODES = "numaNodes";
    public static final String NUMA_NODE_DISTANCE = "numaNodeDistance";
    public static final String AUTO_NUMA = "autoNumaBalancing";
    public static final String NUMA_NODE_CPU_LIST = "cpus";
    public static final String NUMA_NODE_TOTAL_MEM = "totalMemory";
    public static final String NUMA_NODE_FREE_MEM_STAT = "numaNodeMemFree";
    public static final String CPU_STATS = "cpuStatistics";
    public static final String NUMA_NODE_FREE_MEM = "memFree";
    public static final String NUMA_NODE_MEM_PERCENT = "memPercent";
    public static final String NUMA_NODE_INDEX = "nodeIndex";
    public static final String NUMA_CPU_SYS = "cpuSys";
    public static final String NUMA_CPU_USER = "cpuUser";
    public static final String NUMA_CPU_IDLE = "cpuIdle";
    public static final String NUMA_TUNE_MODE = "mode";
    public static final String NUMA_TUNE_NODESET = "nodeset";
    public static final String VM_NUMA_NODE_MEM = "memory";
    public static final String NUMA_TUNE = "numaTune";
    public static final String VM_NUMA_NODES = "guestNumaNodes";

    // Network related properties
    public static final String NETWORK = "network";
    public static final String LINK_ACTIVE = "linkActive";
    public static final String BOOT_PROTOCOL = "BOOTPROTO";
    public static final String STP = "STP";
    public static final String MTU = "mtu";
    public static final String VLAN_ID = "vlanid";
    public static final String BASE_INTERFACE = "iface";
    public static final String BONDING_OPTIONS = "BONDING_OPTS";
    public static final String DHCP = "dhcp";
    public static final String FORCE = "force";
    public static final String CONNECTIVITY_CHECK = "connectivityCheck";
    public static final String CONNECTIVITY_TIMEOUT = "connectivityTimeout";
    public static final String IP_ADDRESS = "IPADDR";
    public static final String NETMASK = "NETMASK";
    public static final String GATEWAY = "GATEWAY";
    public static final String GLOBAL_GATEWAY = "gateway";
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
    public static final String NETWORK_LAST_CLIENT_INTERFACE = "lastClientIface";
    public static final String VM_NETWORK_INTERFACES = "netIfaces";
    public static final String VM_IPV4_ADDRESSES = "inet";
    public static final String VM_IPV6_ADDRESSES = "inet6";
    public static final String VM_INTERFACE_MAC_ADDRESS = "hw";
    public static final String NIC_TYPE = "nicModel";
    public static final String PORT_MIRRORING = "portMirroring";
    public static final String BRIDGE = "bridge";
    public static final String NW_FILTER = "filter";
    public static final String MAC_ADDR = "macAddr";
    public static final String NETWORK_CUSTOM_PROPERTIES = "custom";

    public static final String supported_cluster_levels = "clusterLevels";
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
    public static final String GLUSTER_PACKAGE_NAME = "glusterfs";

    // Addressing related strings
    public static final String Controller = "controller";
    public static final String Drive = "drive";
    public static final String spapr_vio = "spapr-vio";

    public static final String mem_available = "memAvailable";
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

    public static final String exit_code = "exitCode";
    public static final String exit_message = "exitMessage";
    public static final String multimedia_ports = "multimediaPorts";
    public static final String monitorResponse = "monitorResponse";
    public static final String exit_reason = "exitReason";

    // Disks usage configuration
    public static final String VM_DISKS_USAGE = "disksUsage";

    // Disks configuration
    public static final String vm_disks = "disks";
    public static final String vm_disk_name = "name";
    public static final String vm_disk_read_rate = "readRate";
    public static final String vm_disk_write_rate = "writeRate";
    public static final String vm_disk_read_latency = "readLatency";
    public static final String vm_disk_write_latency = "writeLatency";
    public static final String vm_disk_flush_latency = "flushLatency";
    public static final String disk_actual_size = "actualsize";
    public static final String disk_true_size = "truesize";
    public static final String image_group_id = "imageID";
    public static final String size = "size";
    public static final String lun_guid = "lunGUID";

    // Iso/Floppy related properties
    public static final String iso_list = "isolist";
    public static final String file_stats = "fileStats";

    // vm configuration (i.e. VmStatic)
    public static final String mem_size_mb = "memSize";
    public static final String mem_guaranteed_size_mb = "memGuaranteedSize";
    public static final String num_of_monitors = "spiceMonitors";
    public static final String num_of_cpus = "smp";
    public static final String cores_per_socket = "smpCoresPerSocket";
    public static final String max_number_of_cpus = "maxVCpus";
    public static final String cpuPinning = "cpuPinning";
    public static final String vm_name = "vmName";
    public static final String vm_guid = "vmId";
    public static final String smartcardEnabled = "smartcardEnable";
    // vm configuration (i.e. VmDynamic)
    public static final String guest_cur_user_name = "username";
    public static final String VM_IP = "guestIPs";
    public static final String VM_FQDN = "guestFQDN";
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
    public static final String vm_pid = "pid";
    public static final String vm_type = "vmType";
    public static final String guest_last_login_time = "lastLogin";
    public static final String guest_last_logout_time = "lastLogout";
    public static final String launch_paused_param = "launchPaused";
    public static final String session = "session";
    public static final String spiceSslCipherSuite = "spiceSslCipherSuite";
    public static final String liveSnapshotSupport = "liveSnapshot";

    public static final String vm_balloonInfo = "balloonInfo";
    public static final String vm_balloon_cur = "balloon_cur";
    public static final String vm_balloon_max = "balloon_max";
    public static final String vm_balloon_min = "balloon_min";
    public static final String vm_balloon_target = "balloon_target";

    // reported by guest-agent
    public static final String GUEST_CPU_COUNT = "guestCPUCount";

    public static final String DriveC = "hda"; // drive C:
    public static final String DriveE = "hdb"; // drive E: (D: is the CD-ROM)
    public static final String DriveF = "hdc"; // drive F:
    public static final String DriveG = "hdd"; // drive G:

    public static final String kvmEnable = "kvmEnable"; // Optional
    public static final String acpiEnable = "acpiEnable"; // Optional
    public static final String BOOT_MENU_ENABLE = "bootMenuEnable";
    public static final String win2kHackEnable = "win2kHackEnable"; // Optional
    public static final String initFromFloppy = "initFromFloppy"; // Optional
    public static final String sysprepInf = "sysprepInf"; // for the binary sys
    public static final String spiceFileTransferEnable = "fileTransferEnable";
    public static final String spiceCopyPasteEnable = "copyPasteEnable";
    public static final String hypervEnable = "hypervEnable";
                                                          // prep
    public static final String Boot = "boot"; // Optional
    public static final String BootOrder = "bootOrder";
    public static final String CDRom = "cdrom"; // Optional
    public static final String Floppy = "floppy"; // Optional
    public static final String Snapshot = "snapshotFile"; // Optional
    public static final String soundDevice = "soundDevice";
    public static final String virtioConsole = "console";
    public static final String cpuType = "cpuType";
    public static final String niceLevel = "nice";
    public static final String cpuShares = "cpuShares";
    public static final String hiberVolHandle = "hiberVolHandle";
    public static final String pauseCode = "pauseCode";
    public static final String KeyboardLayout = "keyboardLayout";
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

    public static final String Index = "index";
    public static final String PoolId = "poolID";
    public static final String DomainId = "domainID";
    public static final String ImageId = "imageID";
    public static final String VolumeId = "volumeID";
    public static final String Format = "format";
    public static final String Shareable = "shared";
    public static final String None = "none";
    public static final String Exclusive = "exclusive";
    public static final String Shared = "shared";
    public static final String Transient = "transient";
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
    public static final String Ide = "ide";
    public static final String Fdc = "fdc";
    public static final String Guid = "GUID";
    public static final String Disk = "disk";
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
    public static final String offline = "offline";
    public static final String online = "online";
    public static final String domains = "storageDomains";
    public static final String hooks = "hooks";
    public static final String TUNNELED = "tunneled";
    public static final String DST_QEMU = "dstqemu";
    public static final String MIGRATION_DOWNTIME = "downtime";

    // storage domains
    public static final String code = "code";
    public static final String lastCheck = "lastCheck";
    public static final String delay = "delay";

    public static final String DISK_STATS = "diskStats";
    public static final String DISK_STATS_FREE = "free";

    // watchdog
    public static final String watchdogEvent = "watchdogEvent";
    public static final String time = "time";
    public static final String action = "action";

    // Network QoS
    public static final String HOST_QOS_INBOUND = "qosInbound";
    public static final String HOST_QOS_OUTBOUND = "qosOutbound";
    public static final String QOS_INBOUND = "inbound";
    public static final String QOS_OUTBOUND = "outbound";
    public static final String QOS_AVERAGE = "average";
    public static final String QOS_PEAK = "peak";
    public static final String QOS_BURST = "burst";

    public static String migrationMethodtoString(MigrationMethod method) {
        switch (method) {
        case OFFLINE:
            return offline;

        case ONLINE:
            return online;

        default:
            return "";
        }
    }

    // MoM policy tuning
    public static final String balloonEnabled = "balloonEnabled";
    public static final String ksmEnabled = "ksmEnabled";

    // properties for ServerConnectionListReturnForXmlRpc
    public static final String serverType = "serverType";
    public static final String target = "target";

    // VM Jobs
    public static final String vmJobs = "vmJobs";
    public static final String vmJobId = "id";
    public static final String vmJobType = "jobType";
    public static final String vmBlockJobType = "blockJobType";
    public static final String vmJobCursorCur = "cur";
    public static final String vmJobCursorEnd = "end";
    public static final String vmJobBandwidth = "bandwidth";
    public static final String vmJobImageUUID = "imgUUID";

}
