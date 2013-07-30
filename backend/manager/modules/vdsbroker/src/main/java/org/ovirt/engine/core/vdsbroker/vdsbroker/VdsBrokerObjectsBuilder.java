package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
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
import org.ovirt.engine.core.compat.FormatException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class encapsulate the knowledge of how to create objects from the VDS RPC protocol response.
 * This class has methods that receive XmlRpcStruct and construct the following Classes: VmDynamic VdsDynamic VdsStatic.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VdsBrokerObjectsBuilder {
    private final static int VNC_START_PORT = 5900;
    private final static double NANO_SECONDS = 1000000000;

    public static VmDynamic buildVMDynamicDataFromList(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmdynamic = new VmDynamic();
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            try {
                vmdynamic.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vmdynamic.setId(Guid.Empty);
            }
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
            sPool.setStorageType(StorageType.valueOf(xmlRpcStruct.get("type").toString()));
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

    public static void updateVMDynamicData(VmDynamic vm, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            try {
                vm.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vm.setId(Guid.Empty);
            }
        }
        if (xmlRpcStruct.containsKey(VdsProperties.session)) {
            String session = (String) xmlRpcStruct.get(VdsProperties.session);
            try {
                vm.setSession(SessionState.valueOf(session));
            } catch (Exception e) {
                log.errorFormat("vm session value illegal : {0}", session);
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
        if (xmlRpcStruct.containsKey(VdsProperties.display_port)) {
            try {
                vm.setDisplay(Integer.parseInt(xmlRpcStruct.get(VdsProperties.display_port).toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display_port value illegal : {0}", xmlRpcStruct.get(VdsProperties.display_port));
            }
        } else if (xmlRpcStruct.containsKey(VdsProperties.display)) {
            try {
                vm.setDisplay(VNC_START_PORT + Integer.parseInt(xmlRpcStruct.get(VdsProperties.display).toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display value illegal : {0}", xmlRpcStruct.get(VdsProperties.display));
            }
        }
        if (xmlRpcStruct.containsKey(VdsProperties.display_secure_port)) {
            try {
                vm.setDisplaySecurePort(Integer.parseInt(xmlRpcStruct.get(VdsProperties.display_secure_port)
                        .toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display_secure_port value illegal : {0}",
                        xmlRpcStruct.get(VdsProperties.display_secure_port));
            }
        }
        if (xmlRpcStruct.containsKey((VdsProperties.displayType))) {
            String displayType = xmlRpcStruct.get(VdsProperties.displayType).toString();
            try {
                vm.setDisplayType(DisplayType.valueOf(displayType));

            } catch (Exception e2) {
                log.errorFormat("vm display type value illegal : {0}", displayType);
            }
        }
        if (xmlRpcStruct.containsKey((VdsProperties.displayIp))) {
            vm.setDisplayIp((String) xmlRpcStruct.get(VdsProperties.displayIp));
        }

        if (xmlRpcStruct.containsKey((VdsProperties.utc_diff))) {
            String utc_diff = xmlRpcStruct.get(VdsProperties.utc_diff).toString();
            if (utc_diff.startsWith("+")) {
                utc_diff = utc_diff.substring(1);
            }
            try {
                vm.setUtcDiff(Integer.parseInt(utc_diff));
            } catch (NumberFormatException e) {
                log.errorFormat("vm offset (utc_diff) value illegal : {0}", utc_diff);
            }
        }

        if (xmlRpcStruct.containsKey(VdsProperties.hash)) {
            String hash = (String) xmlRpcStruct.get(VdsProperties.hash);
            try {
                vm.setHash(hash);
            } catch (Exception e) {
                log.errorFormat("vm hash value illegal : {0}", hash);
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
    }

    public static void updateVMStatisticsData(VmStatistics vm, Map<String, Object> xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.vm_guid)) {
            try {
                vm.setId(new Guid((String) xmlRpcStruct.get(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vm.setId(Guid.Empty);
            }
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

    public static void updateVDSDynamicData(VDS vds, Map<String, Object> xmlRpcStruct) {
        updateNetworkData(vds, xmlRpcStruct);

        vds.setCpuThreads(AssignIntValue(xmlRpcStruct, VdsProperties.cpuThreads));
        vds.setCpuCores(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_cores));
        vds.setCpuSockets(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_sockets));
        vds.setCpuModel(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_model));
        vds.setCpuSpeedMh(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_speed_mh));
        vds.setPhysicalMemMb(AssignIntValue(xmlRpcStruct, VdsProperties.physical_mem_mb));

        vds.setKvmEnabled(AssignBoolValue(xmlRpcStruct, VdsProperties.kvm_enabled));

        vds.setReservedMem(AssignIntValue(xmlRpcStruct, VdsProperties.reservedMem));
        Integer guestOverhead = AssignIntValue(xmlRpcStruct, VdsProperties.guestOverhead);
        vds.setGuestOverhead(guestOverhead != null ? guestOverhead : 0);

        vds.setCpuFlags(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_flags));

        UpdatePackagesVersions(vds, xmlRpcStruct);

        vds.setSupportedClusterLevels(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_cluster_levels));
        vds.setSupportedEngines(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_engines));
        vds.setIScsiInitiatorName(AssignStringValue(xmlRpcStruct, VdsProperties.iSCSIInitiatorName));

        vds.setSupportedEmulatedMachines(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.emulatedMachines));

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
                devicesList.add((Map<String,String>)device);
            }

            hbas.put(el.getKey(), devicesList);
        }
        vds.setHBAs(hbas);
    }

    public static void checkTimeDrift(VDS vds, Map<String, Object> xmlRpcStruct) {
        Boolean isHostTimeDriftEnabled = Config.GetValue(ConfigValues.EnableHostTimeDrift);
        if (isHostTimeDriftEnabled) {
            Integer maxTimeDriftAllowed = Config.GetValue(ConfigValues.HostTimeDriftInSec);
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
                vds.setVersion(getPackageRpmVersion("vdsm",vdsm));
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

    public static void UpdateHardwareSystemInformation(Map<String, Object> hwInfo, VDS vds){
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
        Map<String, Object> interfaces = (Map<String, Object>) xmlRpcStruct
                .get(VdsProperties.NETWORK);
        if (interfaces != null) {
            int networkUsage = 0;
            for (Entry<String, Object> entry : interfaces.entrySet()) {
                VdsNetworkInterface iface = null;
                for (VdsNetworkInterface tempInterface : vds.getInterfaces()) {
                    if (tempInterface.getName().equals(entry.getKey())) {
                        iface = tempInterface;
                        break;
                    }
                }
                if (iface != null) {
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

                    int hold =
                            (iface.getStatistics().getTransmitRate().compareTo(iface.getStatistics().getReceiveRate()) > 0 ? iface.getStatistics()
                                    .getTransmitRate()
                                    : iface
                                            .getStatistics().getReceiveRate()).intValue();
                    if (hold > networkUsage) {
                        networkUsage = hold;
                    }
                }
            }
            vds.setUsageNetworkPercent((networkUsage > 100) ? 100 : networkUsage);
        }

        // ----------- vds cpu statistics info ---------------------
        vds.setCpuSys(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_sys));
        vds.setCpuUser(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_user));
        if (vds.getCpuSys() != null && vds.getCpuUser() != null) {
            vds.setUsageCpuPercent((int) (vds.getCpuSys() + vds.getCpuUser()));
            if (vds.getUsageCpuPercent() >= vds.getHighUtilization()
                    || vds.getUsageCpuPercent() <= vds.getLowUtilization()) {
                if (vds.getCpuOverCommitTimestamp() == null) {
                    vds.setCpuOverCommitTimestamp(new Date());
                }
            } else {
                vds.setCpuOverCommitTimestamp(null);
            }
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
                    log.error("failed building domains", e);
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
                String errMsg = String.format("Failed to parse %1$s value %2$s to integer", name, stringValue);
                log.error(errMsg, nfe);
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
                log.errorFormat("Failed to parse {0} value {1} to long", name, stringValue);
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
            String msg = String.format("VdsBroker::AssignDateTImeFromEpoch - failed to convert field %1$s to dateTime",
                    name);
            log.warn(msg, ex);
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
                log.errorFormat("Vm status: {0} illegal", statusName);
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

        Map<String, Integer> currVlans = addHostVlanDevices(vds, xmlRpcStruct);

        addHostBondDevices(vds, xmlRpcStruct);

        addHostNetworksAndUpdateInterfaces(vds, xmlRpcStruct, currVlans);

        // set bonding options
        setBondingOptions(vds, oldInterfaces);

        // This information was added in 3.1, so don't use it if it's not there.
        if (xmlRpcStruct.containsKey(VdsProperties.netConfigDirty)) {
            vds.setNetConfigDirty(AssignBoolValue(xmlRpcStruct, VdsProperties.netConfigDirty));
        }
    }

    private static void addHostNetworksAndUpdateInterfaces(VDS vds,
            Map<String, Object> xmlRpcStruct,
            Map<String, Integer> currVlans) {

        Map<String, Integer> networkVlans = new HashMap<String, Integer>();

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
                                currVlans,
                                networkVlans,
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
        if (interfaces.size() != 1) {
            AuditLogableBase logable = new AuditLogableBase(vds.getId());
            logable.addCustomValue("NetworkName", network.getName());
            logable.addCustomValue("Interfaces", StringUtils.join(Entities.objectNames(interfaces), ","));
            AuditLogDirector.log(logable, AuditLogType.BRIDGED_NETWORK_OVER_MULTIPLE_INTERFACES);
        }
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
     * @return a map of the added vlan device names and their vlan tag
     */
    private static Map<String, Integer> addHostVlanDevices(VDS vds, Map<String, Object> xmlRpcStruct) {
        // interface to vlan map
        Map<String, Integer> currVlans = new HashMap<String, Integer>();

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

                if (vlanDeviceName.contains(".")) {
                    String[] names = vlanDeviceName.split("[.]", -1);
                    String vlan = names[1];
                    iface.setVlanId(Integer.parseInt(vlan));
                    currVlans.put(vlanDeviceName, iface.getVlanId());
                }

                Map<String, Object> vlan = (Map<String, Object>) entry.getValue();

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
        return currVlans;
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
            if (nic.get("permhwaddr") != null) {
                iface.setMacAddress((String) nic.get("permhwaddr"));
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
     * @param currVlans
     *            Used for checking the VLANs later.
     * @param networkVlans
     *            Used for checking the VLANs later.
     * @param network
     *            Network struct to get details from.
     * @param net
     *            Network to get details from.
     */
    private static void updateNetworkDetailsInInterface(VdsNetworkInterface iface,
            Map<String, Integer> currVlans,
            Map<String, Integer> networkVlans,
            Map<String, Object> network,
            VDS host,
            Network net) {

        if (iface != null) {
            iface.setNetworkName(net.getName());

            if (currVlans.containsKey(iface.getName())) {
                networkVlans.put(net.getName(), currVlans.get(iface.getName()));
            }

            // set the management ip
            if (StringUtils.equals(iface.getNetworkName(), NetworkUtils.getEngineNetwork())) {
                iface.setType(iface.getType() | VdsInterfaceType.MANAGEMENT.getValue());
            }

            iface.setAddress(net.getAddr());
            iface.setSubnet(net.getSubnet());
            boolean bridgedNetwork = isBridgedNetwork(network);
            iface.setBridged(bridgedNetwork);
            setGatewayIfManagementNetwork(iface, host, net.getGateway());

            if (bridgedNetwork) {
                Map<String, Object> networkConfig = (Map<String, Object>) network.get("cfg");
                addBootProtocol(networkConfig, host, iface);
            }
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
                    setGatewayIfManagementNetwork(iface, host, gateway.toString());
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
     * Store the gateway for management network or the active interface only (could happen when there is no management
     * network yet). If gateway was provided for non-management network, its value should be ignored.
     *
     * @param iface
     *            the host network interface
     * @param gateway
     *            the gateway value to be set
     */
    private static void setGatewayIfManagementNetwork(VdsNetworkInterface iface, VDS host, String gateway) {
        if (NetworkUtils.getEngineNetwork().equals(iface.getNetworkName())
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

    private static final Log log = LogFactory.getLog(VdsBrokerObjectsBuilder.class);
}
