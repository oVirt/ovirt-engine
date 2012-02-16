package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.comparators.DiskImageByBootComparator;
import org.ovirt.engine.core.dal.comparators.DiskImageByDriveMappingComparator;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class CreateVDSCommand<P extends CreateVmVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    protected VM vm;
    protected XmlRpcStruct createInfo = new XmlRpcStruct();

    public CreateVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getvm_guid());
        vm = parameters.getVm();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        buildVmData();
        mVmReturn = getBroker().create(createInfo);
        logCommandInfo();
        ProceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                mVmReturn.mVm);
    }

    /**
     * Logs the command info.
     */
    private void logCommandInfo() {
        final char EQUAL = '=';
        final char SEP = ',';
        StringBuilder info = new StringBuilder();
        String sep = "";
        for (String o : createInfo.getKeys()) {
            info.append(sep);
            info.append(o);
            info.append(EQUAL);
            info.append(createInfo.getItem(o));
            sep = (new Character(SEP)).toString();
        }
        log.infoFormat("{0} {1}", getClass().getName(), info.toString());
    }

    private void buildVmData() {
        buildVmProperties();
        buildVmDrives();
        buildVmNetworkInterfaces();
        buildVmNetworkCluster();
        buildVmBootSequence();
        setVmBootOptions();
        buildVmSoundDevices();
        buildVmTimeZone();
    }

    private void buildVmProperties() {
        createInfo.add(VdsProperties.vm_guid, vm.getvm_guid().toString());
        createInfo.add(VdsProperties.vm_name, vm.getvm_name());
        createInfo.add(VdsProperties.mem_size_mb, vm.getvm_mem_size_mb());
        createInfo.add(VdsProperties.num_of_monitors,
                (new Integer(vm.getnum_of_monitors())).toString());
        createInfo.add(VdsProperties.num_of_cpus,
                (new Integer(vm.getnum_of_cpus())).toString());
        if (Config.<Boolean> GetValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.add(VdsProperties.cores_per_socket,
                    (new Integer(vm.getcpu_per_socket())).toString());
        }
        createInfo.add(VdsProperties.emulatedMachine, Config
                .<String> GetValue(ConfigValues.EmulatedMachine, vm
                        .getvds_group_compatibility_version().toString()));
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> GetValue(ConfigValues.SSLEnabled)) {
            createInfo.add(VdsProperties.spiceSslCipherSuite,
                    Config.<String> GetValue(ConfigValues.CipherSuite));
            createInfo.add(VdsProperties.SpiceSecureChannels,
                    Config.<String> GetValue(ConfigValues.SpiceSecureChannels));
        }
        createInfo.add(VdsProperties.kvmEnable, vm.getkvm_enable().toString()
                .toLowerCase());
        createInfo.add(VdsProperties.acpiEnable, vm.getacpi_enable()
                .toString().toLowerCase());

        createInfo.add(VdsProperties.Custom,
                VmPropertiesUtils.getVMProperties(vm.getStaticData()));
        createInfo
                .add(VdsProperties.display, vm.getdisplay_type().toString()); // vnc,
        // qxl
        createInfo.add(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.getRunAndPause()) {
            createInfo.add(VdsProperties.launch_paused_param, "true");
        }
        if (vm.getvds_group_cpu_flags_data() != null) {
            createInfo.add(VdsProperties.cpuType, vm.getvds_group_cpu_flags_data());
        }
        createInfo.add(VdsProperties.niceLevel,
                (new Integer(vm.getnice_level())).toString());
        if (vm.getstatus() == VMStatus.Suspended
                && !StringHelper.isNullOrEmpty(vm.gethibernation_vol_handle())) {
            createInfo.add(VdsProperties.hiberVolHandle, vm.gethibernation_vol_handle());
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

    private void buildVmDrives() {
        int[] ideIndexSlots = new int[] { 0, 1, 3 };
        int ideCount = 0, pciCount = 0;
        int i = 0;

        if (!StringHelper.isNullOrEmpty(vm.getCdPath())) {
            createInfo.add(VdsProperties.CDRom, vm.getCdPath());
        }
        if (!StringHelper.isNullOrEmpty(vm.getFloppyPath())) {
            createInfo.add(VdsProperties.Floppy, vm.getFloppyPath());
        }

        // order first by drive numbers and then order by boot for the bootable
        // drive to be first (important for IDE to be index 0) !
        List<DiskImage> diskImages = new ArrayList<DiskImage>(vm.getDiskMap()
                .values());
        Collections.sort(diskImages, new DiskImageByDriveMappingComparator());
        Collections.sort(diskImages,
                Collections.reverseOrder(new DiskImageByBootComparator()));
        List<VmDevice> diskVmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDAO()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getvm_guid(),
                                VmDeviceType.getName(VmDeviceType.DISK),
                                VmDeviceType.getName(VmDeviceType.DISK));
        Set<Guid> pluggedDiskIds = new HashSet<Guid>();
        for (VmDevice diskVmDevice : diskVmDevices) {
            if (diskVmDevice.getIsPlugged()) {
                pluggedDiskIds.add(diskVmDevice.getDeviceId());
            }
        }
        Map[] drives = new Map[pluggedDiskIds.size()];
        for (DiskImage disk : diskImages) {
            if (pluggedDiskIds.contains(disk.getDisk().getId())) {
                Map drive = new HashMap();
                drive.put("domainID", disk.getstorage_id().toString());
                drive.put("poolID", disk.getstorage_pool_id().toString());
                drive.put("volumeID", disk.getId().toString());
                drive.put("imageID", disk.getimage_group_id().toString());
                drive.put("format", disk.getvolume_format().toString()
                        .toLowerCase());
                drive.put("propagateErrors", disk.getpropagate_errors().toString()
                        .toLowerCase());

                switch (disk.getdisk_interface()) {
                case IDE:
                    drive.put("if", "ide");
                    drive.put("index", String.valueOf(ideIndexSlots[ideCount]));
                    ideCount++;
                    break;
                case VirtIO:
                    drive.put("if", "virtio");
                    drive.put("index", String.valueOf(pciCount));
                    drive.put("boot", String.valueOf(disk.getboot()).toLowerCase());
                    pciCount++;
                    break;
                default:
                    // ISCI not supported
                    break;
                }

                drives[i] = drive;
                i++;
            }
        }
        createInfo.add("drives", drives);
    }

    private void buildVmNetworkInterfaces() {
        StringBuilder macs = new StringBuilder();
        StringBuilder nics = new StringBuilder();
        StringBuilder networks = new StringBuilder();
        for (int i = 0; i < vm.getInterfaces().size(); i++) {
            macs.append(vm.getInterfaces().get(i).getMacAddress());
            networks.append(vm.getInterfaces().get(i).getNetworkName());

            VmInterfaceType ifaceType = VmInterfaceType.rtl8139;
            if (vm.getInterfaces().get(i).getType() != null) {
                ifaceType = VmInterfaceType.forValue(vm.getInterfaces().get(i)
                        .getType());
            }

            if (ifaceType == VmInterfaceType.rtl8139_pv) {
                Boolean useRtl8139_pv = Config.<Boolean> GetValue(
                        ConfigValues.UseRtl8139_pv, vm
                                .getvds_group_compatibility_version()
                                .toString());

                if (!useRtl8139_pv) {
                    if (vm.getHasAgent()) {
                        nics.append("pv");
                    } else {
                        nics.append("rtl8139");
                    }
                } else {
                    nics.append("rtl8139,pv");
                    macs.append(",");
                    macs.append(vm.getInterfaces().get(i).getMacAddress());
                    networks.append(",");
                    networks.append(vm.getInterfaces().get(i).getNetworkName());
                }
            } else {

                nics.append(ifaceType.toString());

            }

            if (i < vm.getInterfaces().size() - 1) {
                macs.append(",");
                nics.append(",");
                networks.append(",");
            }
        }
        if (!StringHelper.isNullOrEmpty(macs.toString().trim())) {
            createInfo.add(VdsProperties.mac_addr, macs.toString());
            createInfo.add(VdsProperties.nic_type, nics.toString());
            createInfo.add(VdsProperties.bridge, networks.toString());
        }
    }
    private void buildVmNetworkCluster() {
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

    private void buildVmBootSequence() {
        // get device list for the VM
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmId(vm.getvm_guid());
        String bootSeqInDB = VmDeviceCommonUtils.getBootSequence(devices).toString().toLowerCase();
        String bootSeqInBE = vm.getboot_sequence().toString().toLowerCase();
        // TODO : find another way to distinguish run vs. run-once
        if (bootSeqInBE.equals(bootSeqInDB))
            createInfo.add(VdsProperties.Boot, bootSeqInDB);
        else
            // run once
            createInfo.add(VdsProperties.Boot, bootSeqInBE);

    }

    private void setVmBootOptions() {
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
    private void buildVmSoundDevices() {
        final String OS_REGEX = "^.*%1s,([^,]*).*$";
        final String DEFAULT_TYPE = "default";

        if (vm.getvm_type() == VmType.Desktop) {

            String soundDeviceTypeConfig = Config.<String> GetValue(
                    ConfigValues.DesktopAudioDeviceType, vm
                            .getvds_group_compatibility_version().toString());
            String vmOS = vm.getos().name();

            Pattern regexPattern = Pattern.compile(String.format(OS_REGEX, vmOS));
            Matcher regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);

            if (regexMatcher.find()) {
                createInfo.add(VdsProperties.soundDevice, regexMatcher.group(1));
            } else {
                regexPattern = Pattern.compile(String
                        .format(OS_REGEX, DEFAULT_TYPE));
                regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);

                if (regexMatcher.find()) {
                    createInfo.add(VdsProperties.soundDevice,
                            regexMatcher.group(1));
                }
            }
        }
    }
    private void buildVmTimeZone() {
        // send vm_dynamic.utc_diff if exist, if not send vm_static.time_zone
        if (vm.getutc_diff() != null) {
            createInfo.add(VdsProperties.utc_diff, vm.getutc_diff()
                    .toString());
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

    private static Log log = LogFactory.getLog(CreateVDSCommand.class);
}
