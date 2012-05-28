package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.comparators.DiskImageByBootComparator;
import org.ovirt.engine.core.dal.comparators.DiskImageByDiskAliasComparator;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public abstract class VmInfoBuilderBase {

    protected static final Log log = LogFactory.getLog(VmInfoBuilderBase.class);
    protected XmlRpcStruct createInfo;
    protected VM vm;
    // IDE supports only 4 slots , slot 2 is preserved by VDSM to the CDROM
    protected int[] ideIndexSlots = new int[] { 0, 1, 3 };

    protected void buildVmProperties() {
        createInfo.add(VdsProperties.vm_guid, vm.getId().toString());
        createInfo.add(VdsProperties.vm_name, vm.getvm_name());
        createInfo.add(VdsProperties.mem_size_mb, vm.getvm_mem_size_mb());
        createInfo.add(VdsProperties.num_of_cpus,
                (new Integer(vm.getnum_of_cpus())).toString());
        if (Config.<Boolean> GetValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.add(VdsProperties.cores_per_socket,
                    (new Integer(vm.getcpu_per_socket())).toString());
        }
        createInfo.add(VdsProperties.emulatedMachine, Config.<String> GetValue(
                ConfigValues.EmulatedMachine, vm
                        .getvds_group_compatibility_version().toString()));
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> GetValue(ConfigValues.SSLEnabled)) {
            createInfo.add(VdsProperties.spiceSslCipherSuite,
                    Config.<String> GetValue(ConfigValues.CipherSuite));
            createInfo.add(VdsProperties.SpiceSecureChannels,Config.<String> GetValue(
                    ConfigValues.SpiceSecureChannels, vm.getvds_group_compatibility_version().toString()));
        }
        createInfo.add(VdsProperties.kvmEnable, vm.getkvm_enable().toString()
                .toLowerCase());
        createInfo.add(VdsProperties.acpiEnable, vm.getacpi_enable().toString()
                .toLowerCase());

        createInfo.add(VdsProperties.Custom,
                VmPropertiesUtils.getInstance().getVMProperties(vm.getvds_group_compatibility_version(), vm.getStaticData()));
        createInfo.add(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.getRunAndPause()) {
            createInfo.add(VdsProperties.launch_paused_param, "true");
        }
        if (vm.getvds_group_cpu_flags_data() != null) {
            createInfo.add(VdsProperties.cpuType,
                    vm.getvds_group_cpu_flags_data());
        }
        createInfo.add(VdsProperties.niceLevel,
                (new Integer(vm.getnice_level())).toString());
        if (vm.getstatus() == VMStatus.Suspended
                && !StringHelper.isNullOrEmpty(vm.gethibernation_vol_handle())) {
            createInfo.add(VdsProperties.hiberVolHandle,
                    vm.gethibernation_vol_handle());
        }
        createInfo.add(VdsProperties.KeyboardLayout,
                Config.<String> GetValue(ConfigValues.VncKeyboardLayout));
        if (vm.getvm_os().isLinux()) {
            createInfo.add(VdsProperties.PitReinjection, "false");
        }

        if (vm.getdisplay_type() == DisplayType.vnc) {
            createInfo.add(VdsProperties.TabletEnable, "true");
        }
        createInfo.add(VdsProperties.transparent_huge_pages,
                vm.getTransparentHugePages() ? "true" : "false");
    }

    protected void buildVmNetworkCluster() {
        // set Display network
        List<network_cluster> all = DbFacade.getInstance()
                .getNetworkClusterDAO().getAllForCluster(vm.getvds_group_id());
        network_cluster networkCluster = null;
        for (network_cluster tempNetworkCluster : all) {
            if (tempNetworkCluster.getis_display()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        if (networkCluster != null) {
            network net = null;
            List<network> allNetworks = DbFacade.getInstance().getNetworkDAO()
                    .getAll();
            for (network tempNetwork : allNetworks) {
                if (tempNetwork.getId().equals(networkCluster.getnetwork_id())) {
                    net = tempNetwork;
                    break;
                }
            }
            if (net != null) {
                createInfo.add(VdsProperties.displaynetwork, net.getname());
            }
        }
    }

    protected void buildVmBootOptions() {
        // Boot Options
        if (!StringHelper.isNullOrEmpty(vm.getinitrd_url())) {
            createInfo.add(VdsProperties.InitrdUrl, vm.getinitrd_url());
        }
        if (!StringHelper.isNullOrEmpty(vm.getkernel_url())) {
            createInfo.add(VdsProperties.KernelUrl, vm.getkernel_url());

            if (!StringHelper.isNullOrEmpty(vm.getkernel_params())) {
                createInfo.add(VdsProperties.KernelParams,
                        vm.getkernel_params());
            }
        }
    }

    protected void buildVmTimeZone() {
        // send vm_dynamic.utc_diff if exist, if not send vm_static.time_zone
        if (vm.getutc_diff() != null) {
            createInfo.add(VdsProperties.utc_diff, vm.getutc_diff().toString());
        } else {
            // get vm timezone
            String timeZone = TimeZoneInfo.Local.getId();
            if (!StringHelper.isNullOrEmpty(vm.gettime_zone())) {
                timeZone = vm.gettime_zone();
            }

            // convert to java & calculate offset
            String javaZoneId = WindowsJavaTimezoneMapping.windowsToJava
                    .get(timeZone);
            int offset = 0;
            if (javaZoneId != null) {
                offset = (TimeZone.getTimeZone(javaZoneId).getOffset(
                        new Date().getTime()) / 1000);
            }
            createInfo.add(VdsProperties.utc_diff, "" + offset);
        }
    }

    protected List<Disk> getSortedDisks() {
        // order first by drive numbers and then order by boot for the bootable
        // drive to be first (important for IDE to be index 0) !
        List<Disk> diskImages = new ArrayList<Disk>(vm.getDiskMap()
                .values());
        Collections.sort(diskImages, new DiskImageByDiskAliasComparator());
        Collections.sort(diskImages,
                Collections.reverseOrder(new DiskImageByBootComparator()));
        return diskImages;
    }

    /**
     * gets the vm sound device type
     *
     * @param vm
     *            The VM
     * @return String, the sound card device type
     */
    public static String getSoundDevice(VM vm) {
        final String OS_REGEX = "^.*%1s,([^,]*).*$";
        final String DEFAULT_TYPE = "default";
        String ret = DEFAULT_TYPE;

        if (vm.getvm_type() == VmType.Desktop) {

            String soundDeviceTypeConfig = Config.<String> GetValue(
                        ConfigValues.DesktopAudioDeviceType, vm
                                .getvds_group_compatibility_version().toString());
            String vmOS = vm.getos().name();

            Pattern regexPattern = Pattern.compile(String
                        .format(OS_REGEX, vmOS));
            Matcher regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);

            if (regexMatcher.find()) {
                ret = regexMatcher.group(1);
            } else {
                regexPattern = Pattern.compile(String.format(OS_REGEX,
                            DEFAULT_TYPE));
                regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);
                if (regexMatcher.find()) {
                    ret = regexMatcher.group(1);
                }
            }
        }
        return ret;
    }

    protected void logUnsupportedInterfaceType() {
        log.error("Unsupported interface type, ISCSI interface type is not supported.");
    }

    protected abstract void buildVmVideoCards();

    protected abstract void buildVmCD();

    protected abstract void buildVmFloppy();

    protected abstract void buildVmDrives();

    protected abstract void buildVmNetworkInterfaces();

    protected abstract void buildVmSoundDevices();

    protected abstract void buildUnmanagedDevices();

    protected abstract void buildVmBootSequence();

    protected abstract void buildSysprepVmPayload(String strSysPrepContent);

    protected abstract void buildVmUsbDevices();
}
