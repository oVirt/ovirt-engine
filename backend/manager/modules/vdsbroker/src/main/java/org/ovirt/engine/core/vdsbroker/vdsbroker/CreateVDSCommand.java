package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeZoneInfo;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.comparators.DiskImageByBootComparator;
import org.ovirt.engine.core.dal.comparators.DiskImageByDriveMappingComparator;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class CreateVDSCommand<P extends CreateVmVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    protected VM mVm;
    protected XmlRpcStruct mCreateInfo = new XmlRpcStruct();

    public CreateVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getvm_guid());
        mVm = parameters.getVm();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        InitData();
        mVmReturn = getBroker().create(mCreateInfo);
        LogCommandInfo();
        ProceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(mVm.getDynamicData(), mVmReturn.mVm);
    }

    /**
     * Logs the command info.
     */
    private void LogCommandInfo() {
        final char EQUAL = '=';
        final char SEP = ',';
        StringBuilder info = new StringBuilder();
        String sep = "";
        for (String o : mCreateInfo.getKeys()) {
            info.append(sep);
            info.append(o);
            info.append(EQUAL);
            info.append(mCreateInfo.getItem(o));
            sep = (new Character(SEP)).toString();
        }
        log.infoFormat("{0} {1}", getClass().getName(), info.toString());
    }

    private void InitDesktopSoundDevice(){
        final String OS_REGEX = "^.*%1s,([^,]*).*$";
        final String DEFAULT_TYPE = "default";

        String soundDeviceTypeConfig = Config.<String> GetValue(ConfigValues.DesktopAudioDeviceType,mVm.getvds_group_compatibility_version().toString());
        String vmOS = mVm.getos().name();

        Pattern regexPattern = Pattern.compile(String.format(OS_REGEX, vmOS));
        Matcher regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);

        if (regexMatcher.find()) {
            mCreateInfo.add(VdsProperties.soundDevice, regexMatcher.group(1));
        }
        else {
            regexPattern = Pattern.compile(String.format(OS_REGEX, DEFAULT_TYPE));
            regexMatcher = regexPattern.matcher(soundDeviceTypeConfig);

            if (regexMatcher.find()) {
                mCreateInfo.add(VdsProperties.soundDevice, regexMatcher.group(1));
            }
        }
    }

    private void InitData() {
        mCreateInfo.add(VdsProperties.vm_guid, mVm.getvm_guid().toString());
        mCreateInfo.add(VdsProperties.vm_name, mVm.getvm_name());
        mCreateInfo.add(VdsProperties.mem_size_mb, mVm.getvm_mem_size_mb());
        mCreateInfo.add(VdsProperties.num_of_monitors, (new Integer(mVm.getnum_of_monitors())).toString());
        mCreateInfo.add(VdsProperties.num_of_cpus, (new Integer(mVm.getnum_of_cpus())).toString());
        if (Config.<Boolean> GetValue(ConfigValues.SendSMPOnRunVm)) {
            mCreateInfo.add(VdsProperties.cores_per_socket, (new Integer(mVm.getcpu_per_socket())).toString());
        }
        mCreateInfo.add(VdsProperties.emulatedMachine, Config.<String> GetValue(ConfigValues.EmulatedMachine, mVm
                .getvds_group_compatibility_version().toString()));
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> GetValue(ConfigValues.SSLEnabled)) {
            mCreateInfo.add(VdsProperties.spiceSslCipherSuite, Config.<String> GetValue(ConfigValues.CipherSuite));
            mCreateInfo.add(VdsProperties.SpiceSecureChannels,
                    Config.<String> GetValue(ConfigValues.SpiceSecureChannels));
        }
        if (!StringHelper.isNullOrEmpty(mVm.getCdPath())) {
            mCreateInfo.add(VdsProperties.CDRom, mVm.getCdPath());
        }
        if (!StringHelper.isNullOrEmpty(mVm.getFloppyPath())) {
            mCreateInfo.add(VdsProperties.Floppy, mVm.getFloppyPath());
        }
        mCreateInfo.add(VdsProperties.kvmEnable, mVm.getkvm_enable().toString().toLowerCase());
        mCreateInfo.add(VdsProperties.acpiEnable, mVm.getacpi_enable().toString().toLowerCase());

        mCreateInfo.add(VdsProperties.Custom, VmPropertiesUtils.getVMProperties(mVm.getStaticData()));

        InitDrivesData();

        // netowrk
        StringBuilder macs = new StringBuilder();
        StringBuilder nics = new StringBuilder();
        StringBuilder networks = new StringBuilder();
        for (int i = 0; i < mVm.getInterfaces().size(); i++) {
            macs.append(mVm.getInterfaces().get(i).getMacAddress());
            networks.append(mVm.getInterfaces().get(i).getNetworkName());

            VmInterfaceType ifaceType = VmInterfaceType.rtl8139;
            if (mVm.getInterfaces().get(i).getType() != null) {
                ifaceType = VmInterfaceType.forValue(mVm.getInterfaces().get(i).getType());
            }

            if (ifaceType == VmInterfaceType.rtl8139_pv) {
                Boolean useRtl8139_pv = Config.<Boolean> GetValue(ConfigValues.UseRtl8139_pv,
                        mVm.getvds_group_compatibility_version().toString());

                if (!useRtl8139_pv) {
                    if (mVm.getHasAgent()) {
                        nics.append("pv");
                    } else {
                        nics.append("rtl8139");
                    }
                } else {
                    nics.append("rtl8139,pv");
                    macs.append(",");
                    macs.append(mVm.getInterfaces().get(i).getMacAddress());
                    networks.append(",");
                    networks.append(mVm.getInterfaces().get(i).getNetworkName());
                }
            } else {

                nics.append(ifaceType.toString());

            }

            if (i < mVm.getInterfaces().size() - 1) {
                macs.append(",");
                nics.append(",");
                networks.append(",");
            }
        }

        // set Display network
        List<network_cluster> all = DbFacade.getInstance().getNetworkClusterDAO().getAllForCluster(
                mVm.getvds_group_id());
        // LINQ 29456
        // network_cluster networkCluster = all.FirstOrDefault(n =>
        // n.is_display);
        network_cluster networkCluster = null;
        for (network_cluster tempNetworkCluster : all) {
            if (tempNetworkCluster.getis_display()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        // LINQ 29456
        if (networkCluster != null) {
            // LINQ 29456
            // network net = DbFacade.Instance.getAllNetworks().FirstOrDefault(n
            // => n.id == networkCluster.network_id);
            network net = null;
            List<network> allNetworks = DbFacade.getInstance().getNetworkDAO().getAll();
            for (network tempNetwork : allNetworks) {
                if (tempNetwork.getId().equals(networkCluster.getnetwork_id())) {
                    net = tempNetwork;
                    break;
                }
            }
            // LINQ 29456
            if (net != null) {
                mCreateInfo.add(VdsProperties.displaynetwork, net.getname());
            }
        }

        if (!StringHelper.isNullOrEmpty(macs.toString().trim())) {
            mCreateInfo.add(VdsProperties.mac_addr, macs.toString());
            mCreateInfo.add(VdsProperties.nic_type, nics.toString());
            mCreateInfo.add(VdsProperties.bridge, networks.toString());
        }

        mCreateInfo.add(VdsProperties.display, mVm.getdisplay_type().toString()); // vnc,
                                                                                  // qxl
        mCreateInfo.add(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (mVm.getRunAndPause()) {
            mCreateInfo.add(VdsProperties.launch_paused_param, "true");
        }

        mCreateInfo.add(VdsProperties.Boot, mVm.getboot_sequence().toString().toLowerCase());

        // send vm_dynamic.utc_diff if exist, if not send vm_static.time_zone
        if (mVm.getutc_diff() != null) {
            mCreateInfo.add(VdsProperties.utc_diff, mVm.getutc_diff().toString());
        } else {
            // get vm timezone
            String timeZone = TimeZoneInfo.Local.getId();
            if (!StringHelper.isNullOrEmpty(mVm.gettime_zone())) {
                timeZone = mVm.gettime_zone();
            }

            // convert to java & calculate offset
            String javaZoneId = WindowsJavaTimezoneMapping.windowsToJava.get(timeZone);
            int offset = 0;
            if (javaZoneId != null) {
                offset = (TimeZone.getTimeZone(javaZoneId).getOffset(new Date().getTime()) / 1000);
            }
            mCreateInfo.add(VdsProperties.utc_diff, "" + offset);
        }

        if (mVm.getvm_type() == VmType.Desktop) {
            InitDesktopSoundDevice();
        }

        if (mVm.getvds_group_cpu_flags_data() != null) {
            mCreateInfo.add(VdsProperties.cpuType, mVm.getvds_group_cpu_flags_data());
        }
        mCreateInfo.add(VdsProperties.niceLevel, (new Integer(mVm.getnice_level())).toString());
        if (mVm.getstatus() == VMStatus.Suspended && !StringHelper.isNullOrEmpty(mVm.gethibernation_vol_handle())) {
            mCreateInfo.add(VdsProperties.hiberVolHandle, mVm.gethibernation_vol_handle());
        }
        mCreateInfo.add(VdsProperties.KeyboardLayout, Config.<String> GetValue(ConfigValues.VncKeyboardLayout));
        if (mVm.getvm_os().isLinux()) {
            mCreateInfo.add(VdsProperties.PitReinjection, "false");
        }

        if (mVm.getdisplay_type() == DisplayType.vnc) {
            mCreateInfo.add(VdsProperties.TabletEnable, "true");
        }

        // Boot Options
        if (!StringHelper.isNullOrEmpty(mVm.getinitrd_url())) {
            mCreateInfo.add(VdsProperties.InitrdUrl, mVm.getinitrd_url());
        }
        if (!StringHelper.isNullOrEmpty(mVm.getkernel_url())) {
            mCreateInfo.add(VdsProperties.KernelUrl, mVm.getkernel_url());

            if (!StringHelper.isNullOrEmpty(mVm.getkernel_params())) {
                mCreateInfo.add(VdsProperties.KernelParams, mVm.getkernel_params());
            }
        }

        mCreateInfo.add(VdsProperties.transparent_huge_pages, mVm.getTransparentHugePages() ? "true" : "false");

    }

    private void InitDrivesData() {
        int[] ideIndexSlots = new int[] { 0, 1, 3 };
        Map[] drives = new Map[mVm.getDiskMap().size()];
        int ideCount = 0, pciCount = 0;
        int i = 0;
        // LINQ 29456
        // order first by drive numbers and then order by boot for the bootable
        // drive to be first (important for IDE to be index 0) !
        // foreach (DiskImage disk in mVm.DiskMap.Values.OrderBy(a =>
        // int.Parse(a.internal_drive_mapping)).OrderByDescending(a => a.boot))
        java.util.List<DiskImage> diskImages = new java.util.ArrayList<DiskImage>(mVm.getDiskMap().values());
        Collections.sort(diskImages, new DiskImageByDriveMappingComparator());
        Collections.sort(diskImages, Collections.reverseOrder(new DiskImageByBootComparator()));
        for (DiskImage disk : diskImages)
        // LINQ 29456
        {
            Map drive = new HashMap();
            drive.put("domainID", disk.getstorage_id().toString());
            drive.put("poolID", disk.getstorage_pool_id().toString());
            drive.put("volumeID", disk.getId().toString());
            drive.put("imageID", disk.getimage_group_id().toString());
            drive.put("format", disk.getvolume_format().toString().toLowerCase());
            drive.put("propagateErrors", disk.getpropagate_errors().toString().toLowerCase());

            switch (disk.getdisk_interface()) {
            case IDE:
                drive.put("if", "ide");
                drive.put("index", (new Integer(ideIndexSlots[ideCount])).toString());
                ideCount++;
                break;
            case VirtIO:
                drive.put("if", "virtio");
                drive.put("bus", (new Integer(pciCount)).toString());
                drive.put("boot", (new Boolean(disk.getboot())).toString().toLowerCase());
                pciCount++;
                break;
            default:
                // ISCI not supported
                break;
            }

            drives[i] = drive;
            i++;
        }
        mCreateInfo.add("drives", drives);
    }

    private static LogCompat log = LogFactoryCompat.getLog(CreateVDSCommand.class);
}
