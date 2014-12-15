package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.NumaUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulate the knowledge of how to create objects from the VDS RPC protocol response.
 * This class has methods that receive XmlRpcStruct and construct the following Classes: VmDynamic VdsDynamic VdsStatic.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VdsBrokerObjectsBuilder {

    private static final Logger log = LoggerFactory.getLogger(VdsBrokerObjectsBuilder.class);

    private final static int VNC_START_PORT = 5900;
    private final static double NANO_SECONDS = 1000000000;

    private static final Comparator<VdsNumaNode> numaNodeComparator = new Comparator<VdsNumaNode>() {

        @Override
        public int compare(VdsNumaNode arg0, VdsNumaNode arg1) {
            return arg0.getIndex() < arg1.getIndex() ? -1 : 1;
        }

    };

    public static VmDynamic buildVMDynamicDataFromList(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmdynamic = new VmDynamic();
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            vmdynamic.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.status)) {
            vmdynamic.setStatus(convertToVmStatus((String) xmlRpcStruct.get(VdsProperties.status)));
        }
        return vmdynamic;
    }

    public static VmDynamic buildVMDynamicData(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmdynamic = new VmDynamic();
        updateVMDynamicData(vmdynamic, xmlRpcStruct);
        return vmdynamic;
    }

    public static StoragePool buildStoragePool(Map<String, Object> xmlRpcStruct) {
        StoragePool sPool = new StoragePool();
        if (xmlRpcStruct.containsKey("type")) {
            sPool.setIsLocal(StorageType.valueOf(xmlRpcStruct.get("type").toString()).isLocal());
        }
        sPool.setName(AssignStringValue(xmlRpcStruct, "name"));
        Integer masterVersion = AssignIntValue(xmlRpcStruct, "master_ver");
        if (masterVersion != null) {
            sPool.setmaster_domain_version(masterVersion);
        }
        return sPool;
    }

    public static VmStatistics buildVMStatisticsData(Map<String, Object> xmlRpcStruct) {
        VmStatistics vmStatistics = new VmStatistics();
        updateVMStatisticsData(vmStatistics, xmlRpcStruct);
        return vmStatistics;
    }

    public static Map<String, LUNs> buildVmLunDisksData(Map<String, Object> xmlRpcStruct) {
        Map<String, Object> disks = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.vm_disks);
        Map<String, LUNs> lunsMap = new HashMap<>();

        if (disks != null) {
            for (Object diskAsObj : disks.values()) {
                Map<String, Object> disk = (Map<String, Object>) diskAsObj;

                String lunGuidString = AssignStringValue(disk, VdsProperties.lun_guid);
                if (!StringUtils.isEmpty(lunGuidString)) {
                    LUNs lun = new LUNs();
                    lun.setLUN_id(lunGuidString);

                    if (disk.containsKey(VdsProperties.disk_true_size)) {
                        long sizeInBytes = AssignLongValue(disk, VdsProperties.disk_true_size);
                        int sizeInGB = SizeConverter.convert(
                                sizeInBytes, SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GB).intValue();
                        lun.setDeviceSize(sizeInGB);
                    }

                    lunsMap.put(lunGuidString, lun);
                }
            }
        }

        return lunsMap;
    }

    public static void updateVMDynamicData(VmDynamic vm, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            vm.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.session)) {
            String session = (String) xmlRpcStruct.get(VdsProperties.session);
            try {
                vm.setSession(SessionState.valueOf(session));
            } catch (Exception e) {
                log.error("Illegal vm session '{}'.", session);
            }
        }
        if (xmlRpcStruct.containsKey(VdsProperties.kvmEnable)) {
            vm.setKvmEnable(Boolean.parseBoolean((String) xmlRpcStruct.get(VdsProperties.kvmEnable)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.acpiEnable)) {
            vm.setAcpiEnable(Boolean.parseBoolean((String) xmlRpcStruct.get(VdsProperties.acpiEnable)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.win2kHackEnable)) {
            vm.setWin2kHackEnable(Boolean.parseBoolean((String) xmlRpcStruct.get(VdsProperties.win2kHackEnable)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.status)) {
            vm.setStatus(convertToVmStatus((String) xmlRpcStruct.get(VdsProperties.status)));
        }

        boolean hasGraphicsInfo = updateGraphicsInfo(vm, xmlRpcStruct);
        if (!hasGraphicsInfo) {
            updateGraphicsInfoFromConf(vm, xmlRpcStruct);
        }

        if (xmlRpcStruct.containsKey((VdsProperties.utc_diff))) {
            String utc_diff = xmlRpcStruct.get(VdsProperties.utc_diff).toString();
            if (utc_diff.startsWith("+")) {
                utc_diff = utc_diff.substring(1);
            }
            try {
                vm.setUtcDiff(Integer.parseInt(utc_diff));
            } catch (NumberFormatException e) {
                log.error("Illegal vm offset (utc_diff) '{}'.", utc_diff);
            }
        }

        if (xmlRpcStruct.containsKey(VdsProperties.hash)) {
            String hash = (String) xmlRpcStruct.get(VdsProperties.hash);
            try {
                vm.setHash(hash);
            } catch (Exception e) {
                log.error("Illegal vm hash '{}'.", hash);
            }
        }

        /**
         * vm disks
         */
        if (xmlRpcStruct.containsKey(VdsProperties.vm_disks)) {
            initDisks(xmlRpcStruct, vm);
        }

        // ------------- vm internal agent data
        vm.setGuestLastLoginTime(AssignDateTImeFromEpoch(xmlRpcStruct, VdsProperties.guest_last_login_time));
        vm.setVmHost(AssignStringValue(xmlRpcStruct, VdsProperties.vm_host));

        String guestUserName = AssignStringValue(xmlRpcStruct, VdsProperties.guest_cur_user_name);
        vm.setGuestCurrentUserName(guestUserName);

        initAppsList(xmlRpcStruct, vm);
        vm.setGuestOs(AssignStringValue(xmlRpcStruct, VdsProperties.guest_os));
        if (xmlRpcStruct.containsKey(VdsProperties.VM_FQDN)) {
            vm.setVmFQDN(AssignStringValue(xmlRpcStruct, VdsProperties.VM_FQDN));
            String fqdn = vm.getVmFQDN().trim();
            if ("localhost".equalsIgnoreCase(fqdn) || "localhost.localdomain".equalsIgnoreCase(fqdn)) {
                vm.setVmFQDN(null);
            }
            else {
                vm.setVmFQDN(fqdn);
            }
        }

        vm.setVmIp(AssignStringValue(xmlRpcStruct, VdsProperties.VM_IP));
        if (vm.getVmIp() != null) {
            if (vm.getVmIp().startsWith("127.0.")) {
                vm.setVmIp(null);
            } else {
                vm.setVmIp(vm.getVmIp().trim());
            }
        }

        if (xmlRpcStruct.containsKey(VdsProperties.exit_code)) {
            String exitCodeStr = xmlRpcStruct.get(VdsProperties.exit_code).toString();
            vm.setExitStatus(VmExitStatus.forValue(Integer.parseInt(exitCodeStr)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.exit_message)) {
            String exitMsg = (String) xmlRpcStruct.get(VdsProperties.exit_message);
            vm.setExitMessage(exitMsg);
        }
        if (xmlRpcStruct.containsKey(VdsProperties.exit_reason)) {
            String exitReasonStr = xmlRpcStruct.get(VdsProperties.exit_reason).toString();
            vm.setExitReason(VmExitReason.forValue(Integer.parseInt(exitReasonStr)));
        } else {
            vm.setExitReason(VmExitReason.Unknown);
        }

        // if monitorResponse returns negative it means its erroneous
        if (xmlRpcStruct.containsKey(VdsProperties.monitorResponse)) {
            int response = Integer.parseInt(xmlRpcStruct.get(VdsProperties.monitorResponse).toString());
            if (response < 0) {
                vm.setStatus(VMStatus.NotResponding);
            }
        }
        if (xmlRpcStruct.containsKey(VdsProperties.clientIp)) {
            vm.setClientIp(xmlRpcStruct.get(VdsProperties.clientIp).toString());
        }

        VmPauseStatus pauseStatus = VmPauseStatus.NONE;
        if (xmlRpcStruct.containsKey(VdsProperties.pauseCode)) {
            String pauseCodeStr = (String) xmlRpcStruct.get(VdsProperties.pauseCode);
            try {
                pauseStatus = VmPauseStatus.valueOf(pauseCodeStr);

            } catch (IllegalArgumentException ex) {
                log.error("Error in parsing vm pause status. Setting value to NONE");
                pauseStatus = VmPauseStatus.NONE;
            }
        }
        vm.setPauseStatus(pauseStatus);

        if (xmlRpcStruct.containsKey(VdsProperties.watchdogEvent)) {
            Map<String, Object> watchdogStruct = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.watchdogEvent);
            double time = Double.parseDouble(watchdogStruct.get(VdsProperties.time).toString());
            // vdsm may not send the action http://gerrit.ovirt.org/14134
            String action =
                    watchdogStruct.containsKey(VdsProperties.action) ? watchdogStruct.get(VdsProperties.action)
                            .toString() : null;
            vm.setLastWatchdogEvent((long) time);
            vm.setLastWatchdogAction(action);
        }

        if (xmlRpcStruct.containsKey(VdsProperties.CDRom)) {
            String isoName = Paths.get((String) xmlRpcStruct.get(VdsProperties.CDRom)).getFileName().toString();
            vm.setCurrentCd(isoName);
        }

        if (xmlRpcStruct.containsKey(VdsProperties.GUEST_CPU_COUNT)) {
            vm.setGuestCpuCount(AssignIntValue(xmlRpcStruct, VdsProperties.GUEST_CPU_COUNT));
        }
    }

    /**
     * Updates graphics runtime information according displayInfo VDSM structure if it exists.
     *
     * @param vm - VmDynamic to update
     * @param xmlRpcStruct - data from VDSM
     * @return true if displayInfo exists, false otherwise
     */
    private static boolean updateGraphicsInfo(VmDynamic vm, Map<String, Object> xmlRpcStruct) {
        Object displayInfo = xmlRpcStruct.get(VdsProperties.displayInfo);

        if (displayInfo == null) {
            return false;
        }

        for (Object info : (Object[]) displayInfo) {
            Map<String, String> infoMap = (Map<String, String>) info;
            GraphicsType graphicsType = GraphicsType.fromString(infoMap.get(VdsProperties.type));

            GraphicsInfo graphicsInfo = new GraphicsInfo();
            graphicsInfo.setIp(infoMap.get(VdsProperties.ipAddress))
                        .setPort(parseIntegerOrNull(infoMap.get(VdsProperties.port)))
                        .setTlsPort(parseIntegerOrNull(infoMap.get(VdsProperties.tlsPort)));

            if (graphicsInfo.getPort() != null || graphicsInfo.getTlsPort() != null) {
                vm.getGraphicsInfos().put(graphicsType, graphicsInfo);
            }
        }
        return true;
    }

    /**
     * Updates graphics runtime information according to vm.conf vdsm structure. It's used with legacy VDSMs that have
     * no notion about graphics device.
     * @param vm - VmDynamic to update
     * @param xmlRpcStruct - data from VDSM
     */
    private static void updateGraphicsInfoFromConf(VmDynamic vm, Map<String, Object> xmlRpcStruct) {
        DisplayType displayType = parseDisplayType(xmlRpcStruct);

        if (displayType == null) {
            log.warn("Can't set display type from XML.");
            return;
        }

        vm.setDisplayType(displayType);

        GraphicsType vmGraphicsType = (displayType == DisplayType.qxl)
                ? GraphicsType.SPICE
                : GraphicsType.VNC;
        GraphicsInfo graphicsInfo = vm.getGraphicsInfos().get(vmGraphicsType);

        if (graphicsInfo != null) {
            if (xmlRpcStruct.containsKey(VdsProperties.display_port)) {
                try {
                    graphicsInfo.setPort(Integer.parseInt(xmlRpcStruct.get(VdsProperties.display_port).toString()));
                } catch (NumberFormatException e) {
                    log.error("vm display_port value illegal : {0}", xmlRpcStruct.get(VdsProperties.display_port));
                }
            } else if (xmlRpcStruct.containsKey(VdsProperties.display)) {
                try {
                    graphicsInfo
                            .setPort(VNC_START_PORT + Integer.parseInt(xmlRpcStruct.get(VdsProperties.display).toString()));
                } catch (NumberFormatException e) {
                    log.error("vm display value illegal : {0}", xmlRpcStruct.get(VdsProperties.display));
                }
            }
            if (xmlRpcStruct.containsKey(VdsProperties.display_secure_port)) {
                try {
                    graphicsInfo
                            .setTlsPort(Integer.parseInt(xmlRpcStruct.get(VdsProperties.display_secure_port).toString()));
                } catch (NumberFormatException e) {
                    log.error("vm display_secure_port value illegal : {0}",
                            xmlRpcStruct.get(VdsProperties.display_secure_port));
                }
            }
            if (xmlRpcStruct.containsKey((VdsProperties.displayIp))) {
                graphicsInfo.setIp((String) xmlRpcStruct.get(VdsProperties.displayIp));
            }
        }
    }

    /**
     * Retrieves display type from xml.
     * @param xmlRpcStruct
     * @return
     *  - display type derived from xml on success
     *  - null on error
     */
    private static DisplayType parseDisplayType(Map<String, Object> xmlRpcStruct) {
        try {
            String displayTypeStr = xmlRpcStruct.get(VdsProperties.displayType).toString();
            return DisplayType.valueOf(displayTypeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseIntegerOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static void updateVMStatisticsData(VmStatistics vm, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            vm.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
        }

        vm.setelapsed_time(AssignDoubleValue(xmlRpcStruct, VdsProperties.elapsed_time));

        // ------------- vm network statistics -----------------------
        if (xmlRpcStruct.containsKey(VdsProperties.VM_NETWORK)) {
            Map networkStruct = (Map) xmlRpcStruct.get(VdsProperties.VM_NETWORK);
            vm.setInterfaceStatistics(new ArrayList<VmNetworkInterface>());
            for (Object tempNic : networkStruct.values()) {
                Map nic = (Map) tempNic;
                VmNetworkInterface stats = new VmNetworkInterface();
                vm.getInterfaceStatistics().add(stats);

                if (nic.containsKey(VdsProperties.VM_INTERFACE_NAME)) {
                    stats.setName((String) ((nic.get(VdsProperties.VM_INTERFACE_NAME) instanceof String) ? nic
                            .get(VdsProperties.VM_INTERFACE_NAME) : null));
                }
                Double rx_rate = AssignDoubleValue(nic, VdsProperties.rx_rate);
                Double rx_dropped = AssignDoubleValue(nic, VdsProperties.rx_dropped);
                Double tx_rate = AssignDoubleValue(nic, VdsProperties.tx_rate);
                Double tx_dropped = AssignDoubleValue(nic, VdsProperties.tx_dropped);
                stats.getStatistics().setReceiveRate(rx_rate != null ? rx_rate : 0);
                stats.getStatistics().setReceiveDropRate(rx_dropped != null ? rx_dropped : 0);
                stats.getStatistics().setTransmitRate(tx_rate != null ? tx_rate : 0);
                stats.getStatistics().setTransmitDropRate(tx_dropped != null ? tx_dropped : 0);
                stats.setMacAddress((String) ((nic.get(VdsProperties.MAC_ADDR) instanceof String) ? nic
                        .get(VdsProperties.MAC_ADDR) : null));
                stats.setSpeed(AssignIntValue(nic, VdsProperties.INTERFACE_SPEED));
            }
        }

        if (xmlRpcStruct.containsKey(VdsProperties.VM_DISKS_USAGE)) {
            initDisksUsage(xmlRpcStruct, vm);
        }

        // ------------- vm cpu statistics -----------------------
        vm.setcpu_sys(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_sys));
        vm.setcpu_user(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_user));

        // ------------- vm memory statistics -----------------------
        vm.setusage_mem_percent(AssignIntValue(xmlRpcStruct, VdsProperties.vm_usage_mem_percent));
        vm.setVmBalloonInfo(getBalloonInfo(xmlRpcStruct));

        // ------------- vm migration statistics -----------------------
        Integer migrationProgress = AssignIntValue(xmlRpcStruct, VdsProperties.vm_migration_progress_percent);
        vm.setMigrationProgressPercent(migrationProgress != null ? migrationProgress : 0);

        // ------------- vm jobs -------------
        vm.setVmJobs(getVmJobs(vm.getId(), xmlRpcStruct));

        // ------------- vm numa nodes runtime info -------------------------
        if (xmlRpcStruct.containsKey(VdsProperties.VM_NUMA_NODES_RUNTIME_INFO)) {
            updateVmNumaNodesRuntimeInfo(vm, xmlRpcStruct);
        }
    }

    private static VmBalloonInfo getBalloonInfo(Map<String, Object> xmlRpcStruct) {
        Map<String, Object> balloonInfo = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.vm_balloonInfo);
        VmBalloonInfo vmBalloonInfo = new VmBalloonInfo();
        if (balloonInfo != null && balloonInfo.size() > 0) {
            vmBalloonInfo.setCurrentMemory(AssignLongValue(balloonInfo, VdsProperties.vm_balloon_cur));
            vmBalloonInfo.setBalloonMaxMemory(AssignLongValue(balloonInfo, VdsProperties.vm_balloon_max));
            vmBalloonInfo.setBalloonTargetMemory(AssignLongValue(balloonInfo, VdsProperties.vm_balloon_target));
            vmBalloonInfo.setBalloonMinMemory(AssignLongValue(balloonInfo, VdsProperties.vm_balloon_min));
            if (balloonInfo.size() >= 4) { // only if all 4 properties are found the balloon is considered enabled (available from 3.3)
                vmBalloonInfo.setBalloonDeviceEnabled(true);
            }
        } else {
            vmBalloonInfo.setBalloonDeviceEnabled(false);
        }
        return vmBalloonInfo;
    }

    private static List<VmJob> getVmJobs(Guid vmId, Map<String, Object> xmlRpcStruct) {
        if (!xmlRpcStruct.containsKey(VdsProperties.vmJobs)) {
            return null;
        }
        List<VmJob> vmJobs = new ArrayList<VmJob>();
        for (Object jobMap : ((Map<String, Object>) xmlRpcStruct.get(VdsProperties.vmJobs)).values()) {
            VmJob job = buildVmJobData(vmId, (Map<String, Object>) jobMap);
            vmJobs.add(job);
        }
        return vmJobs;
    }

    private static VmJob buildVmJobData(Guid vmId, Map<String, Object> xmlRpcStruct) {
        VmJob ret;
        VmJobType jobType = VmJobType.getByName(AssignStringValue(xmlRpcStruct, VdsProperties.vmJobType));
        if (jobType == null) {
            jobType = VmJobType.UNKNOWN;
        }

        switch (jobType) {
        case BLOCK:
            VmBlockJob blockJob = new VmBlockJob();
            blockJob.setBlockJobType(VmBlockJobType.getByName(AssignStringValue(xmlRpcStruct, VdsProperties.vmBlockJobType)));
            blockJob.setCursorCur(AssignLongValue(xmlRpcStruct, VdsProperties.vmJobCursorCur));
            blockJob.setCursorEnd(AssignLongValue(xmlRpcStruct, VdsProperties.vmJobCursorEnd));
            blockJob.setBandwidth(AssignLongValue(xmlRpcStruct, VdsProperties.vmJobBandwidth));
            blockJob.setImageGroupId(new Guid(AssignStringValue(xmlRpcStruct, VdsProperties.vmJobImageUUID)));
            ret = blockJob;
            break;
        default:
            ret = new VmJob();
            break;
        }

        ret.setVmId(vmId);
        ret.setId(new Guid(AssignStringValue(xmlRpcStruct, VdsProperties.vmJobId)));
        ret.setJobState(VmJobState.NORMAL);
        ret.setJobType(jobType);
        return ret;
    }

    public static void updateVDSDynamicData(VDS vds, Map<String, Object> xmlRpcStruct) {
        vds.setSupportedClusterLevels(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_cluster_levels));

        updateNetworkData(vds, xmlRpcStruct);
        updateNumaNodesData(vds, xmlRpcStruct);

        vds.setCpuThreads(AssignIntValue(xmlRpcStruct, VdsProperties.cpuThreads));
        vds.setCpuCores(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_cores));
        vds.setCpuSockets(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_sockets));
        vds.setCpuModel(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_model));
        vds.setOnlineCpus(AssignStringValue(xmlRpcStruct, VdsProperties.online_cpus));
        vds.setCpuSpeedMh(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_speed_mh));
        vds.setPhysicalMemMb(AssignIntValue(xmlRpcStruct, VdsProperties.physical_mem_mb));

        vds.setKvmEnabled(AssignBoolValue(xmlRpcStruct, VdsProperties.kvm_enabled));

        vds.setReservedMem(AssignIntValue(xmlRpcStruct, VdsProperties.reservedMem));
        Integer guestOverhead = AssignIntValue(xmlRpcStruct, VdsProperties.guestOverhead);
        vds.setGuestOverhead(guestOverhead != null ? guestOverhead : 0);

        vds.setCpuFlags(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_flags));

        UpdatePackagesVersions(vds, xmlRpcStruct);

        vds.setSupportedEngines(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_engines));
        vds.setIScsiInitiatorName(AssignStringValue(xmlRpcStruct, VdsProperties.iSCSIInitiatorName));

        vds.setSupportedEmulatedMachines(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.emulatedMachines));

        setRngSupportedSourcesToVds(vds, xmlRpcStruct);

        String hooksStr = ""; // default value if hooks is not in the xml rpc struct
        if (xmlRpcStruct.containsKey(VdsProperties.hooks)) {
            hooksStr = xmlRpcStruct.get(VdsProperties.hooks).toString();
        }
        vds.setHooksStr(hooksStr);

        // parse out the HBAs available in this host
        Map<String, List<Map<String, String>>> hbas = new HashMap<>();
        for (Map.Entry<String, Object[]> el: ((Map<String, Object[]>)xmlRpcStruct.get(VdsProperties.HBAInventory)).entrySet()) {
            List<Map<String, String>> devicesList = new ArrayList<Map<String, String>>();

            for (Object device: el.getValue()) {
                devicesList.add((Map<String, String>)device);
            }

            hbas.put(el.getKey(), devicesList);
        }
        vds.setHBAs(hbas);
        vds.setBootTime(AssignLongValue(xmlRpcStruct, VdsProperties.bootTime));
        vds.setKdumpStatus(KdumpStatus.valueOfNumber(AssignIntValue(xmlRpcStruct, VdsProperties.KDUMP_STATUS)));

        Map<String, Object> selinux = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.selinux);
        if (selinux != null) {
            vds.setSELinuxEnforceMode(AssignIntValue(selinux, VdsProperties.selinux_mode));
        } else {
            vds.setSELinuxEnforceMode(null);
        }

        if (xmlRpcStruct.containsKey(VdsProperties.liveSnapshotSupport)) {
            vds.setLiveSnapshotSupport(AssignBoolValue(xmlRpcStruct, VdsProperties.liveSnapshotSupport));
        } else {
            vds.setLiveSnapshotSupport(true); // for backward compatibility's sake
        }
        if (xmlRpcStruct.containsKey(VdsProperties.liveMergeSupport)) {
            vds.setLiveMergeSupport(AssignBoolValue(xmlRpcStruct, VdsProperties.liveMergeSupport));
        } else {
            vds.setLiveMergeSupport(false);
        }
    }

    private static void setRngSupportedSourcesToVds(VDS vds, Map<String, Object> xmlRpcStruct) {
        vds.getSupportedRngSources().clear();
        String rngSourcesFromStruct = AssignStringValueFromArray(xmlRpcStruct, VdsProperties.rngSources);
        if (rngSourcesFromStruct != null) {
            vds.getSupportedRngSources().addAll(VmRngDevice.csvToSourcesSet(rngSourcesFromStruct.toUpperCase()));
        }
    }

    public static void checkTimeDrift(VDS vds, Map<String, Object> xmlRpcStruct) {
        Boolean isHostTimeDriftEnabled = Config.getValue(ConfigValues.EnableHostTimeDrift);
        if (isHostTimeDriftEnabled) {
            Integer maxTimeDriftAllowed = Config.getValue(ConfigValues.HostTimeDriftInSec);
            Date hostDate = AssignDatetimeValue(xmlRpcStruct, VdsProperties.hostDatetime);
            if (hostDate != null) {
                Long timeDrift =
                        TimeUnit.MILLISECONDS.toSeconds(Math.abs(hostDate.getTime() - System.currentTimeMillis()));
                if (timeDrift > maxTimeDriftAllowed) {
                    AuditLogableBase logable = new AuditLogableBase(vds.getId());
                    logable.addCustomValue("Actual", timeDrift.toString());
                    logable.addCustomValue("Max", maxTimeDriftAllowed.toString());
                    AuditLogDirector.log(logable, AuditLogType.VDS_TIME_DRIFT_ALERT);
                }
            } else {
                log.error("Time Drift validation: failed to get Host or Engine time.");
            }
        }
    }

    private static void initDisksUsage(Map<String, Object> vmStruct, VmStatistics vm) {
        Object[] vmDisksUsage = (Object[]) vmStruct.get(VdsProperties.VM_DISKS_USAGE);
        if (vmDisksUsage != null) {
            ArrayList<Object> disksUsageList = new ArrayList<Object>(Arrays.asList(vmDisksUsage));
            vm.setDisksUsage(SerializationFactory.getSerializer().serializeUnformattedJson(disksUsageList));
        }
    }

    private static void UpdatePackagesVersions(VDS vds, Map<String, Object> xmlRpcStruct) {

        vds.setVersionName(AssignStringValue(xmlRpcStruct, VdsProperties.version_name));
        vds.setSoftwareVersion(AssignStringValue(xmlRpcStruct, VdsProperties.software_version));
        vds.setBuildName(AssignStringValue(xmlRpcStruct, VdsProperties.build_name));
        if (xmlRpcStruct.containsKey(VdsProperties.host_os)) {
            vds.setHostOs(GetPackageVersionFormated(
                    (Map<String, Object>) xmlRpcStruct.get(VdsProperties.host_os), true));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.packages)) {
            // packages is an array of xmlRpcStruct (that each is a name, ver,
            // release.. of a package)
            for (Object hostPackageMap : (Object[]) xmlRpcStruct.get(VdsProperties.packages)) {
                Map<String, Object> hostPackage = (Map<String, Object>) hostPackageMap;
                String packageName = AssignStringValue(hostPackage, VdsProperties.package_name);
                if (VdsProperties.kvmPackageName.equals(packageName)) {
                    vds.setKvmVersion(GetPackageVersionFormated(hostPackage, false));
                } else if (VdsProperties.spicePackageName.equals(packageName)) {
                    vds.setSpiceVersion(GetPackageVersionFormated(hostPackage, false));
                } else if (VdsProperties.kernelPackageName.equals(packageName)) {
                    vds.setKernelVersion(GetPackageVersionFormated(hostPackage, false));
                }
            }
        } else if (xmlRpcStruct.containsKey(VdsProperties.packages2)) {
            Map<String, Object> packages = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.packages2);

            if (packages.containsKey(VdsProperties.vdsmPackageName)) {
                Map<String, Object> vdsm = (Map<String, Object>) packages.get(VdsProperties.vdsmPackageName);
                vds.setVersion(getPackageRpmVersion("vdsm", vdsm));
            }
            if (packages.containsKey(VdsProperties.qemuKvmPackageName)) {
                Map<String, Object> kvm = (Map<String, Object>) packages.get(VdsProperties.qemuKvmPackageName);
                vds.setKvmVersion(getPackageVersionFormated2(kvm));
            }
            if (packages.containsKey(VdsProperties.libvirtPackageName)) {
                Map<String, Object> libvirt = (Map<String, Object>) packages.get(VdsProperties.libvirtPackageName);
                vds.setLibvirtVersion(getPackageRpmVersion("libvirt", libvirt));
            }
            if (packages.containsKey(VdsProperties.spiceServerPackageName)) {
                Map<String, Object> spice = (Map<String, Object>) packages.get(VdsProperties.spiceServerPackageName);
                vds.setSpiceVersion(getPackageVersionFormated2(spice));
            }
            if (packages.containsKey(VdsProperties.kernelPackageName)) {
                Map<String, Object> kernel = (Map<String, Object>) packages.get(VdsProperties.kernelPackageName);
                vds.setKernelVersion(getPackageVersionFormated2(kernel));
            }
            if (packages.containsKey(VdsProperties.GLUSTER_PACKAGE_NAME)) {
                Map<String, Object> gluster = (Map<String, Object>) packages.get(VdsProperties.GLUSTER_PACKAGE_NAME);
                vds.setGlusterVersion(getPackageRpmVersion("glusterfs", gluster));
            }
        }
    }

    // Version 2 of GetPackageVersionFormated2:
    // from 2.3 we get dictionary and not a flat list.
    // from now the packages names (of spice, kernel, qemu and libvirt) are the same as far as VDSM and ENGINE.
    // (VDSM use to report packages name of rpm so in RHEL6 when it change it broke our interface)
    private static String getPackageVersionFormated2(Map<String, Object> hostPackage) {

        String packageVersion = (hostPackage.get(VdsProperties.package_version) != null) ? (String) hostPackage
                .get(VdsProperties.package_version) : null;
        String packageRelease = (hostPackage.get(VdsProperties.package_release) != null) ? (String) hostPackage
                .get(VdsProperties.package_release) : null;

        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageVersion)) {
            sb.append(packageVersion);
        }
        if (!StringUtils.isEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    private static RpmVersion getPackageRpmVersion(String packageName, Map<String, Object> hostPackage) {

        String packageVersion = (hostPackage.get(VdsProperties.package_version) != null) ? (String) hostPackage
                .get(VdsProperties.package_version) : null;
        String packageRelease = (hostPackage.get(VdsProperties.package_release) != null) ? (String) hostPackage
                .get(VdsProperties.package_release) : null;

        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageName)) {
            sb.append(packageName);
        }
        boolean hasPackageVersion = StringUtils.isEmpty(packageVersion);
        boolean hasPackageRelease = StringUtils.isEmpty(packageRelease);
        if (!hasPackageVersion || !hasPackageRelease) {
            sb.append("-");
        }
        if (!hasPackageVersion) {
            sb.append(packageVersion);
        }
        if (!hasPackageRelease) {
            if (sb.length() > 0) {
                sb.append(String.format("-%1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return new RpmVersion(sb.toString());
    }

    public static void updateHardwareSystemInformation(Map<String, Object> hwInfo, VDS vds){
        vds.setHardwareManufacturer(AssignStringValue(hwInfo, VdsProperties.hwManufacturer));
        vds.setHardwareProductName(AssignStringValue(hwInfo, VdsProperties.hwProductName));
        vds.setHardwareVersion(AssignStringValue(hwInfo, VdsProperties.hwVersion));
        vds.setHardwareSerialNumber(AssignStringValue(hwInfo, VdsProperties.hwSerialNumber));
        vds.setHardwareUUID(AssignStringValue(hwInfo, VdsProperties.hwUUID));
        vds.setHardwareFamily(AssignStringValue(hwInfo, VdsProperties.hwFamily));
    }

    private static String GetPackageVersionFormated(Map<String, Object> hostPackage, boolean getName) {
        String packageName = AssignStringValue(hostPackage, VdsProperties.package_name);
        String packageVersion = AssignStringValue(hostPackage, VdsProperties.package_version);
        String packageRelease = AssignStringValue(hostPackage, VdsProperties.package_release);
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageName) && getName) {
            sb.append(packageName);
        }
        if (!StringUtils.isEmpty(packageVersion)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageVersion));
            } else {
                sb.append(packageVersion);
            }
        }
        if (!StringUtils.isEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    public static void updateVDSStatisticsData(VDS vds, Map<String, Object> xmlRpcStruct) {
        // ------------- vds memory usage ---------------------------
        vds.setUsageMemPercent(AssignIntValue(xmlRpcStruct, VdsProperties.mem_usage));

        // ------------- vds network statistics ---------------------
        Map<String, Object> interfaces = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORK);
        if (interfaces != null) {
            int networkUsage = 0;
            Map<String, VdsNetworkInterface> nicsByName = Entities.entitiesByName(vds.getInterfaces());
            for (Entry<String, Object> entry : interfaces.entrySet()) {
                if (nicsByName.containsKey(entry.getKey())) {
                    VdsNetworkInterface iface = nicsByName.get(entry.getKey());
                    iface.setVdsId(vds.getId());
                    Map<String, Object> dict = (Map<String, Object>) entry.getValue();
                    Double rx_rate = AssignDoubleValue(dict, VdsProperties.rx_rate);
                    Double rx_dropped = AssignDoubleValue(dict, VdsProperties.rx_dropped);
                    Double tx_rate = AssignDoubleValue(dict, VdsProperties.tx_rate);
                    Double tx_dropped = AssignDoubleValue(dict, VdsProperties.tx_dropped);
                    iface.getStatistics().setReceiveRate(rx_rate != null ? rx_rate : 0);
                    iface.getStatistics().setReceiveDropRate(rx_dropped != null ? rx_dropped : 0);
                    iface.getStatistics().setTransmitRate(tx_rate != null ? tx_rate : 0);
                    iface.getStatistics().setTransmitDropRate(tx_dropped != null ? tx_dropped : 0);
                    iface.setSpeed(AssignIntValue(dict, VdsProperties.INTERFACE_SPEED));
                    iface.getStatistics().setStatus(AssignInterfaceStatusValue(dict, VdsProperties.iface_status));

                    if (!NetworkUtils.isVlan(iface) && !iface.isBondSlave()) {
                        networkUsage = (int) Math.max(networkUsage, computeInterfaceUsage(iface));
                    }
                }
            }
            vds.setUsageNetworkPercent(networkUsage);
        }

        // ----------- vds cpu statistics info ---------------------
        vds.setCpuSys(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_sys));
        vds.setCpuUser(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_user));
        if (vds.getCpuSys() != null && vds.getCpuUser() != null) {
            vds.setUsageCpuPercent((int) (vds.getCpuSys() + vds.getCpuUser()));
        }
        // CPU load reported by VDSM is in uptime-style format, i.e. normalized
        // to unity, so that say an 8% load is reported as 0.08

        Double d = AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_load);
        d = (d != null) ? d : 0;
        vds.setCpuLoad(d.doubleValue() * 100.0);
        vds.setCpuIdle(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_idle));
        vds.setMemAvailable(AssignLongValue(xmlRpcStruct, VdsProperties.mem_available));
        vds.setMemFree(AssignLongValue(xmlRpcStruct, VdsProperties.memFree));
        vds.setMemShared(AssignLongValue(xmlRpcStruct, VdsProperties.mem_shared));

        vds.setSwapFree(AssignLongValue(xmlRpcStruct, VdsProperties.swap_free));
        vds.setSwapTotal(AssignLongValue(xmlRpcStruct, VdsProperties.swap_total));
        vds.setKsmCpuPercent(AssignIntValue(xmlRpcStruct, VdsProperties.ksm_cpu_percent));
        vds.setKsmPages(AssignLongValue(xmlRpcStruct, VdsProperties.ksm_pages));
        vds.setKsmState(AssignBoolValue(xmlRpcStruct, VdsProperties.ksm_state));

        // dynamic data got from GetVdsStats
        if (xmlRpcStruct.containsKey(VdsProperties.transparent_huge_pages_state)) {
            vds.setTransparentHugePagesState(EnumUtils.valueOf(VdsTransparentHugePagesState.class, xmlRpcStruct
                    .get(VdsProperties.transparent_huge_pages_state).toString(), true));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.anonymous_transparent_huge_pages)) {
            vds.setAnonymousHugePages(AssignIntValue(xmlRpcStruct, VdsProperties.anonymous_transparent_huge_pages));
        }
        vds.setNetConfigDirty(AssignBoolValue(xmlRpcStruct, VdsProperties.netConfigDirty));

        vds.setImagesLastCheck(AssignDoubleValue(xmlRpcStruct, VdsProperties.images_last_check));
        vds.setImagesLastDelay(AssignDoubleValue(xmlRpcStruct, VdsProperties.images_last_delay));

        Integer vm_count = AssignIntValue(xmlRpcStruct, VdsProperties.vm_count);
        vds.setVmCount(vm_count == null ? 0 : vm_count);
        vds.setVmActive(AssignIntValue(xmlRpcStruct, VdsProperties.vm_active));
        vds.setVmMigrating(AssignIntValue(xmlRpcStruct, VdsProperties.vm_migrating));
        updateVDSDomainData(vds, xmlRpcStruct);
        updateLocalDisksUsage(vds, xmlRpcStruct);

        // hosted engine
        Integer haScore = null;
        Boolean haIsConfigured = null;
        Boolean haIsActive = null;
        Boolean haGlobalMaint = null;
        Boolean haLocalMaint = null;
        if (xmlRpcStruct.containsKey(VdsProperties.ha_stats)) {
            Map<String, Object> haStats = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.ha_stats);
            if (haStats != null) {
                haScore = AssignIntValue(haStats, VdsProperties.ha_stats_score);
                haIsConfigured = AssignBoolValue(haStats, VdsProperties.ha_stats_is_configured);
                haIsActive = AssignBoolValue(haStats, VdsProperties.ha_stats_is_active);
                haGlobalMaint = AssignBoolValue(haStats, VdsProperties.ha_stats_global_maintenance);
                haLocalMaint = AssignBoolValue(haStats, VdsProperties.ha_stats_local_maintenance);
            }
        } else {
            haScore = AssignIntValue(xmlRpcStruct, VdsProperties.ha_score);
            // prior to 3.4, haScore was returned if ha was installed; assume active if > 0
            if (haScore != null) {
                haIsConfigured = true;
                haIsActive = (haScore > 0);
            }
        }
        vds.setHighlyAvailableScore(haScore != null ? haScore : 0);
        vds.setHighlyAvailableIsConfigured(haIsConfigured != null ? haIsConfigured : false);
        vds.setHighlyAvailableIsActive(haIsActive != null ? haIsActive : false);
        vds.setHighlyAvailableGlobalMaintenance(haGlobalMaint != null ? haGlobalMaint : false);
        vds.setHighlyAvailableLocalMaintenance(haLocalMaint != null ? haLocalMaint : false);

        vds.setBootTime(AssignLongValue(xmlRpcStruct, VdsProperties.bootTime));

        updateNumaStatisticsData(vds, xmlRpcStruct);

    }

    private static double computeInterfaceUsage(VdsNetworkInterface iface) {
        return Math.max(truncatePercentage(iface.getStatistics().getReceiveRate()),
                truncatePercentage(iface.getStatistics().getTransmitRate()));
    }

    private static double truncatePercentage(double value) {
        return Math.min(100, value);
    }

    public static void updateNumaStatisticsData(VDS vds, Map<String, Object> xmlRpcStruct) {
        List<VdsNumaNode> vdsNumaNodes = new ArrayList<>();
        List<CpuStatistics> cpuStatsData = new ArrayList<>();
        if (xmlRpcStruct.containsKey(VdsProperties.CPU_STATS)) {
            Map<String, Map<String, Object>> cpuStats = (Map<String, Map<String, Object>>)
                    xmlRpcStruct.get(VdsProperties.CPU_STATS);
            Map<Integer, List<CpuStatistics>> numaNodeCpuStats = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> item : cpuStats.entrySet()) {
                CpuStatistics data = buildVdsCpuStatistics(item);
                cpuStatsData.add(data);
                int numaNodeIndex = AssignIntValue(item.getValue(), VdsProperties.NUMA_NODE_INDEX);
                if (!numaNodeCpuStats.containsKey(numaNodeIndex)) {
                    numaNodeCpuStats.put(numaNodeIndex, new ArrayList<CpuStatistics>());
                }
                numaNodeCpuStats.get(numaNodeIndex).add(data);
            }
            DecimalFormat percentageFormatter = new DecimalFormat("#.##");
            for (Map.Entry<Integer, List<CpuStatistics>> item : numaNodeCpuStats.entrySet()) {
                VdsNumaNode node = buildVdsNumaNodeStatistics(percentageFormatter, item);
                vdsNumaNodes.add(node);
            }
        }
        if (xmlRpcStruct.containsKey(VdsProperties.NUMA_NODE_FREE_MEM_STAT)) {
            Map<String, Map<String, Object>> memStats = (Map<String, Map<String, Object>>)
                    xmlRpcStruct.get(VdsProperties.NUMA_NODE_FREE_MEM_STAT);
            for (Map.Entry<String, Map<String, Object>> item : memStats.entrySet()) {
                VdsNumaNode node = NumaUtils.getVdsNumaNodeByIndex(vdsNumaNodes, Integer.valueOf(item.getKey()));
                if (node != null) {
                    node.getNumaNodeStatistics().setMemFree(AssignLongValue(item.getValue(),
                            VdsProperties.NUMA_NODE_FREE_MEM));
                    node.getNumaNodeStatistics().setMemUsagePercent(AssignIntValue(item.getValue(),
                            VdsProperties.NUMA_NODE_MEM_PERCENT));
                }
            }
        }
        vds.getNumaNodeList().clear();
        vds.getNumaNodeList().addAll(vdsNumaNodes);
        vds.getStatisticsData().getCpuCoreStatistics().clear();
        vds.getStatisticsData().getCpuCoreStatistics().addAll(cpuStatsData);
    }

    private static VdsNumaNode buildVdsNumaNodeStatistics(DecimalFormat percentageFormatter,
            Map.Entry<Integer, List<CpuStatistics>> item) {
        VdsNumaNode node = new VdsNumaNode();
        NumaNodeStatistics nodeStat = new NumaNodeStatistics();
        double nodeCpuUser = 0.0;
        double nodeCpuSys = 0.0;
        double nodeCpuIdle = 0.0;
        for (CpuStatistics cpuStat : item.getValue()) {
            nodeCpuUser += cpuStat.getCpuUser();
            nodeCpuSys += cpuStat.getCpuSys();
            nodeCpuIdle += cpuStat.getCpuIdle();
        }
        nodeStat.setCpuUser(Double.valueOf(percentageFormatter.format(nodeCpuUser / item.getValue().size())));
        nodeStat.setCpuSys(Double.valueOf(percentageFormatter.format(nodeCpuSys / item.getValue().size())));
        nodeStat.setCpuIdle(Double.valueOf(percentageFormatter.format(nodeCpuIdle / item.getValue().size())));
        nodeStat.setCpuUsagePercent((int) (nodeStat.getCpuSys() + nodeStat.getCpuUser()));
        node.setIndex(item.getKey());
        node.setNumaNodeStatistics(nodeStat);
        return node;
    }

    private static CpuStatistics buildVdsCpuStatistics(Map.Entry<String, Map<String, Object>> item) {
        CpuStatistics data = new CpuStatistics();
        data.setCpuId(Integer.valueOf(item.getKey()));
        data.setCpuUser(AssignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_USER));
        data.setCpuSys(AssignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_SYS));
        data.setCpuIdle(AssignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_IDLE));
        data.setCpuUsagePercent((int) (data.getCpuSys() + data.getCpuUser()));
        return data;
    }

    /**
     * Update {@link VDS#setLocalDisksUsage(Map)} with map of paths usage extracted from the returned returned value. The
     * usage is reported in MB.
     *
     * @param vds
     *            The VDS object to update.
     * @param xmlRpcStruct
     *            The XML/RPC to extract the usage from.
     */
    protected static void updateLocalDisksUsage(VDS vds, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.DISK_STATS)) {
            Map<String, Object> diskStatsStruct = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.DISK_STATS);
            Map<String, Long> diskStats = new HashMap<String, Long>();

            vds.setLocalDisksUsage(diskStats);

            for (Entry<String, Object> entry : diskStatsStruct.entrySet()) {
                Map<String, Object> pathStatsStruct = (Map<String, Object>) entry.getValue();

                diskStats.put(entry.getKey(), AssignLongValue(pathStatsStruct, VdsProperties.DISK_STATS_FREE));
            }
        }
    }

    private static void updateVDSDomainData(VDS vds, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.domains)) {
            Map<String, Object> domains = (Map<String, Object>)
                    xmlRpcStruct.get(VdsProperties.domains);
            ArrayList<VDSDomainsData> domainsData = new ArrayList<VDSDomainsData>();
            for (Map.Entry<String, ?> value : domains.entrySet()) {
                try {
                    VDSDomainsData data = new VDSDomainsData();
                    data.setDomainId(new Guid(value.getKey().toString()));
                    Map<String, Object> internalValue = (Map<String, Object>) value.getValue();
                    double lastCheck = 0;
                    data.setCode((Integer) (internalValue).get(VdsProperties.code));
                    if (internalValue.containsKey(VdsProperties.lastCheck)) {
                        lastCheck = Double.parseDouble((String) internalValue.get(VdsProperties.lastCheck));
                    }
                    data.setLastCheck(lastCheck);
                    double delay = 0;
                    if (internalValue.containsKey(VdsProperties.delay)) {
                        delay = Double.parseDouble((String) internalValue.get(VdsProperties.delay));
                    }
                    data.setDelay(delay);
                    domainsData.add(data);
                } catch (Exception e) {
                    log.error("failed building domains: {}", e.getMessage());
                    log.debug("Exception", e);
                }
            }
            vds.setDomains(domainsData);
        }
    }

    private static InterfaceStatus AssignInterfaceStatusValue(Map<String, Object> input, String name) {
        InterfaceStatus ifaceStatus = InterfaceStatus.NONE;
        if (input.containsKey(name)) {
            String stringValue = (String) ((input.get(name) instanceof String) ? input.get(name) : null);
            if (!StringUtils.isEmpty(stringValue)) {
                if (stringValue.toLowerCase().trim().equals("up")) {
                    ifaceStatus = InterfaceStatus.UP;
                } else {
                    ifaceStatus = InterfaceStatus.DOWN;
                }
            }
        }
        return ifaceStatus;
    }

    private static Double AssignDoubleValue(Map<String, Object> input, String name) {
        Double returnValue = null;
        if (input.containsKey(name)) {
            String stringValue = (String) ((input.get(name) instanceof String) ? input.get(name) : null);
            returnValue = (stringValue == null) ? null : Double.parseDouble(stringValue);
        }
        return returnValue;
    }

    /**
     * Do the same logic as AssignDoubleValue does, but instead, in case of null we return 0.
     * @param input - the Input xml
     * @param name - The name of the field we want to cast it to double.
     * @return - the double value.
     */
    private static Double assignDoubleValueWithNullProtection(Map<String, Object> input, String name) {
        Double doubleValue = AssignDoubleValue(input, name);
        return (doubleValue == null ? Double.valueOf(0.0) : doubleValue);
    }

    private static Integer AssignIntValue(Map input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Integer) {
                return (Integer) input.get(name);
            }
            String stringValue = (String) input.get(name);
            if (!StringUtils.isEmpty(stringValue)) { // in case the input
                                                     // is decimal and we
                                                     // need int.
                stringValue = stringValue.split("[.]", -1)[0];
            }
            try {
                int intValue = Integer.parseInt(stringValue);
                return intValue;
            } catch (NumberFormatException nfe) {
                log.error("Failed to parse '{}' value '{}' to integer: {}", name, stringValue, nfe.getMessage());
            }
        }
        return null;
    }

    private static Long AssignLongValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Long || input.get(name) instanceof Integer) {
                return Long.parseLong(input.get(name).toString());
            }
            String stringValue = (String) ((input.get(name) instanceof String) ? input.get(name) : null);
            if (!StringUtils.isEmpty(stringValue)) { // in case the input
                                                     // is decimal and we
                                                     // need int.
                stringValue = stringValue.split("[.]", -1)[0];
            }
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException e) {
                log.error("Failed to parse '{}' value '{}' to long: {}", name, stringValue, e.getMessage());
            }
        }
        return null;
    }

    private static String AssignStringValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            return (String) ((input.get(name) instanceof String) ? input.get(name) : null);
        }
        return null;
    }

    private static String AssignStringValueFromArray(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            String[] arr = (String[]) ((input.get(name) instanceof String[]) ? input.get(name) : null);
            if (arr == null) {
                Object[] arr2 = (Object[]) ((input.get(name) instanceof Object[]) ? input.get(name) : null);
                if (arr2 != null) {
                    arr = new String[arr2.length];
                    for (int i = 0; i < arr2.length; i++)
                        arr[i] = arr2[i].toString();
                }
            }
            if (arr != null) {
                return StringUtils.join(arr, ',');
            }
        }
        return null;
    }

    private static Date AssignDateTImeFromEpoch(Map<String, Object> input, String name) {
        Date retval = null;
        try {
            if (input.containsKey(name)) {
                Double secsSinceEpoch = (Double) input.get(name);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(secsSinceEpoch.longValue());
                retval = calendar.getTime();
            }
        } catch (RuntimeException ex) {
            log.warn("VdsBroker::AssignDateTImeFromEpoch - failed to convert field '{}' to dateTime: {}",
                    name, ex.getMessage());
            log.debug("Exception", ex);
            retval = null;
        }
        return retval;
    }

    private static Date AssignDatetimeValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Date) {
                return (Date) input.get(name);
            }
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            try {
                String dateStr = input.get(name).toString().replaceFirst("T", " ").trim();
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Boolean AssignBoolValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Boolean) {
                return (Boolean) input.get(name);
            }
            return Boolean.parseBoolean(input.get(name).toString());
        }
        return Boolean.FALSE;
    }

    private static void initDisks(Map<String, Object> vmStruct, VmDynamic vm) {
        Map<String, Object> disks = (Map<String, Object>) vmStruct.get(VdsProperties.vm_disks);
        ArrayList<DiskImageDynamic> disksData = new ArrayList<DiskImageDynamic>();
        for (Object diskAsObj : disks.values()) {
            Map<String, Object> disk = (Map<String, Object>) diskAsObj;
            DiskImageDynamic diskData = new DiskImageDynamic();
            String imageGroupIdString = AssignStringValue(disk, VdsProperties.image_group_id);
            if (!StringUtils.isEmpty(imageGroupIdString)) {
                Guid imageGroupIdGuid = new Guid(imageGroupIdString);
                diskData.setId(imageGroupIdGuid);
                diskData.setread_rate(AssignIntValue(disk, VdsProperties.vm_disk_read_rate));
                diskData.setwrite_rate(AssignIntValue(disk, VdsProperties.vm_disk_write_rate));

                if (disk.containsKey(VdsProperties.disk_actual_size)) {
                    Long size = AssignLongValue(disk, VdsProperties.disk_actual_size);
                    diskData.setactual_size(size != null ? size * 512 : 0);
                } else if (disk.containsKey(VdsProperties.disk_true_size)) {
                    Long size = AssignLongValue(disk, VdsProperties.disk_true_size);
                    diskData.setactual_size(size != null ? size : 0);
                }
                if (disk.containsKey(VdsProperties.vm_disk_read_latency)) {
                    diskData.setReadLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_read_latency) / NANO_SECONDS);
                }
                if (disk.containsKey(VdsProperties.vm_disk_write_latency)) {
                    diskData.setWriteLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_write_latency) / NANO_SECONDS);
                }
                if (disk.containsKey(VdsProperties.vm_disk_flush_latency)) {
                    diskData.setFlushLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_flush_latency) / NANO_SECONDS);
                }
                disksData.add(diskData);
            }
        }
        vm.setDisks(disksData);
    }

    private static void initAppsList(Map<String, Object> vmStruct, VmDynamic vm) {
        if (vmStruct.containsKey(VdsProperties.app_list)) {
            Object tempAppsList = vmStruct.get(VdsProperties.app_list);
            if (tempAppsList instanceof Object[]) {
                Object[] apps = (Object[]) tempAppsList;
                StringBuilder builder = new StringBuilder();
                boolean firstTime = true;
                for (Object app : apps) {
                    String appString = (String) ((app instanceof String) ? app : null);
                    if (app == null) {
                        log.warn("Failed to convert app: [null] to string");
                    }
                    if (!firstTime) {
                        builder.append(",");
                    } else {
                        firstTime = false;
                    }
                    builder.append(appString);
                }
                vm.setAppList(builder.toString());
            } else {
                vm.setAppList("");
            }
        }
    }

    private static VMStatus convertToVmStatus(String statusName) {
        VMStatus status = VMStatus.Unassigned;

        // TODO: The following condition should deleted as soon as we drop compatibility with 3.3 since "Running" state
        // will be replaced "Up" state and "Unknown" will exist no more. The "Up" state will be processed by
        // EnumUtils as other states below.
        if ("Running".equals(statusName) || "Unknown".equals(statusName)) {
            status = VMStatus.Up;
        }
        else if ("Migration Source".equals(statusName)) {
            status = VMStatus.MigratingFrom;
        }
        else if ("Migration Destination".equals(statusName)) {
            status = VMStatus.MigratingTo;
        } else {
            try {
                statusName = statusName.replace(" ", "");
                status = EnumUtils.valueOf(VMStatus.class, statusName, true);
            } catch (Exception e) {
                log.error("Illegal Vm status: '{}'.", statusName);
            }
        }
        return status;
    }

    /**
     * Updates the host network data with the network data reported by the host
     *
     * @param vds
     *            The host to update
     * @param xmlRpcStruct
     *            A nested map contains network interfaces data
     */
    public static void updateNetworkData(VDS vds, Map<String, Object> xmlRpcStruct) {
        vds.setActiveNic(AssignStringValue(xmlRpcStruct, VdsProperties.NETWORK_LAST_CLIENT_INTERFACE));

        List<VdsNetworkInterface> oldInterfaces =
                DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(vds.getId());
        vds.getInterfaces().clear();

        addHostNetworkInterfaces(vds, xmlRpcStruct);

        addHostVlanDevices(vds, xmlRpcStruct);

        addHostBondDevices(vds, xmlRpcStruct);

        addHostNetworksAndUpdateInterfaces(vds, xmlRpcStruct);

        // set bonding options
        setBondingOptions(vds, oldInterfaces);

        // This information was added in 3.1, so don't use it if it's not there.
        if (xmlRpcStruct.containsKey(VdsProperties.netConfigDirty)) {
            vds.setNetConfigDirty(AssignBoolValue(xmlRpcStruct, VdsProperties.netConfigDirty));
        }
    }

    private static void addHostNetworksAndUpdateInterfaces(VDS vds,
            Map<String, Object> xmlRpcStruct) {

        // Networks collection (name point to list of nics or bonds)
        Map<String, Object> networks = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORKS);

        if (networks != null) {
            vds.getNetworks().clear();
            for (Entry<String, Object> entry : networks.entrySet()) {
                Map<String, Object> network = (Map<String, Object>) entry.getValue();
                if (network != null) {
                    Network net = createNetworkData(entry.getKey(), network);

                    List<VdsNetworkInterface> interfaces = findNetworkInterfaces(vds, xmlRpcStruct, network);

                    for (VdsNetworkInterface iface : interfaces) {
                        updateNetworkDetailsInInterface(iface,
                                network,
                                vds,
                                net);
                    }

                    vds.getNetworks().add(net);
                    reportInvalidInterfacesForNetwork(interfaces, net, vds);
                }
            }
        }
    }

    /**
     * Reports a warning to the audit log if a bridge is connected to more than one interface which is considered bad
     * configuration.
     *
     * @param interfaces
     *            The network's interfaces
     * @param network
     *            The network to report for
     * @param vds
     *            The host in which the network is defined
     */
    private static void reportInvalidInterfacesForNetwork(List<VdsNetworkInterface> interfaces, Network network, VDS vds) {
        if (interfaces.isEmpty()) {
            AuditLogDirector.log(createHostNetworkAuditLog(network, vds), AuditLogType.NETWORK_WITHOUT_INTERFACES);
        } else if (interfaces.size() > 1) {
            AuditLogableBase logable = createHostNetworkAuditLog(network, vds);
            logable.addCustomValue("Interfaces", StringUtils.join(Entities.objectNames(interfaces), ","));
            AuditLogDirector.log(logable, AuditLogType.BRIDGED_NETWORK_OVER_MULTIPLE_INTERFACES);
        }
    }

    protected static AuditLogableBase createHostNetworkAuditLog(Network network, VDS vds) {
        AuditLogableBase logable = new AuditLogableBase(vds.getId());
        logable.addCustomValue("NetworkName", network.getName());
        return logable;
    }

    private static List<VdsNetworkInterface> findNetworkInterfaces(VDS vds,
            Map<String, Object> xmlRpcStruct,
            Map<String, Object> network) {

        Map<String, VdsNetworkInterface> vdsInterfaces = Entities.entitiesByName(vds.getInterfaces());

        List<VdsNetworkInterface> interfaces = new ArrayList<VdsNetworkInterface>();
        if (FeatureSupported.bridgesReportByVdsm(vds.getVdsGroupCompatibilityVersion())) {
            VdsNetworkInterface iface = null;
            String interfaceName = (String) network.get(VdsProperties.INTERFACE);
            if (interfaceName != null) {
                iface = vdsInterfaces.get(interfaceName);
                if (iface == null) {
                    Map<String, Object> bridges =
                            (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORK_BRIDGES);
                    if (bridges != null && bridges.containsKey(interfaceName)) {
                        interfaces.addAll(findBridgedNetworkInterfaces((Map<String, Object>) bridges.get(interfaceName),
                                vdsInterfaces));
                    }
                } else {
                    interfaces.add(iface);
                }
            }
        } else {
            interfaces.addAll(findBridgedNetworkInterfaces(network, vdsInterfaces));
        }
        return interfaces;
    }

    private static Network createNetworkData(String networkName, Map<String, Object> network) {
        Network net = new Network();
        net.setName(networkName);
        net.setAddr((String) network.get("addr"));
        net.setSubnet((String) network.get("netmask"));
        net.setGateway((String) network.get(VdsProperties.GLOBAL_GATEWAY));
        if (StringUtils.isNotBlank((String) network.get(VdsProperties.MTU))) {
            net.setMtu(Integer.parseInt((String) network.get(VdsProperties.MTU)));
        }
        return net;
    }

    private static List<VdsNetworkInterface> findBridgedNetworkInterfaces(Map<String, Object> bridge,
            Map<String, VdsNetworkInterface> vdsInterfaces) {
        List<VdsNetworkInterface> interfaces = new ArrayList<VdsNetworkInterface>();
        Object[] ports = (Object[]) bridge.get("ports");
        if (ports != null) {
            for (Object port : ports) {
                if (vdsInterfaces.containsKey(port.toString())) {
                    interfaces.add(vdsInterfaces.get(port.toString()));
                }
            }
        }
        return interfaces;
    }

    private static void addHostBondDevices(VDS vds, Map<String, Object> xmlRpcStruct) {
        Map<String, Object> bonds = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORK_BONDINGS);
        if (bonds != null) {
            for (Entry<String, Object> entry : bonds.entrySet()) {
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.newGuid());
                iStats.setVdsId(vds.getId());
                iface.setId(iStats.getId());

                iface.setName(entry.getKey());
                iface.setVdsId(vds.getId());
                iface.setBonded(true);

                Map<String, Object> bond = (Map<String, Object>) entry.getValue();
                if (bond != null) {
                    iface.setMacAddress((String) bond.get("hwaddr"));
                    iface.setAddress((String) bond.get("addr"));
                    iface.setSubnet((String) bond.get("netmask"));
                    if (bond.get("slaves") != null) {
                        addBondDeviceToHost(vds, iface, (Object[]) bond.get("slaves"));
                    }

                    if (StringUtils.isNotBlank((String) bond.get(VdsProperties.MTU))) {
                        iface.setMtu(Integer.parseInt((String) bond.get(VdsProperties.MTU)));
                    }

                    Map<String, Object> config =
                            (Map<String, Object>) bond.get("cfg");

                    if (config != null && config.get("BONDING_OPTS") != null) {
                        iface.setBondOptions(config.get("BONDING_OPTS").toString());
                    }
                    addBootProtocol(config, vds, iface);
                }
            }
        }
    }

    /**
     * Updates the host interfaces list with vlan devices
     *
     * @param vds
     *            The host to update
     * @param xmlRpcStruct
     *            a map contains pairs of vlan device name and vlan data
     */
    private static void addHostVlanDevices(VDS vds, Map<String, Object> xmlRpcStruct) {
        // vlans
        Map<String, Object> vlans = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORK_VLANS);
        if (vlans != null) {
            for (Entry<String, Object> entry : vlans.entrySet()) {
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.newGuid());
                iface.setId(iStats.getId());

                String vlanDeviceName = entry.getKey();
                iface.setName(vlanDeviceName);
                iface.setVdsId(vds.getId());

                Map<String, Object> vlan = (Map<String, Object>) entry.getValue();

                if (vlan.get(VdsProperties.VLAN_ID) != null && vlan.get(VdsProperties.BASE_INTERFACE) != null) {
                    iface.setVlanId((Integer) vlan.get(VdsProperties.VLAN_ID));
                    iface.setBaseInterface((String) vlan.get(VdsProperties.BASE_INTERFACE));
                } else if (vlanDeviceName.contains(".")) {
                    String[] names = vlanDeviceName.split("[.]", -1);
                    String vlanId = names[1];
                    iface.setVlanId(Integer.parseInt(vlanId));
                    iface.setBaseInterface(names[0]);
                }

                iface.setAddress((String) vlan.get("addr"));
                iface.setSubnet((String) vlan.get("netmask"));
                if (StringUtils.isNotBlank((String) vlan.get(VdsProperties.MTU))) {
                    iface.setMtu(Integer.parseInt((String) vlan.get(VdsProperties.MTU)));
                }

                iStats.setVdsId(vds.getId());
                addBootProtocol((Map<String, Object>) vlan.get("cfg"), vds, iface);
                vds.getInterfaces().add(iface);
            }
        }
    }

    /**
     * Updates the host network interfaces with the collected data from the host
     *
     * @param vds
     *            The host to update its interfaces
     * @param xmlRpcStruct
     *            A nested map contains network interfaces data
     */
    private static void addHostNetworkInterfaces(VDS vds, Map<String, Object> xmlRpcStruct) {
        Map<String, Object> nics = (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NETWORK_NICS);
        if (nics != null) {
            for (Entry<String, Object> entry : nics.entrySet()) {
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.newGuid());
                iface.setId(iStats.getId());
                iface.setName(entry.getKey());
                iface.setVdsId(vds.getId());

                updateNetworkInterfaceDataFromHost(iface, vds, (Map<String, Object>) entry.getValue());

                iStats.setVdsId(vds.getId());
                vds.getInterfaces().add(iface);
            }
        }
    }

    /**
     * Updates a given interface by data as collected from the host.
     *
     * @param iface
     *            The interface to update
     * @param nic
     *            A key-value map of the interface properties and their value
     */
    private static void updateNetworkInterfaceDataFromHost(
            VdsNetworkInterface iface, VDS host, Map<String, Object> nic) {
        if (nic != null) {
            if (nic.get("speed") != null) {
                Object speed = nic.get("speed");
                iface.setSpeed((Integer) speed);
            }
            iface.setAddress((String) nic.get("addr"));
            iface.setSubnet((String) nic.get("netmask"));
            iface.setMacAddress((String) nic.get("hwaddr"));
            // if we get "permhwaddr", we are a part of a bond and we use that as the mac address
            String mac = (String) nic.get("permhwaddr");
            if (mac != null) {
                //TODO remove when the minimal supported vdsm version is >=3.6
                // in older VDSM version, slave's Mac is in upper case
                iface.setMacAddress(mac.toLowerCase());
            }
            if (StringUtils.isNotBlank((String) nic.get(VdsProperties.MTU))) {
                iface.setMtu(Integer.parseInt((String) nic.get(VdsProperties.MTU)));
            }
            addBootProtocol((Map<String, Object>) nic.get("cfg"), host, iface);
        }
    }

    /**
     * Update the network details on a given interface.
     *
     * @param iface
     *            The interface to update.
     * @param network
     *            Network struct to get details from.
     * @param net
     *            Network to get details from.
     */
    private static void updateNetworkDetailsInInterface(VdsNetworkInterface iface,
            Map<String, Object> network,
            VDS host,
            Network net) {

        if (iface != null) {
            iface.setNetworkName(net.getName());

            // set the management ip
            if (StringUtils.equals(iface.getNetworkName(), NetworkUtils.getEngineNetwork())) {
                iface.setType(iface.getType() | VdsInterfaceType.MANAGEMENT.getValue());
            }

            iface.setAddress(net.getAddr());
            iface.setSubnet(net.getSubnet());
            boolean bridgedNetwork = isBridgedNetwork(network);
            iface.setBridged(bridgedNetwork);
            setGatewayIfNecessary(iface, host, net.getGateway());

            if (bridgedNetwork) {
                Map<String, Object> networkConfig = (Map<String, Object>) network.get("cfg");
                addBootProtocol(networkConfig, host, iface);
            }

            HostNetworkQosMapper qosMapper = new HostNetworkQosMapper(network);
            iface.setQos(qosMapper.deserialize());
        }
    }

    /**
     * Returns true if vdsm doesn't report the 'bridged' attribute or if reported - its actual value.<br>
     * The assumption is bridge-less network isn't supported if the 'bridged' attribute wasn't reported.<br>
     * Bridge-less networks must report 'false' for this property.
     *
     * @param network
     *            The network to evaluate its bridge attribute
     * @return true is no attribute is reported or its actual value
     */
    private static boolean isBridgedNetwork(Map<String, Object> network) {
        return network.get("bridged") == null || Boolean.parseBoolean(network.get("bridged").toString());
    }

    // we check for old bonding options,
    // if we had value for the bonding options, i.e. the user set it by the UI
    // and we have host that is not returning it's bonding options(host below 2.2.4) we override
    // the "new" bonding options with the old one only if we have the new one as null and the old one is not
    private static void setBondingOptions(VDS vds, List<VdsNetworkInterface> oldInterfaces) {
        for (VdsNetworkInterface iface : oldInterfaces) {
            if (iface.getBondOptions() != null) {
                for (VdsNetworkInterface newIface : vds.getInterfaces()) {
                    if (iface.getName().equals(newIface.getName()) && newIface.getBondOptions() == null) {
                        newIface.setBondOptions(iface.getBondOptions());
                        break;
                    }
                }
            }
        }
    }

    private static void addBootProtocol(Map<String, Object> cfg, VDS host, VdsNetworkInterface iface) {
        NetworkBootProtocol bootproto = NetworkBootProtocol.NONE;

        if (cfg != null) {
            String bootProtocol = (String) cfg.get("BOOTPROTO");

            if (bootProtocol != null) {
                if (bootProtocol.toLowerCase().equals("dhcp")) {
                    bootproto = NetworkBootProtocol.DHCP;
                } else if (bootProtocol.toLowerCase().equals("none") || bootProtocol.toLowerCase().equals("static")) {
                    if (StringUtils.isNotEmpty((String) cfg.get("IPADDR"))) {
                        bootproto = NetworkBootProtocol.STATIC_IP;
                    }
                }
            } else if (StringUtils.isNotEmpty((String) cfg.get("IPADDR"))) {
                bootproto = NetworkBootProtocol.STATIC_IP;
            }

            if (bootproto == NetworkBootProtocol.STATIC_IP) {
                String gateway = (String) cfg.get(VdsProperties.GATEWAY);
                if (StringUtils.isNotEmpty(gateway)) {
                    setGatewayIfNecessary(iface, host, gateway.toString());
                }
            }
        }
        iface.setBootProtocol(bootproto);
    }

    private static void addBondDeviceToHost(VDS vds, VdsNetworkInterface iface, Object[] interfaces) {
        vds.getInterfaces().add(iface);
        if (interfaces != null) {
            for (Object name : interfaces) {
                for (VdsNetworkInterface tempInterface : vds.getInterfaces()) {
                    if (tempInterface.getName().equals(name.toString())) {
                        tempInterface.setBondName(iface.getName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Store the gateway for either of these cases:
     * 1. any host network, in a cluster that supports multiple gateways
     * 2. management network, no matter the cluster compatibility version
     * 3. the active interface (could happen when there is no management network yet)
     * If gateway was provided for non-management network when multiple gateways aren't supported, its value should be ignored.
     *
     * @param iface
     *            the host network interface
     * @param host
     *            the host whose interfaces are being edited
     * @param gateway
     *            the gateway value to be set
     */
    private static void setGatewayIfNecessary(VdsNetworkInterface iface, VDS host, String gateway) {
        if (FeatureSupported.multipleGatewaysSupported(host.getVdsGroupCompatibilityVersion())
                || NetworkUtils.getEngineNetwork().equals(iface.getNetworkName())
                || iface.getName().equals(host.getActiveNic())) {
            iface.setGateway(gateway);
        }
    }

    /**
     * Creates a list of {@link VmGuestAgentInterface} from the {@link VdsProperties.GuestNetworkInterfaces}
     *
     * @param vmId
     *            the Vm's ID which contains the interfaces
     *
     * @param xmlRpcStruct
     *            the xml structure that describes the VM as reported by VDSM
     * @return a list of {@link VmGuestAgentInterface} or null if no guest vNics were reported
     */
    public static List<VmGuestAgentInterface> buildVmGuestAgentInterfacesData(Guid vmId, Map<String, Object> xmlRpcStruct) {
        if (!xmlRpcStruct.containsKey(VdsProperties.VM_NETWORK_INTERFACES)) {
            return null;
        }

        List<VmGuestAgentInterface> interfaces = new ArrayList<VmGuestAgentInterface>();
        for (Object ifaceStruct : (Object[]) xmlRpcStruct.get(VdsProperties.VM_NETWORK_INTERFACES)) {
            VmGuestAgentInterface nic = new VmGuestAgentInterface();
            Map ifaceMap = (Map) ifaceStruct;
            nic.setInterfaceName(AssignStringValue(ifaceMap, VdsProperties.VM_INTERFACE_NAME));
            nic.setMacAddress(getMacAddress(ifaceMap));
            nic.setIpv4Addresses(extracStringtList(ifaceMap, VdsProperties.VM_IPV4_ADDRESSES));
            nic.setIpv6Addresses(extracStringtList(ifaceMap, VdsProperties.VM_IPV6_ADDRESSES));
            nic.setVmId(vmId);
            interfaces.add(nic);
        }
        return interfaces;
    }

    private static String getMacAddress(Map<String, Object> ifaceMap) {
        String macAddress = AssignStringValue(ifaceMap, VdsProperties.VM_INTERFACE_MAC_ADDRESS);
        return macAddress != null ? macAddress.replace('-', ':') : null;
    }

    /**
     * Build through the received NUMA nodes information
     * @param vds
     * @param xmlRpcStruct
     */
    private static void updateNumaNodesData(VDS vds, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.AUTO_NUMA)) {
            vds.getDynamicData().setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(
                    AssignIntValue(xmlRpcStruct, VdsProperties.AUTO_NUMA)));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.NUMA_NODES)) {
            Map<String, Map<String, Object>> numaNodeMap =
                    (Map<String, Map<String, Object>>) xmlRpcStruct.get(VdsProperties.NUMA_NODES);
            Map<String, Object> numaNodeDistanceMap =
                    (Map<String, Object>) xmlRpcStruct.get(VdsProperties.NUMA_NODE_DISTANCE);

            List<VdsNumaNode> newNumaNodeList = new ArrayList<>(numaNodeMap.size());

            for (Map.Entry<String, Map<String, Object>> item : numaNodeMap.entrySet()) {
                int index = Integer.valueOf(item.getKey());
                Map<String, Object> itemMap = item.getValue();
                List<Integer> cpuIds = extractIntegerList(itemMap, VdsProperties.NUMA_NODE_CPU_LIST);
                long memTotal =  AssignLongValue(itemMap, VdsProperties.NUMA_NODE_TOTAL_MEM);
                VdsNumaNode numaNode = new VdsNumaNode();
                numaNode.setIndex(index);
                if (cpuIds != null) {
                    numaNode.setCpuIds(cpuIds);
                }
                numaNode.setMemTotal(memTotal);
                newNumaNodeList.add(numaNode);
            }

            Collections.sort(newNumaNodeList, numaNodeComparator);

            for (VdsNumaNode vdsNumaNode : newNumaNodeList) {
                int index = vdsNumaNode.getIndex();
                List<Integer> distances = extractIntegerList(numaNodeDistanceMap, String.valueOf(index));
                Map<Integer, Integer> distanceMap = new HashMap<>(distances.size());
                for (int i = 0; i < distances.size(); i++) {
                    distanceMap.put(newNumaNodeList.get(i).getIndex(), distances.get(i));
                }
                VdsNumaNode newNumaNode = NumaUtils.getVdsNumaNodeByIndex(newNumaNodeList, index);
                if (newNumaNode != null) {
                    newNumaNode.setNumaNodeDistances(distanceMap);
                }
            }

            vds.getDynamicData().setNumaNodeList(newNumaNodeList);
            vds.setNumaSupport(newNumaNodeList.size() > 1);
        }

    }

    /**
     * Build through the received vm NUMA nodes runtime information
     * @param vm
     * @param xmlRpcStruct
     */
    private static void updateVmNumaNodesRuntimeInfo(VmStatistics vm, Map<String, Object> xmlRpcStruct) {
        Map<String, Object[]> vNodesRunInfo = (Map<String, Object[]>)xmlRpcStruct.get(
                VdsProperties.VM_NUMA_NODES_RUNTIME_INFO);
        for (Map.Entry<String, Object[]> item : vNodesRunInfo.entrySet()) {
            VmNumaNode vNode = new VmNumaNode();
            vNode.setIndex(Integer.valueOf(item.getKey()));
            for (Object pNodeIndex : item.getValue()) {
                vNode.getVdsNumaNodeList().add(new Pair<>(
                        Guid.Empty, new Pair<>(false, (Integer)pNodeIndex)));
            }
            vm.getvNumaNodeStatisticsList().add(vNode);
        }
    }

    private static List<String> extracStringtList(Map<String, Object> xmlRpcStruct, String propertyName) {
        if (!xmlRpcStruct.containsKey(propertyName)){
            return null;
        }

        Object[] items = (Object[]) xmlRpcStruct.get(propertyName);
        if (items.length == 0) {
            return null;
        }

        List<String> list = new ArrayList<String>();
        for (Object item : items) {
            list.add((String) item);
        }
        return list;
    }

    private static List<Integer> extractIntegerList(Map<String, Object> xmlRpcStruct, String propertyName) {
        if (!xmlRpcStruct.containsKey(propertyName)){
            return null;
        }

        Object[] items = (Object[]) xmlRpcStruct.get(propertyName);
        if (items.length == 0) {
            return null;
        }

        List<Integer> list = new ArrayList<Integer>();
        for (Object item : items) {
            list.add((Integer) item);
        }
        return list;
    }
}
