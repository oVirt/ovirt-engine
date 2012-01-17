package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsInterfaceType;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VdsVersion;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.BooleanCompat;
import org.ovirt.engine.core.compat.FormatException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.LongCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

/**
 * This class encapsulate the knowlage of how to create objects from the VDS Rpc protocol responce. This class has
 * methods that receive XmlRpcStruct and construct the following Classes: VmDynamic VdsDynamic VdsStatic
 */
@SuppressWarnings({ "unchecked", "unchecked", "unchecked" })
public class VdsBrokerObjectsBuilder {
    private final static int VNC_START_PORT = 5900;
    private final static double NANO_SECONDS = 1000000000;

    public static VmDynamic buildVMDynamicDataFromList(XmlRpcStruct xmlRpcStruct) {
        VmDynamic vmdynamic = new VmDynamic();
        if (xmlRpcStruct.contains(VdsProperties.vm_guid)) {
            try {
                vmdynamic.setId(new Guid((String) xmlRpcStruct.getItem(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vmdynamic.setId(new Guid());
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.status)) {
            vmdynamic.setstatus(convertToVmStatus((String) xmlRpcStruct.getItem(VdsProperties.status)));
        }
        return vmdynamic;
    }

    public static VmDynamic buildVMDynamicData(XmlRpcStruct xmlRpcStruct) {
        VmDynamic vmdynamic = new VmDynamic();
        updateVMDynamicData(vmdynamic, xmlRpcStruct);
        return vmdynamic;
    }

    public static storage_pool buildStoragePool(XmlRpcStruct xmlRpcStruct) {
        storage_pool sPool = new storage_pool();
        if (xmlRpcStruct.contains("type")) {
            sPool.setstorage_pool_type(StorageType.valueOf(xmlRpcStruct.getItem("type").toString()));
        }
        sPool.setname(AssignStringValue(xmlRpcStruct, "name"));
        Integer masterVersion = AssignIntValue(xmlRpcStruct, "master_ver");
        if (masterVersion != null) {
            sPool.setmaster_domain_version(masterVersion);
        }
        return sPool;
    }

    public static VmStatistics buildVMStatisticsData(XmlRpcStruct xmlRpcStruct) {
        VmStatistics vmStatistics = new VmStatistics();
        updateVMStatisticsData(vmStatistics, xmlRpcStruct);
        return vmStatistics;
    }

    public static void updateVMDynamicData(VmDynamic vm, XmlRpcStruct xmlRpcStruct) {
        if (xmlRpcStruct.contains(VdsProperties.vm_guid)) {
            try {
                vm.setId(new Guid((String) xmlRpcStruct.getItem(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vm.setId(new Guid());
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.session)) {
            String session = (String) xmlRpcStruct.getItem(VdsProperties.session);
            try {
                vm.setsession(SessionState.valueOf(session));
            } catch (java.lang.Exception e) {
                log.errorFormat("vm session value illegal : {0}", session);
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.kvmEnable)) {
            boolean enabled = false;
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(enabled);
            if (BooleanCompat.TryParse((String) xmlRpcStruct.getItem(VdsProperties.kvmEnable), tempRefObject)) {
                vm.setkvm_enable(tempRefObject.argvalue);
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.acpiEnable)) {
            boolean enabled = false;
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(enabled);
            if (BooleanCompat.TryParse((String) xmlRpcStruct.getItem(VdsProperties.acpiEnable), tempRefObject)) {
                vm.setacpi_enable(tempRefObject.argvalue);
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.win2kHackEnable)) {
            boolean enabled = false;
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(enabled);
            if (BooleanCompat.TryParse((String) xmlRpcStruct.getItem(VdsProperties.win2kHackEnable), tempRefObject)) {
                vm.setWin2kHackEnable(tempRefObject.argvalue);
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.status)) {
            vm.setstatus(convertToVmStatus((String) xmlRpcStruct.getItem(VdsProperties.status)));
        }
        if (xmlRpcStruct.contains(VdsProperties.display_port)) {
            try {
                vm.setdisplay(Integer.parseInt(xmlRpcStruct.getItem(VdsProperties.display_port).toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display_port value illegal : {0}", xmlRpcStruct.getItem(VdsProperties.display_port));
            }
        } else if (xmlRpcStruct.contains(VdsProperties.display)) {
            try {
                vm.setdisplay(VNC_START_PORT + Integer.parseInt(xmlRpcStruct.getItem(VdsProperties.display).toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display value illegal : {0}", xmlRpcStruct.getItem(VdsProperties.display));
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.display_secure_port)) {
            try {
                vm.setdisplay_secure_port(Integer.parseInt(xmlRpcStruct.getItem(VdsProperties.display_secure_port)
                        .toString()));
            } catch (NumberFormatException e) {
                log.errorFormat("vm display_secure_port value illegal : {0}",
                        xmlRpcStruct.getItem(VdsProperties.display_secure_port));
            }
        }
        if (xmlRpcStruct.contains((VdsProperties.displayType))) {
            String displayType = xmlRpcStruct.getItem(VdsProperties.displayType).toString();
            try {
                vm.setdisplay_type(DisplayType.valueOf(displayType));

            } catch (java.lang.Exception e2) {
                log.errorFormat("vm display type value illegal : {0}", displayType);
            }
        }
        if (xmlRpcStruct.contains((VdsProperties.displayIp))) {
            vm.setdisplay_ip((String) xmlRpcStruct.getItem(VdsProperties.displayIp));
        }

        if (xmlRpcStruct.contains((VdsProperties.utc_diff))) {
            String utc_diff = xmlRpcStruct.getItem(VdsProperties.utc_diff).toString();
            if (utc_diff.startsWith("+")) {
                utc_diff = utc_diff.substring(1);
            }
            try {
                vm.setutc_diff(Integer.parseInt(utc_diff));
            } catch (NumberFormatException e) {
                log.errorFormat("vm offset (utc_diff) value illegal : {0}", utc_diff);
            }
        }

        /**
         * vm disks
         */
        if (xmlRpcStruct.contains(VdsProperties.vm_disks)) {
            initDisks(xmlRpcStruct, vm);
        }

        // ------------- vm internal agent data
        vm.setguest_cur_user_name(AssignStringValue(xmlRpcStruct, VdsProperties.guest_cur_user_name));
        vm.setguest_last_login_time(AssignDateTImeFromEpoch(xmlRpcStruct, VdsProperties.guest_last_login_time));
        // vm.guest_last_logout_time = AssignDateTImeFromEpoch(xmlRpcStruct,
        // VdsProperties.guest_last_logout_time);
        vm.setvm_host(AssignStringValue(xmlRpcStruct, VdsProperties.vm_host));

        initAppsList(xmlRpcStruct, vm);
        vm.setguest_os(AssignStringValue(xmlRpcStruct, VdsProperties.guest_os));
        vm.setvm_ip(AssignStringValue(xmlRpcStruct, VdsProperties.vm_ip));
        if (vm.getvm_ip() != null) {
            if (vm.getvm_ip().startsWith("127.0.")) {
                vm.setvm_ip(null);
            } else {
                vm.setvm_ip(vm.getvm_ip().trim());
            }
        }

        if (xmlRpcStruct.contains(VdsProperties.exit_code)) {
            String exitCodeStr = xmlRpcStruct.getItem(VdsProperties.exit_code).toString();
            vm.setExitStatus(VmExitStatus.forValue(Integer.parseInt(exitCodeStr)));
        }
        if (xmlRpcStruct.contains(VdsProperties.exit_message)) {
            String exitMsg = (String) xmlRpcStruct.getItem(VdsProperties.exit_message);
            vm.setExitMessage(exitMsg);
        }

        // if monitorResponse returns negative it means its erroneous
        if (xmlRpcStruct.contains(VdsProperties.monitorResponse)) {
            int response = Integer.parseInt(xmlRpcStruct.getItem(VdsProperties.monitorResponse).toString());
            if (response < 0) {
                vm.setstatus(VMStatus.NotResponding);
            }
        }
        if (xmlRpcStruct.contains(VdsProperties.clientIp)) {
            vm.setclient_ip(xmlRpcStruct.getItem(VdsProperties.clientIp).toString());
        }

        VmPauseStatus pauseStatus = VmPauseStatus.NONE;
        if (xmlRpcStruct.contains(VdsProperties.pauseCode)) {
            String pauseCodeStr = (String) xmlRpcStruct.getItem(VdsProperties.pauseCode);
            try {
                pauseStatus = VmPauseStatus.valueOf(pauseCodeStr);

            } catch (IllegalArgumentException ex) {
                log.error("Error in parsing vm pause status. Setting value to NONE");
                pauseStatus = VmPauseStatus.NONE;
            }
        }
        vm.setPauseStatus(pauseStatus);
    }

    public static void updateVMStatisticsData(VmStatistics vm, XmlRpcStruct xmlRpcStruct) {
        if (xmlRpcStruct.contains(VdsProperties.vm_guid)) {
            try {
                vm.setId(new Guid((String) xmlRpcStruct.getItem(VdsProperties.vm_guid)));
            } catch (FormatException e) {
                log.info("vm id is not in uuid format, ", e);
                vm.setId(new Guid());
            }
        }

        vm.setelapsed_time(AssignDoubleValue(xmlRpcStruct, VdsProperties.elapsed_time));

        // ------------- vm network statistics -----------------------
        if (xmlRpcStruct.containsKey(VdsProperties.vm_network)) {
            java.util.Map networkStruct =
                    (java.util.Map) xmlRpcStruct.getItem(VdsProperties.vm_network);
            vm.setInterfaceStatistics(new java.util.ArrayList<VmNetworkInterface>());
            for (Object tempNic : networkStruct.values()) {
                XmlRpcStruct nic = new XmlRpcStruct((java.util.Map) tempNic);
                VmNetworkInterface stats = new VmNetworkInterface();
                vm.getInterfaceStatistics().add(stats);

                if (nic.containsKey(VdsProperties.if_name)) {
                    stats.setName((String) ((nic.getItem(VdsProperties.if_name) instanceof String) ? nic
                            .getItem(VdsProperties.if_name) : null));
                }
                Double rx_rate = AssignDoubleValue(nic, VdsProperties.rx_rate);
                Double rx_dropped = AssignDoubleValue(nic, VdsProperties.rx_dropped);
                Double tx_rate = AssignDoubleValue(nic, VdsProperties.tx_rate);
                Double tx_dropped = AssignDoubleValue(nic, VdsProperties.tx_dropped);
                stats.getStatistics().setReceiveRate(rx_rate != null ? rx_rate : 0);
                stats.getStatistics().setReceiveDropRate(rx_dropped != null ? rx_dropped : 0);
                stats.getStatistics().setTransmitRate(tx_rate != null ? tx_rate : 0);
                stats.getStatistics().setTransmitDropRate(tx_dropped != null ? tx_dropped : 0);
                stats.setMacAddress((String) ((nic.getItem(VdsProperties.mac_addr) instanceof String) ? nic
                        .getItem(VdsProperties.mac_addr) : null));
                stats.setSpeed(AssignIntValue(nic, VdsProperties.if_speed));
            }
        }

        if (xmlRpcStruct.contains(VdsProperties.VM_DISKS_USAGE)) {
            initDisksUsage(xmlRpcStruct, vm);
        }

        // ------------- vm cpu statistics -----------------------
        vm.setcpu_sys(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_sys));
        vm.setcpu_user(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_user));

        // ------------- vm memory statistics -----------------------
        vm.setusage_mem_percent(AssignIntValue(xmlRpcStruct, VdsProperties.vm_usage_mem_percent));

    }

    public static void updateVDSDynamicData(VDS vds, XmlRpcStruct xmlRpcStruct) {
        updateNetworkData(vds, xmlRpcStruct);

        vds.setcpu_cores(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_cores));
        vds.setcpu_sockets(AssignIntValue(xmlRpcStruct, VdsProperties.cpu_sockets));
        vds.setcpu_model(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_model));
        vds.setcpu_speed_mh(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_speed_mh));
        vds.setphysical_mem_mb(AssignIntValue(xmlRpcStruct, VdsProperties.physical_mem_mb));

        vds.setkvm_enabled(AssignBoolValue(xmlRpcStruct, VdsProperties.kvm_enabled));

        vds.setreserved_mem(AssignIntValue(xmlRpcStruct, VdsProperties.reservedMem));
        Integer guestOverhead = AssignIntValue(xmlRpcStruct, VdsProperties.guestOverhead);
        vds.setguest_overhead(guestOverhead != null ? guestOverhead : 0);
        updateVdsStaticVersion(vds, xmlRpcStruct);

        vds.setcpu_flags(AssignStringValue(xmlRpcStruct, VdsProperties.cpu_flags));

        UpdatePackagesVersions(vds, xmlRpcStruct);

        // ----------- vm statistic data ---------------------------
        //
        // vds.destroy_rate = AssignDecimalValue(xmlRpcStruct,
        // VdsProperties.destroy_rate);
        // vds.destroy_total = AssignIntValue(xmlRpcStruct,
        // VdsProperties.destroy_total);
        // vds.launch_rate = AssignDecimalValue(xmlRpcStruct,
        // VdsProperties.launch_rate);
        // vds.launch_total = AssignIntValue(xmlRpcStruct,
        // VdsProperties.launch_total);
        //

        vds.setsupported_cluster_levels(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_cluster_levels));
        vds.setsupported_engines(AssignStringValueFromArray(xmlRpcStruct, VdsProperties.supported_engines));
        vds.setIScsiInitiatorName(AssignStringValue(xmlRpcStruct, VdsProperties.iSCSIInitiatorName));

        String hooksStr = ""; // default value if hooks is not in the xml rpc struct
        if (xmlRpcStruct.containsKey(VdsProperties.hooks)) {
            hooksStr = xmlRpcStruct.getItem(VdsProperties.hooks).toString();
        }
        vds.setHooksStr(hooksStr);
    }

    private static void initDisksUsage(XmlRpcStruct vmStruct, VmStatistics vm) {
        Object[] vmDisksUsage = (Object[]) vmStruct.getItem(VdsProperties.VM_DISKS_USAGE);
        if (vmDisksUsage != null) {
            ArrayList<Object> disksUsageList = new ArrayList<Object>(Arrays.asList(vmDisksUsage));
            vm.setDisksUsage(new JsonObjectSerializer().serializeUnformattedJson(disksUsageList));
        }
    }

    private static void UpdatePackagesVersions(VDS vds, XmlRpcStruct xmlRpcStruct) {
        if (xmlRpcStruct.contains(VdsProperties.host_os)) {
            vds.sethost_os(GetPackageVersionFormated(
                    new XmlRpcStruct((java.util.Map) xmlRpcStruct.getItem(VdsProperties.host_os)), true));
        }
        if (xmlRpcStruct.contains(VdsProperties.packages)) {
            // packages is an array of xmlRpcStruct (that each is a name, ver,
            // release.. of a package)
            for (Object hostPackageMap : (Object[]) xmlRpcStruct.getItem(VdsProperties.packages)) {
                XmlRpcStruct hostPackage = new XmlRpcStruct((java.util.Map) hostPackageMap);
                String packageName = AssignStringValue(hostPackage, VdsProperties.package_name);
                if (StringHelper.EqOp(packageName, VdsProperties.kvmPackageName)) {
                    vds.setkvm_version(GetPackageVersionFormated(hostPackage, false));
                } else if (StringHelper.EqOp(packageName, VdsProperties.spicePackageName)) {
                    vds.setspice_version(GetPackageVersionFormated(hostPackage, false));
                } else if (StringHelper.EqOp(packageName, VdsProperties.kernelPackageName)) {
                    vds.setkernel_version(GetPackageVersionFormated(hostPackage, false));
                }
            }
        } else if (xmlRpcStruct.contains(VdsProperties.packages2)) {
            Map packages = (Map) xmlRpcStruct.getItem(VdsProperties.packages2);

            if (packages.containsKey(VdsProperties.qemuKvmPackageName)) {
                Map kvm = (Map) packages.get(VdsProperties.qemuKvmPackageName);
                vds.setkvm_version(getPackageVersionFormated2(kvm));
            }
            if (packages.containsKey(VdsProperties.spiceServerPackageName)) {
                Map spice = (Map) packages.get(VdsProperties.spiceServerPackageName);
                vds.setspice_version(getPackageVersionFormated2(spice));
            }
            if (packages.containsKey(VdsProperties.kernelPackageName)) {
                Map kernel = (Map) packages.get(VdsProperties.kernelPackageName);
                vds.setkernel_version(getPackageVersionFormated2(kernel));
            }
        }
    }

    // Version 2 of GetPackageVersionFormated2:
    // from 2.3 we get dictionary and not a flat list.
    // from now the packages names (of spice, kernel, qemu and libvirt) are the same as far as VDSM and ENGINE.
    // (VDSM use to report packages name of rpm so in RHEL6 when it change it broke our interface)
    private static String getPackageVersionFormated2(Map hostPackage) {

        String packageVersion = (hostPackage.get(VdsProperties.package_version) != null) ? (String) hostPackage
                .get(VdsProperties.package_version) : null;
        String packageRelease = (hostPackage.get(VdsProperties.package_release) != null) ? (String) hostPackage
                        .get(VdsProperties.package_release) : null;

        StringBuilder sb = new StringBuilder();
        if (!StringHelper.isNullOrEmpty(packageVersion)) {
            sb.append(packageVersion);
        }
        if (!StringHelper.isNullOrEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    private static String GetPackageVersionFormated(XmlRpcStruct hostPackage, boolean getName) {
        String packageName = AssignStringValue(hostPackage, VdsProperties.package_name);
        String packageVersion = AssignStringValue(hostPackage, VdsProperties.package_version);
        String packageRelease = AssignStringValue(hostPackage, VdsProperties.package_release);
        StringBuilder sb = new StringBuilder();
        if (!StringHelper.isNullOrEmpty(packageName) && getName) {
            sb.append(packageName);
        }
        if (!StringHelper.isNullOrEmpty(packageVersion)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageVersion));
            } else {
                sb.append(packageVersion);
            }
        }
        if (!StringHelper.isNullOrEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    public static void updateVDSStatisticsData(VDS vds, XmlRpcStruct xmlRpcStruct) {
        // ------------- vds memory usage ---------------------------
        vds.setusage_mem_percent(AssignIntValue(xmlRpcStruct, VdsProperties.mem_usage));

        // ------------- vds network statistics ---------------------
        java.util.Map<String, Object> interfaces = (java.util.Map<String, Object>) ((xmlRpcStruct
                .getItem(VdsProperties.network) instanceof java.util.Map) ? xmlRpcStruct.getItem(VdsProperties.network)
                : null);
        if (interfaces != null) {
            int networkUsage = 0;
            for (String name : interfaces.keySet()) {
                // LINQ 29456
                // Interface iface = vds.Interfaces.FirstOrDefault(x => x.name
                // == name);
                VdsNetworkInterface iface = null;
                for (VdsNetworkInterface tempInterface : vds.getInterfaces()) {
                    if (tempInterface.getName().equals(name)) {
                        iface = tempInterface;
                        break;
                    }
                }
                // LINQ 29456
                if (iface != null) {
                    iface.setVdsId(vds.getvds_id());
                    java.util.Map<String, Object> dictTemp =
                            (java.util.Map<String, Object>) ((interfaces.get(name) instanceof java.util.Map) ? interfaces
                                    .get(name)
                                    : null);
                    XmlRpcStruct dict = new XmlRpcStruct(dictTemp);
                    Double rx_rate = AssignDoubleValue(dict, VdsProperties.rx_rate);
                    Double rx_dropped = AssignDoubleValue(dict, VdsProperties.rx_dropped);
                    Double tx_rate = AssignDoubleValue(dict, VdsProperties.tx_rate);
                    Double tx_dropped = AssignDoubleValue(dict, VdsProperties.tx_dropped);
                    iface.getStatistics().setReceiveRate(rx_rate != null ? rx_rate : 0);
                    iface.getStatistics().setReceiveDropRate(rx_dropped != null ? rx_dropped : 0);
                    iface.getStatistics().setTransmitRate(tx_rate != null ? tx_rate : 0);
                    iface.getStatistics().setTransmitDropRate(tx_dropped != null ? tx_dropped : 0);
                    iface.setSpeed(AssignIntValue(dict, VdsProperties.if_speed));
                    iface.getStatistics().setStatus(AssignInterfaceStatusValue(dict, VdsProperties.iface_status));

                    // try
                    // {
                    int hold = (iface.getStatistics().getTransmitRate().compareTo(iface.getStatistics().getReceiveRate()) > 0 ? iface.getStatistics().getTransmitRate() : iface
                            .getStatistics().getReceiveRate()).intValue();
                    if (hold > networkUsage) {
                        networkUsage = hold;
                    }
                    // }
                    // catch (OverflowException ex)
                    // {
                    // log.error("Failed to assign usage network percent", ex);
                    // }
                }
            }
            vds.setusage_network_percent((networkUsage > 100) ? 100 : networkUsage);
        }

        // ----------- vds cpu statistics info ---------------------
        vds.setcpu_sys(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_sys));
        vds.setcpu_user(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_user));
        if (vds.getcpu_sys() != null && vds.getcpu_user() != null) {
            // try
            // {
            vds.setusage_cpu_percent((int)(vds.getcpu_sys() + vds.getcpu_user()));
            if (vds.getusage_cpu_percent() >= vds.gethigh_utilization()
                    || vds.getusage_cpu_percent() <= vds.getlow_utilization()) {
                if (vds.getcpu_over_commit_time_stamp() == null) {
                    vds.setcpu_over_commit_time_stamp(new java.util.Date());
                }
            } else {
                vds.setcpu_over_commit_time_stamp(null);
            }
            // }
            // LIVANT - this is a comapt exception, please check
            // catch (OverflowException ex)
            // {
            // log.error("Failed to assign usage cpu percent", ex);
            // }
        }
        //CPU load reported by VDSM is in uptime-style format, i.e. normalized
        //to unity, so that say an 8% load is reported as 0.08

        Double d = AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_load);
        d = ( d != null ) ? d : 0;
        vds.setcpu_load(d.doubleValue() * 100.0);
        vds.setcpu_idle(AssignDoubleValue(xmlRpcStruct, VdsProperties.cpu_idle));
        vds.setmem_available(AssignLongValue(xmlRpcStruct, VdsProperties.mem_available));
        vds.setmem_shared(AssignLongValue(xmlRpcStruct, VdsProperties.mem_shared));

        vds.setswap_free(AssignLongValue(xmlRpcStruct, VdsProperties.swap_free));
        vds.setswap_total(AssignLongValue(xmlRpcStruct, VdsProperties.swap_total));
        vds.setksm_cpu_percent(AssignIntValue(xmlRpcStruct, VdsProperties.ksm_cpu_percent));
        vds.setksm_pages(AssignLongValue(xmlRpcStruct, VdsProperties.ksm_pages));
        vds.setksm_state(AssignBoolValue(xmlRpcStruct, VdsProperties.ksm_state));

        // dynamic data got from GetVdsStats
        if (xmlRpcStruct.containsKey(VdsProperties.transparent_huge_pages_state)) {
            vds.setTransparentHugePagesState(EnumUtils.valueOf(VdsTransparentHugePagesState.class, xmlRpcStruct
                    .getItem(VdsProperties.transparent_huge_pages_state).toString(), true));
        }
        if (xmlRpcStruct.containsKey(VdsProperties.anonymous_transparent_huge_pages)) {
            vds.setAnonymousHugePages(AssignIntValue(xmlRpcStruct, VdsProperties.anonymous_transparent_huge_pages));
        }
        vds.setnet_config_dirty(AssignBoolValue(xmlRpcStruct, VdsProperties.netConfigDirty));

        vds.setImagesLastCheck(AssignDoubleValue(xmlRpcStruct, VdsProperties.images_last_check));
        vds.setImagesLastDelay(AssignDoubleValue(xmlRpcStruct, VdsProperties.images_last_delay));

        Integer vm_count = AssignIntValue(xmlRpcStruct, VdsProperties.vm_count);
        vds.setvm_count(vm_count == null ? 0 : vm_count);
        vds.setvm_active(AssignIntValue(xmlRpcStruct, VdsProperties.vm_active));
        vds.setvm_migrating(AssignIntValue(xmlRpcStruct, VdsProperties.vm_migrating));
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
    protected static void updateLocalDisksUsage(VDS vds, XmlRpcStruct xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.DISK_STATS)) {
            Map<String, Object> diskStatsStruct = (Map<String, Object>) xmlRpcStruct.getItem(VdsProperties.DISK_STATS);
            Map<String, Long> diskStats = new HashMap<String, Long>();

            vds.setLocalDisksUsage(diskStats);

            for (String path : diskStatsStruct.keySet()) {
                XmlRpcStruct pathStatsStruct = new XmlRpcStruct((Map<String, Object>) diskStatsStruct.get(path));

                diskStats.put(path, AssignLongValue(pathStatsStruct, VdsProperties.DISK_STATS_FREE));
            }
        }
    }

    private static void updateVDSDomainData(VDS vds, XmlRpcStruct xmlRpcStruct) {
        if (xmlRpcStruct.containsKey(VdsProperties.domains)) {
            java.util.Map<String, Object> domains = (java.util.Map<String, Object>)
                    xmlRpcStruct.getItem(VdsProperties.domains);
            java.util.ArrayList<VDSDomainsData> domainsData = new java.util.ArrayList<VDSDomainsData>();
            for (java.util.Map.Entry<String, ?> value : domains.entrySet()) {
                try {
                    VDSDomainsData data = new VDSDomainsData();
                    data.setDomainId(new Guid(value.getKey().toString()));
                    java.util.Map<String, Object> internalValue = (java.util.Map<String, Object>) value.getValue();
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
                } catch (java.lang.Exception e) {
                    log.error("failed building domains", e);
                }
            }
            vds.setDomains(domainsData);
        }
    }

    // internal static void updateVDSStaticData(VDS vds, XmlRpcStruct
    // xmlRpcStruct)
    // {
    // // ----------- read once vds info ---------------------
    // //vds.protocol = AssignDecimalValue(xmlRpcStruct,
    // VdsProperties.Protocol);
    //
    // }

    private static InterfaceStatus AssignInterfaceStatusValue(XmlRpcStruct input, String name) {
        InterfaceStatus ifaceStatus = InterfaceStatus.None;
        if (input.containsKey(name)) {
            String stringValue = (String) ((input.getItem(name) instanceof String) ? input.getItem(name) : null);
            if (!StringHelper.isNullOrEmpty(stringValue)) {
                if (StringHelper.EqOp(stringValue.toLowerCase().trim(), "up")) {
                    ifaceStatus = InterfaceStatus.Up;
                } else {
                    ifaceStatus = InterfaceStatus.Down;
                }
            }
        }
        return ifaceStatus;
    }

    private static Double AssignDoubleValue(XmlRpcStruct input, String name) {
        Double returnValue = null;
        if (input.containsKey(name)) {
            String stringValue = (String) ((input.getItem(name) instanceof String) ? input.getItem(name) : null);
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
    private static Double assignDoubleValueWithNullProtection(XmlRpcStruct input, String name) {
        Double doubleValue = AssignDoubleValue(input, name);
        return (doubleValue == null ? 0.0 : doubleValue);
    }

    private static Integer AssignIntValue(XmlRpcStruct input, String name) {
        if (input.containsKey(name)) {
            if (input.getItem(name) instanceof Integer) {
                return (Integer) input.getItem(name);
            }
            String stringValue = (String) input.getItem(name);
            if (!StringHelper.isNullOrEmpty(stringValue)) { // in case the input
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

    private static Long AssignLongValue(XmlRpcStruct input, String name) {
        if (input.containsKey(name)) {
            if (input.getItem(name) instanceof Long || input.getItem(name) instanceof Integer) {
                return Long.parseLong(input.getItem(name).toString());
            }
            String stringValue = (String) ((input.getItem(name) instanceof String) ? input.getItem(name) : null);
            if (!StringHelper.isNullOrEmpty(stringValue)) { // in case the input
                                                            // is decimal and we
                                                            // need int.
                stringValue = stringValue.split("[.]", -1)[0];
            }
            RefObject<Long> refDec = new RefObject<Long>();
            if (LongCompat.TryParse(stringValue, refDec)) {
                return refDec.argvalue;
            } else {
                log.errorFormat("Failed to parse {0} value {1} to long", name, stringValue);
            }
        }
        return null;
    }

    private static String AssignStringValue(XmlRpcStruct input, String name) {
        if (input.containsKey(name)) {
            return (String) ((input.getItem(name) instanceof String) ? input.getItem(name) : null);
        }
        return null;
    }

    private static String AssignStringValueFromArray(XmlRpcStruct input, String name) {
        if (input.containsKey(name)) {
            String[] arr = (String[]) ((input.getItem(name) instanceof String[]) ? input.getItem(name) : null);
            if (arr == null) {
                Object[] arr2 = (Object[]) ((input.getItem(name) instanceof Object[]) ? input.getItem(name) : null);
                if (arr2 != null) {
                    arr = new String[arr2.length];
                    for (int i = 0; i < arr2.length; i++)
                        arr[i] = arr2[i].toString();
                }
            }
            if (arr != null) {
                return StringHelper.join(",", arr);
            }
        }
        return null;
    }

    private static java.util.Date AssignDateTImeFromEpoch(XmlRpcStruct input, String name) {
        java.util.Date retval = null;
        try {
            if (input.containsKey(name)) {
                Double secsSinceEpoch = (Double) input.getItem(name);
                java.util.Calendar calendar = java.util.Calendar.getInstance();
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

    private static Boolean AssignBoolValue(XmlRpcStruct input, String name) {
        if (input.containsKey(name)) {
            if (input.getItem(name) instanceof Boolean) {
                return (Boolean) input.getItem(name);
            }
            boolean result = false;
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(result);
            boolean tempVar = BooleanCompat.TryParse(input.getItem(name).toString(), tempRefObject);
            result = tempRefObject.argvalue;
            if (tempVar) {
                return result;
            } else {
                log.errorFormat("Failed to parse {0} value {1} to bool", name, input.getItem(name).toString());
            }
        }
        return null;
    }

    private static void initDisks(XmlRpcStruct vmStruct, VmDynamic vm) {
        java.util.Map disks =
                (java.util.Map) vmStruct.getItem(VdsProperties.vm_disks);
        java.util.ArrayList<DiskImageDynamic> disksData = new java.util.ArrayList<DiskImageDynamic>();
        List<DiskImage> vmDisksFromDb = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vm.getId());
        for (Object diskAsObj : disks.values()) {
            XmlRpcStruct disk = new XmlRpcStruct((java.util.Map) diskAsObj);
            DiskImageDynamic diskData = new DiskImageDynamic();
            String imageGroupIdString = AssignStringValue(disk, VdsProperties.image_group_id);
            if (!StringHelper.isNullOrEmpty(imageGroupIdString)) {
                Guid imageGroupIdGuid = new Guid(imageGroupIdString);
                DiskImage vmCurrentDisk = null;
                for (DiskImage vmDisk : vmDisksFromDb) {
                    if (vmDisk.getimage_group_id() != null
                            && imageGroupIdGuid.equals(vmDisk.getimage_group_id().getValue())) {
                        vmCurrentDisk = vmDisk;
                        break;
                    }
                }
                if (vmCurrentDisk != null) {
                    diskData.setId(vmCurrentDisk.getId());
                    diskData.setread_rate(AssignIntValue(disk, VdsProperties.vm_disk_read_rate));
                    diskData.setwrite_rate(AssignIntValue(disk, VdsProperties.vm_disk_write_rate));

                    if (disk.contains(VdsProperties.disk_actual_size)) {
                        Long size = AssignLongValue(disk, VdsProperties.disk_actual_size);
                        diskData.setactual_size(size != null ? size * 512 : 0);
                    } else if (disk.contains(VdsProperties.disk_true_size)) {
                        Long size = AssignLongValue(disk, VdsProperties.disk_true_size);
                        diskData.setactual_size(size != null ? size : 0);
                    }
                    if (disk.contains(VdsProperties.vm_disk_read_latency)) {
                        diskData.setReadLatency(assignDoubleValueWithNullProtection(disk,
                                VdsProperties.vm_disk_read_latency) / NANO_SECONDS);
                    }
                    if (disk.contains(VdsProperties.vm_disk_write_latency)) {
                        diskData.setWriteLatency(assignDoubleValueWithNullProtection(disk,
                                VdsProperties.vm_disk_write_latency) / NANO_SECONDS);
                    }
                    if (disk.contains(VdsProperties.vm_disk_flush_latency)) {
                        diskData.setFlushLatency(assignDoubleValueWithNullProtection(disk,
                                VdsProperties.vm_disk_flush_latency) / NANO_SECONDS);
                    }
                    disksData.add(diskData);
                }
            }
        }
        vm.setDisks(disksData);
    }

    private static void initAppsList(XmlRpcStruct vmStruct, VmDynamic vm) {
        if (vmStruct.contains(VdsProperties.app_list)) {
            Object tempAppsList = vmStruct.getItem(VdsProperties.app_list);
            if (tempAppsList instanceof Object[]) {
                Object[] apps = (Object[]) tempAppsList;
                StringBuilder builder = new StringBuilder();
                boolean firstTime = true;
                for (Object app : apps) {
                    String appString = (String) ((app instanceof String) ? app : null);
                    if (app == null) {
                        log.warnFormat("Failed to convert app: {0} to string", app);
                    }
                    if (!firstTime) {
                        builder.append(",");
                    } else {
                        firstTime = false;
                    }
                    builder.append(appString);
                }
                vm.setapp_list(builder.toString());
            } else {
                vm.setapp_list("");
            }
        }
    }

    private static void updateVdsStaticVersion(VDS vds, XmlRpcStruct xmlRpcStruct) {
        VdsVersion version = new VdsVersion();
        version.setVersionName(AssignStringValue(xmlRpcStruct, "version_name"));
        version.setSoftwareVersion(AssignStringValue(xmlRpcStruct, "software_version"));
        version.setSoftwareRevision(AssignStringValue(xmlRpcStruct, "software_revision"));
        version.setBuildName(AssignStringValue(xmlRpcStruct, "build_name"));
        vds.setVersion(version);
    }

    private static VMStatus convertToVmStatus(String statusName) {
        VMStatus status = VMStatus.Unassigned;
        if (StringHelper.EqOp(statusName, "Running") || StringHelper.EqOp(statusName, "Unknown")) {
            status = VMStatus.Up;
        }
        else if (StringHelper.EqOp(statusName, "Migration Source")) {
            status = VMStatus.MigratingFrom;
        }
        else if (StringHelper.EqOp(statusName, "Migration Destination")) {
            status = VMStatus.MigratingTo;
        } else {
            {
                try {
                    statusName = statusName.replace(" ", "");
                    status = EnumUtils.valueOf(VMStatus.class, statusName, true);
                } catch (java.lang.Exception e) {
                    log.errorFormat("Vm status: {0} illegal", statusName);
                }
            }
        }
        return status;
    }

    @SuppressWarnings("unchecked")
    public static void updateNetworkData(VDS vds, XmlRpcStruct xmlRpcStruct) {
        List<VdsNetworkInterface> oldInterfaces =
                DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(vds.getvds_id());
        vds.getInterfaces().clear();

        // Interfaces list
        java.util.Map nics =
                (java.util.Map) ((xmlRpcStruct.getItem(VdsProperties.network_nics) instanceof java.util.Map) ? xmlRpcStruct
                        .getItem(VdsProperties.network_nics)
                        : null);
        if (nics != null) {
            for (Object keyAsObject : nics.keySet()) {
                String key = (String) keyAsObject;
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.NewGuid());
                iface.setId(iStats.getId());

                iface.setName(key);
                iface.setVdsId(vds.getvds_id());

                // name value of nic property, i.e.: speed = 1000
                java.util.Map<String, Object> dataAsMap =
                        (java.util.Map) ((nics.get(key) instanceof java.util.Map) ? nics
                                .get(key) : null);
                XmlRpcStruct data = new XmlRpcStruct(dataAsMap);
                if (data != null) {
                    if (data.getItem("speed") != null) {
                        Object speed = data.getItem("speed");
                        iface.setSpeed((Integer) speed);
                    }
                    if (data.getItem("addr") != null) {
                        iface.setAddress((String) ((data.getItem("addr") instanceof String) ? data.getItem("addr") : null));
                    }
                    if (data.getItem("netmask") != null) {
                        iface.setSubnet((String) ((data.getItem("netmask") instanceof String) ? data.getItem("netmask")
                                : null));
                    }
                    if (data.getItem("hwaddr") != null) {
                        iface.setMacAddress((String) ((data.getItem("hwaddr") instanceof String) ? data.getItem("hwaddr")
                                : null));
                    }
                    // if we get "permhwaddr", we are a part of a bond and we use that as the mac address
                    if (data.getItem("permhwaddr") != null) {
                        iface.setMacAddress((String) ((data.getItem("permhwaddr") instanceof String) ? data.getItem("permhwaddr")
                                : null));
                    }
                }

                iStats.setVdsId(vds.getvds_id());

                vds.getInterfaces().add(iface);
            }
        }

        // interface to vlan map
        Map<String, Integer> currVlans = new java.util.HashMap<String, Integer>();
        // vlans
        java.util.Map<String, Object> vlans =
                (java.util.Map) ((xmlRpcStruct.getItem(VdsProperties.network_vlans) instanceof java.util.Map) ? xmlRpcStruct
                        .getItem(VdsProperties.network_vlans)
                        : null);
        if (vlans != null) {
            for (String key : vlans.keySet()) {
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.NewGuid());
                iface.setId(iStats.getId());

                iface.setName(key);
                iface.setVdsId(vds.getvds_id());

                if (key.contains(".")) {
                    String[] names = key.split("[.]", -1);
                    String vlan = names[1];
                    iface.setVlanId(Integer.parseInt(vlan));
                    currVlans.put(key, iface.getVlanId());
                }

                java.util.Map dataAsMap = (java.util.Map) ((vlans.get(key) instanceof java.util.Map) ? vlans.get(key)
                        : null);
                XmlRpcStruct data = new XmlRpcStruct(dataAsMap);
                if (data.getItem("addr") != null) {
                    iface.setAddress((String) ((data.getItem("addr") instanceof String) ? data.getItem("addr") : null));
                }
                if (data.getItem("netmask") != null) {
                    iface.setSubnet((String) ((data.getItem("netmask") instanceof String) ? data.getItem("netmask")
                            : null));
                }
                iStats.setVdsId(vds.getvds_id());

                vds.getInterfaces().add(iface);
            }
        }

        // bonds
        java.util.Map<String, Object> bonds =
                (java.util.Map) ((xmlRpcStruct.getItem(VdsProperties.network_bondings) instanceof java.util.Map) ? xmlRpcStruct
                        .getItem(VdsProperties.network_bondings)
                        : null);
        if (bonds != null) {
            for (String key : bonds.keySet()) {
                VdsNetworkInterface iface = new VdsNetworkInterface();
                VdsNetworkStatistics iStats = new VdsNetworkStatistics();
                iface.setStatistics(iStats);
                iStats.setId(Guid.NewGuid());
                iface.setId(iStats.getId());

                iface.setName(key);
                iface.setVdsId(vds.getvds_id());
                iface.setBonded(true);

                java.util.Map dataAsMap = (java.util.Map) ((bonds.get(key) instanceof java.util.Map) ? bonds.get(key)
                        : null);
                XmlRpcStruct data = new XmlRpcStruct(dataAsMap);
                if (data != null) {
                    if (data.getItem("hwaddr") != null) {
                        iface.setMacAddress((String) ((data.getItem("hwaddr") instanceof String) ? data.getItem("hwaddr")
                                : null));
                    }
                    if (data.getItem("addr") != null) {
                        iface.setAddress((String) ((data.getItem("addr") instanceof String) ? data.getItem("addr") : null));
                    }
                    if (data.getItem("netmask") != null) {
                        iface.setSubnet((String) ((data.getItem("netmask") instanceof String) ? data.getItem("netmask")
                                : null));
                    }
                    if (data.getItem(VdsProperties.GLOBAL_GATEWAY) != null) {
                        iface.setGateway((String) ((data.getItem(VdsProperties.GLOBAL_GATEWAY) instanceof String) ? data.getItem(VdsProperties.GLOBAL_GATEWAY)
                                : null));
                    }
                    if (data.getItem("slaves") != null) {
                        Object[] interfaces = (Object[]) ((data.getItem("slaves") instanceof Object[]) ? data
                                .getItem("slaves") : null);
                        iStats.setVdsId(vds.getvds_id());
                        AddBond(vds, iface, interfaces);
                    }
                    XmlRpcStruct config =
                            (data.getItem("cfg") instanceof Map) ? new XmlRpcStruct((Map) data.getItem("cfg")) : null;

                    if (config != null && config.getItem("BONDING_OPTS") != null) {
                        iface.setBondOptions(config.getItem("BONDING_OPTS").toString());
                    }
                    AddBootProtocol(config, iface);
                }
            }
        }
        // network to vlan map
        Map<String, Integer> networkVlans = new java.util.HashMap<String, Integer>();

        // Networks collection (name point to list of nics or bonds)
        java.util.Map<String, Object> networks =
                (java.util.Map) ((xmlRpcStruct.getItem(VdsProperties.network_networks) instanceof java.util.Map) ? xmlRpcStruct
                        .getItem(VdsProperties.network_networks)
                        : null);
        if (networks != null) {
            vds.getNetworks().clear();
            for (String key : networks.keySet()) {
                java.util.Map<String, Object> networkAsMap =
                        (java.util.Map) ((networks.get(key) instanceof java.util.Map) ? networks
                                .get(key) : null);
                XmlRpcStruct network = new XmlRpcStruct(networkAsMap);
                if (network != null) {
                    network net = new network();
                    net.setname(key);

                    if (network.getItem("addr") != null) {
                        net.setaddr(network.getItem("addr").toString());
                    }
                    if (network.getItem("netmask") != null) {
                        net.setsubnet(network.getItem("netmask").toString());
                    }
                    if (network.getItem(VdsProperties.GLOBAL_GATEWAY) != null) {
                        net.setgateway(network.getItem(VdsProperties.GLOBAL_GATEWAY).toString());
                    }

                    // map interface to network
                    Object[] ports = (Object[]) ((network.getItem("ports") instanceof Object[]) ? network
                            .getItem("ports") : null);
                    if (ports != null) {
                        for (Object port : ports) {
                            // LINQ 29456
                            // Interface iface = vds.Interfaces.FirstOrDefault(i
                            // => i.name == port);
                            VdsNetworkInterface iface = null;
                            for (VdsNetworkInterface tempInterface : vds.getInterfaces()) {
                                if (tempInterface.getName().equals(port.toString())) {
                                    iface = tempInterface;
                                    break;
                                }
                            }
                            // LINQ 29456
                            if (iface != null) {
                                iface.setNetworkName(net.getname());

                                if (currVlans.containsKey(iface.getName())) {
                                    networkVlans.put(net.getname(), currVlans.get(iface.getName()));
                                }
                                iface.setAddress(net.getaddr());

                                // set the management ip
                                if (StringHelper.EqOp(iface.getNetworkName(), NetworkUtils.EngineNetwork)) {
                                    iface.setType(iface.getType() | VdsInterfaceType.Management.getValue());
                                }
                                iface.setSubnet(net.getsubnet());
                                iface.setGateway(net.getgateway());
                                java.util.Map networkConfigAsMap =
                                        (java.util.Map) ((network.getItem("cfg") instanceof java.util.Map) ? network
                                                .getItem("cfg") : null);
                                XmlRpcStruct networkConfig = networkConfigAsMap == null ? null : new XmlRpcStruct(
                                        networkConfigAsMap);
                                AddBootProtocol(networkConfig, iface);
                            }
                        }
                    }
                    vds.getNetworks().add(net);
                }
            }
        }

        // Check vlans are line with Clusters vlans
        checkClusterVlans(vds, networkVlans);

        // set bonding options
        setBondingOptions(vds, oldInterfaces);
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

    private static void checkClusterVlans(VDS vds, Map<String, Integer> hostVlans) {
        List<network> clusterNetworks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(vds.getvds_group_id());
        for (network net : clusterNetworks) {
            if (net.getvlan_id() != null) {
                if (hostVlans.containsKey(net.getname())) {
                    if (!hostVlans.get(net.getname()).equals(net.getvlan_id())) {
                        // error wrong vlan
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setVdsId(vds.getvds_id());
                        logable.AddCustomValue("VlanIdHost", hostVlans.get(net.getname()).toString());
                        logable.AddCustomValue("VlanIdCluster", net.getvlan_id().toString());
                        AuditLogDirector.log(logable, AuditLogType.NETWORK_HOST_USING_WRONG_CLUSER_VLAN);
                    }
                } else {
                    // error no vlan
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.setVdsId(vds.getvds_id());
                    logable.AddCustomValue("VlanIdCluster", net.getvlan_id().toString());
                    AuditLogDirector.log(logable, AuditLogType.NETWORK_HOST_MISSING_CLUSER_VLAN);
                }
            }
        }
    }

    private static void AddBootProtocol(XmlRpcStruct cfg, VdsNetworkInterface iface) {
        if (cfg != null) {
            if (cfg.getItem("BOOTPROTO") != null) {
                if (StringHelper.EqOp(cfg.getItem("BOOTPROTO").toString().toLowerCase(), "dhcp")) {
                    iface.setBootProtocol(NetworkBootProtocol.Dhcp);
                } else {
                    iface.setBootProtocol(NetworkBootProtocol.None);
                }
            } else if (cfg.containsKey("IPADDR") && !StringHelper.isNullOrEmpty(cfg.getItem("IPADDR").toString())) {
                iface.setBootProtocol(NetworkBootProtocol.StaticIp);
                if (cfg.containsKey(VdsProperties.gateway)) {
                    Object gateway = cfg.getItem(VdsProperties.gateway);
                    if (gateway != null && !StringHelper.isNullOrEmpty(gateway.toString())) {
                        iface.setGateway(gateway.toString());
                    }
                }
            } else {
                iface.setBootProtocol(NetworkBootProtocol.None);
            }
        }
    }

    private static void AddBond(VDS vds, VdsNetworkInterface iface, Object[] interfaces) {
        vds.getInterfaces().add(iface);
        if (interfaces != null) {
            for (Object name : interfaces) {
                // LINQ 29456
                // Interface nic = vds.Interfaces.Single(n => n.name == name);
                VdsNetworkInterface nic = null;
                for (VdsNetworkInterface tempInterface : vds.getInterfaces()) {
                    if (tempInterface.getName().equals(name.toString())) {
                        nic = tempInterface;
                        break;
                    }
                }

                // LINQ 29456
                if (nic != null) {
                    nic.setBondName(iface.getName());
                }
            }
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsBrokerObjectsBuilder.class);

}
