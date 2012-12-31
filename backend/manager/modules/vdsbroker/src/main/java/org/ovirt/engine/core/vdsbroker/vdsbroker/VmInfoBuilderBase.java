package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.compat.Version;
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
        createInfo.add(VdsProperties.vm_name, vm.getVmName());
        createInfo.add(VdsProperties.mem_size_mb, vm.getVmMemSizeMb());
        createInfo.add(VdsProperties.smartcardEnabled, Boolean.toString(vm.isSmartcardEnabled()));
        createInfo.add(VdsProperties.num_of_cpus,
                (new Integer(vm.getNumOfCpus())).toString());
        if (Config.<Boolean> GetValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.add(VdsProperties.cores_per_socket,
                    (Integer.toString(vm.getCpuPerSocket())));
        }
        final String compatibilityVersion = vm.getVdsGroupCompatibilityVersion().toString();
        addCpuPinning(compatibilityVersion);
        createInfo.add(VdsProperties.emulatedMachine, Config.<String> GetValue(
                ConfigValues.EmulatedMachine, compatibilityVersion));
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> GetValue(ConfigValues.SSLEnabled)) {
            createInfo.add(VdsProperties.spiceSslCipherSuite,
                    Config.<String> GetValue(ConfigValues.CipherSuite));
            createInfo.add(VdsProperties.SpiceSecureChannels, Config.<String> GetValue(
                    ConfigValues.SpiceSecureChannels, compatibilityVersion));
        }
        createInfo.add(VdsProperties.kvmEnable, vm.getKvmEnable().toString()
                .toLowerCase());
        createInfo.add(VdsProperties.acpiEnable, vm.getAcpiEnable().toString()
                .toLowerCase());

        createInfo.add(VdsProperties.Custom,
                VmPropertiesUtils.getInstance().getVMProperties(vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData()));
        createInfo.add(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.isRunAndPause()) {
            createInfo.add(VdsProperties.launch_paused_param, "true");
        }
        if(vm.isUseHostCpuFlags()) {
            createInfo.add(VdsProperties.cpuType,
                    "hostPassthrough");
        } else if (vm.getVdsGroupCpuFlagsData() != null) {
            createInfo.add(VdsProperties.cpuType,
                    vm.getVdsGroupCpuFlagsData());
        }
        createInfo.add(VdsProperties.niceLevel,
                (new Integer(vm.getNiceLevel())).toString());
        if (vm.getStatus() == VMStatus.Suspended
                && !StringUtils.isEmpty(vm.getHibernationVolHandle())) {
            createInfo.add(VdsProperties.hiberVolHandle,
                    vm.getHibernationVolHandle());
        }
        createInfo.add(VdsProperties.KeyboardLayout,
                Config.<String> GetValue(ConfigValues.VncKeyboardLayout));
        if (vm.getVmOs().isLinux()) {
            createInfo.add(VdsProperties.PitReinjection, "false");
        }

        if (vm.getDisplayType() == DisplayType.vnc) {
            createInfo.add(VdsProperties.TabletEnable, "true");
        }
        createInfo.add(VdsProperties.transparent_huge_pages,
                vm.isTransparentHugePages() ? "true" : "false");
    }

    private void addCpuPinning(final String compatibilityVersion) {
        final String cpuPinning = vm.getCpuPinning();
        if (StringUtils.isNotEmpty(cpuPinning)
                && Boolean.TRUE.equals(Config.<Boolean> GetValue(ConfigValues.CpuPinningEnabled,
                        compatibilityVersion))) {
            final XmlRpcStruct pinDict = new XmlRpcStruct();
            for (String pin : cpuPinning.split("_")) {
                final String[] split = pin.split("#");
                pinDict.add(split[0], split[1]);
            }
            createInfo.add(VdsProperties.cpuPinning, pinDict);
        }
    }

    protected void buildVmNetworkCluster() {
        // set Display network
        List<NetworkCluster> all = DbFacade.getInstance()
                .getNetworkClusterDao().getAllForCluster(vm.getVdsGroupId());
        NetworkCluster networkCluster = null;
        for (NetworkCluster tempNetworkCluster : all) {
            if (tempNetworkCluster.getis_display()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        if (networkCluster != null) {
            Network net = null;
            List<Network> allNetworks = DbFacade.getInstance().getNetworkDao()
                    .getAll();
            for (Network tempNetwork : allNetworks) {
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
        if (!StringUtils.isEmpty(vm.getInitrdUrl())) {
            createInfo.add(VdsProperties.InitrdUrl, vm.getInitrdUrl());
        }
        if (!StringUtils.isEmpty(vm.getKernelUrl())) {
            createInfo.add(VdsProperties.KernelUrl, vm.getKernelUrl());

            if (!StringUtils.isEmpty(vm.getKernelParams())) {
                createInfo.add(VdsProperties.KernelParams,
                        vm.getKernelParams());
            }
        }
    }

    protected void buildVmTimeZone() {
        // send vm_dynamic.utc_diff if exist, if not send vm_static.time_zone
        if (vm.getUtcDiff() != null) {
            createInfo.add(VdsProperties.utc_diff, vm.getUtcDiff().toString());
        } else {
            // get vm timezone
            String timeZone = TimeZoneInfo.Local.getId();
            if (!StringUtils.isEmpty(vm.getTimeZone())) {
                timeZone = vm.getTimeZone();
            }

            int offset = 0;
            String javaZoneId = null;

            if (vm.getOs().isWindows()) {
                // convert to java & calculate offset
                javaZoneId = WindowsJavaTimezoneMapping.windowsToJava.get(timeZone);
            } else {
                javaZoneId = timeZone;
            }

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
     * @param compatibilityVersion
     * @return String, the sound card device type
     */
    public static String getSoundDevice(VmBase vm, Version compatibilityVersion) {
        final String OS_REGEX = "^.*%1s,([^,]*).*$";
        final String DEFAULT_TYPE = "default";
        String ret = DEFAULT_TYPE;

        if (vm.getvm_type() == VmType.Desktop) {

            String soundDeviceTypeConfig = Config.<String> GetValue(
                    ConfigValues.DesktopAudioDeviceType, compatibilityVersion.toString());
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

    protected abstract void buildVmMemoryBalloon();

}
