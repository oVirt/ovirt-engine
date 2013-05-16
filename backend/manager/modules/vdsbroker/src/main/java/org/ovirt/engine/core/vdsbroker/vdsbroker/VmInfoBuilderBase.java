package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.ovirt.engine.core.common.businessentities.comparators.DiskImageByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class VmInfoBuilderBase {

    protected static final Log log = LogFactory.getLog(VmInfoBuilderBase.class);
    protected Map<String, Object> createInfo;
    protected VM vm;
    // IDE supports only 4 slots , slot 2 is preserved by VDSM to the CDROM
    protected int[] ideIndexSlots = new int[] { 0, 1, 3 };

    private static class DiskImageByBootComparator implements Comparator<Disk>, Serializable {
        private static final long serialVersionUID = 4732164571328497830L;

        @Override
        public int compare(Disk o1, Disk o2) {
            Boolean boot1 = o1.isBoot();
            Boolean boot2 = o2.isBoot();
            return boot1.compareTo(boot2);
        }
    }

    protected void buildVmProperties() {
        createInfo.put(VdsProperties.vm_guid, vm.getId().toString());
        createInfo.put(VdsProperties.vm_name, vm.getName());
        createInfo.put(VdsProperties.mem_size_mb, vm.getVmMemSizeMb());
        createInfo.put(VdsProperties.smartcardEnabled, Boolean.toString(vm.isSmartcardEnabled()));
        createInfo.put(VdsProperties.num_of_cpus,
                String.valueOf(vm.getNumOfCpus()));
        if (Config.<Boolean> GetValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.put(VdsProperties.cores_per_socket,
                    (Integer.toString(vm.getCpuPerSocket())));
        }
        final String compatibilityVersion = vm.getVdsGroupCompatibilityVersion().toString();
        addCpuPinning(compatibilityVersion);
        createInfo.put(VdsProperties.emulatedMachine, Config.<String> GetValue(
                ConfigValues.EmulatedMachine, compatibilityVersion));
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> GetValue(ConfigValues.SSLEnabled)) {
            createInfo.put(VdsProperties.spiceSslCipherSuite,
                    Config.<String> GetValue(ConfigValues.CipherSuite));
            createInfo.put(VdsProperties.SpiceSecureChannels, Config.<String> GetValue(
                    ConfigValues.SpiceSecureChannels, compatibilityVersion));
        }
        createInfo.put(VdsProperties.kvmEnable, vm.getKvmEnable().toString()
                .toLowerCase());
        createInfo.put(VdsProperties.acpiEnable, vm.getAcpiEnable().toString()
                .toLowerCase());

        createInfo.put(VdsProperties.Custom,
                VmPropertiesUtils.getInstance().getVMProperties(vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData()));
        createInfo.put(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.isRunAndPause()) {
            createInfo.put(VdsProperties.launch_paused_param, "true");
        }
        if(vm.isUseHostCpuFlags()) {
            createInfo.put(VdsProperties.cpuType,
                    "hostPassthrough");
        } else if (vm.getVdsGroupCpuFlagsData() != null) {
            createInfo.put(VdsProperties.cpuType,
                    vm.getVdsGroupCpuFlagsData());
        }
        createInfo.put(VdsProperties.niceLevel,
                String.valueOf(vm.getNiceLevel()));
        if (vm.getStatus() == VMStatus.Suspended
                && !StringUtils.isEmpty(vm.getHibernationVolHandle())) {
            createInfo.put(VdsProperties.hiberVolHandle,
                    vm.getHibernationVolHandle());
        }
        String keyboardLayout = vm.getVncKeyboardLayout(); // if set per VM use that value
        if (keyboardLayout == null) { // otherwise fall back to global setting
            keyboardLayout = Config.<String> GetValue(ConfigValues.VncKeyboardLayout);
        }
        createInfo.put(VdsProperties.KeyboardLayout, keyboardLayout);
        if (vm.getVmOs().isLinux()) {
            createInfo.put(VdsProperties.PitReinjection, "false");
        }

        if (vm.getDisplayType() == DisplayType.vnc) {
            createInfo.put(VdsProperties.TabletEnable, "true");
        }
        createInfo.put(VdsProperties.transparent_huge_pages,
                vm.isTransparentHugePages() ? "true" : "false");
    }

    private void addCpuPinning(final String compatibilityVersion) {
        final String cpuPinning = vm.getCpuPinning();
        if (StringUtils.isNotEmpty(cpuPinning)
                && Boolean.TRUE.equals(Config.<Boolean> GetValue(ConfigValues.CpuPinningEnabled,
                        compatibilityVersion))) {
            final Map<String, Object> pinDict = new HashMap<String, Object>();
            for (String pin : cpuPinning.split("_")) {
                final String[] split = pin.split("#");
                pinDict.put(split[0], split[1]);
            }
            createInfo.put(VdsProperties.cpuPinning, pinDict);
        }
    }

    protected void buildVmNetworkCluster() {
        // set Display network
        List<NetworkCluster> all = DbFacade.getInstance()
                .getNetworkClusterDao().getAllForCluster(vm.getVdsGroupId());
        NetworkCluster networkCluster = null;
        for (NetworkCluster tempNetworkCluster : all) {
            if (tempNetworkCluster.isDisplay()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        if (networkCluster != null) {
            Network net = null;
            List<Network> allNetworks = DbFacade.getInstance().getNetworkDao()
                    .getAll();
            for (Network tempNetwork : allNetworks) {
                if (tempNetwork.getId().equals(networkCluster.getNetworkId())) {
                    net = tempNetwork;
                    break;
                }
            }
            if (net != null) {
                createInfo.put(VdsProperties.DISPLAY_NETWORK, net.getName());
            }
        }
    }

    protected void buildVmBootOptions() {
        // Boot Options
        if (!StringUtils.isEmpty(vm.getInitrdUrl())) {
            createInfo.put(VdsProperties.InitrdUrl, vm.getInitrdUrl());
        }
        if (!StringUtils.isEmpty(vm.getKernelUrl())) {
            createInfo.put(VdsProperties.KernelUrl, vm.getKernelUrl());

            if (!StringUtils.isEmpty(vm.getKernelParams())) {
                createInfo.put(VdsProperties.KernelParams,
                        vm.getKernelParams());
            }
        }
    }

    protected void buildVmTimeZone() {
        // send vm_dynamic.utc_diff if exist, if not send vm_static.time_zone
        if (vm.getUtcDiff() != null) {
            createInfo.put(VdsProperties.utc_diff, vm.getUtcDiff().toString());
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
            createInfo.put(VdsProperties.utc_diff, "" + offset);
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

        if (vm.getVmType() == VmType.Desktop) {

            String soundDeviceTypeConfig = Config.<String> GetValue(
                    ConfigValues.DesktopAudioDeviceType, compatibilityVersion.toString());
            String vmOS = vm.getOs().name();

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

    protected abstract void buildVmWatchdog();

}
