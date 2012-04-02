package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.StringUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class VmInfoBuilder extends VmInfoBuilderBase {

    private final String DEVICES = "devices";
    private final List<XmlRpcStruct> devices;
    private List<VmDevice> managedDevices = null;
    private boolean hasNonDefaultBootOrder;

    public VmInfoBuilder(VM vm, XmlRpcStruct createInfo) {
        this.vm = vm;
        this.createInfo = createInfo;
        devices = new ArrayList<XmlRpcStruct>();
        hasNonDefaultBootOrder = (vm.getboot_sequence() != vm.getdefault_boot_sequence());
        if (hasNonDefaultBootOrder) {
            managedDevices = new ArrayList<VmDevice>();
        }
    }

    @Override
    protected void buildVmVideoCards() {
        createInfo.add(VdsProperties.display, vm.getdisplay_type().toString());
        // check if display type was changed in given parameters
        if (vm.getdisplay_type() != vm.getdefault_display_type()) {
            if (vm.getdisplay_type() == DisplayType.vnc) { // check spice to vnc change
                XmlRpcStruct struct = new XmlRpcStruct();
                // create a monitor as an unmanaged device
                struct.add(VdsProperties.Type, VmDeviceType.VIDEO.getName());
                struct.add(VdsProperties.Device, VmDeviceType.CIRRUS.getName());
                struct.add(VdsProperties.SpecParams, StringUtils.string2Map(getNewMonitorSpecParams()));
                devices.add(struct);
            }
        }
        else {
            // get vm device for Video Cards from DB
            List<VmDevice> vmDevices =
                DbFacade.getInstance()
                .getVmDeviceDAO()
                .getVmDeviceByVmIdAndType(vm.getId(), VmDeviceType.VIDEO.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                XmlRpcStruct struct = new XmlRpcStruct();
                struct.add(VdsProperties.Type, vmDevice.getType());
                struct.add(VdsProperties.Device, vmDevice.getDevice());
                addAddress(vmDevice, struct);
                struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
                addToManagedDevices(vmDevice);
                devices.add(struct);
            }
        }
    }

    @Override
    protected void buildVmCD() {
        XmlRpcStruct struct;
        // check first if CD was given as a parameter
        if (!StringHelper.isNullOrEmpty(vm.getCdPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.CDROM.getName(),
                            "",
                            0,
                            "",
                            true,
                            true,
                            true);
            struct = new XmlRpcStruct();
            addCdDetails(vmDevice, struct);
            addDevice(struct, vmDevice, vm.getCdPath());
        } else {
            // get vm device for this CD from DB
            List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                    VmDeviceType.DISK.getName(),
                                    VmDeviceType.CDROM.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                struct = new XmlRpcStruct();
                String file = StringUtils.string2Map(vmDevice.getSpecParams()).get(VdsProperties.Path);
                addCdDetails(vmDevice, struct);
                addAddress(vmDevice, struct);
                if (!StringHelper.isNullOrEmpty(file)) {
                    addCdOrFloppyFileDetails(file, struct);
                }
                else { // create an empty CD (path="")
                    file = "";
                }
                addDevice(struct, vmDevice, file);
                break; // currently only one is supported, may change in future releases
            }
        }
    }

    @Override
    protected void buildVmFloppy() {
        // check first if Floppy was given as a parameter
        if (!StringHelper.isNullOrEmpty(vm.getFloppyPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            "",
                            true,
                            true,
                            true);
            XmlRpcStruct struct = new XmlRpcStruct();
            addFloppyDetails(vmDevice, struct);
            addDevice(struct, vmDevice, vm.getFloppyPath());
        } else {
            // get vm device for this Floppy from DB
            List<VmDevice> vmDevices =
                DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                    VmDeviceType.DISK.getName(),
                                    VmDeviceType.FLOPPY.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                String file = StringUtils.string2Map(vmDevice.getSpecParams()).get(VdsProperties.Path);
                if (!StringHelper.isNullOrEmpty(file)) {
                    XmlRpcStruct struct = new XmlRpcStruct();
                    addFloppyDetails(vmDevice, struct);
                    addCdOrFloppyFileDetails(file, struct);
                    addDevice(struct, vmDevice, file);
                    break; // currently only one is supported, may change in future releases
                }
            }
        }
    }

    @Override
    protected void buildVmDrives() {
        // \\int ideCount = 0, pciCount = 0;
        List<DiskImage> diskImages = getSortedDiskImages();
        for (DiskImage disk : diskImages) {
            XmlRpcStruct struct = new XmlRpcStruct();
            // get vm device for this disk from DB
            VmDevice vmDevice =
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .get(new VmDeviceId(disk.getDisk().getId(), disk.getvm_guid()));
            // skip unamanged devices (handled separtely)
            if (!vmDevice.getIsManaged()) {
                continue;
            }
            if (vmDevice.getIsPlugged()) {
                struct.add(VdsProperties.Type, vmDevice.getType());
                struct.add(VdsProperties.Device, vmDevice.getDevice());
                switch (disk.getdisk_interface()) {
                case IDE:
                    struct.add(VdsProperties.Iface, "ide");
                    // \\struct.add(VdsProperties.Index, String.valueOf(ideIndexSlots[ideCount]));
                    // \\ideCount++;
                    break;
                case VirtIO:
                    struct.add(VdsProperties.Iface, VdsProperties.Virtio);
                    // struct.add(VdsProperties.Index, String.valueOf(pciCount));
                    // \\pciCount++;
                    break;
                default:
                    // ISCI not supported
                    logUnsupportedInterfaceType();
                    break;
                }
                // Insure that boot disk is created first.
                if (disk.getboot()) {
                    struct.add(VdsProperties.Index, 0);
                }
                addAddress(vmDevice, struct);
                struct.add(VdsProperties.PoolId, disk.getstorage_pool_id().toString());
                struct.add(VdsProperties.DomainId, disk.getstorage_ids().get(0).toString());
                struct.add(VdsProperties.ImageId, disk.getimage_group_id().toString());
                struct.add(VdsProperties.VolumeId, disk.getId().toString());

                addBootOrder(vmDevice, struct);
                struct.add(VdsProperties.Format, disk.getvolume_format().toString()
                        .toLowerCase());
                struct.add(VdsProperties.PropagateErrors, disk.getpropagate_errors().toString()
                        .toLowerCase());
                struct.add(VdsProperties.Optional, Boolean.FALSE.toString());
                struct.add(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
                struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
                devices.add(struct);
                addToManagedDevices(vmDevice);
            }
        }
    }

    @Override
    protected void buildVmNetworkInterfaces() {
        Boolean useRtl8139_pv = Config.<Boolean> GetValue(
                ConfigValues.UseRtl8139_pv, vm
                        .getvds_group_compatibility_version()
                        .toString());

        for (int i = 0; i < vm.getInterfaces().size(); i++) {
            XmlRpcStruct struct = new XmlRpcStruct();
            VmNetworkInterface vmInterface = vm.getInterfaces().get(i);
            // get vm device for this disk from DB
            VmDevice vmDevice =
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .get(new VmDeviceId(vmInterface.getId(), vmInterface.getVmId().getValue()));
            // skip unamanged devices (handled separtely)
            if (!vmDevice.getIsManaged()) {
                continue;
            }
            VmInterfaceType ifaceType = VmInterfaceType.rtl8139;
            if (vmInterface.getType() != null) {
                ifaceType = VmInterfaceType.forValue(vmInterface.getType());
            }
            if (ifaceType == VmInterfaceType.rtl8139_pv) {
                if (!useRtl8139_pv) {
                    if (vm.getHasAgent()) {
                        addNetworkInterfaceProperties(struct, vmInterface, vmDevice, VmInterfaceType.pv.name());
                    } else {
                        addNetworkInterfaceProperties(struct, vmInterface, vmDevice, VmInterfaceType.rtl8139.name());
                    }
                } else {
                    addNetworkInterfaceProperties(struct, vmInterface, vmDevice, VmInterfaceType.pv.name());
                    // Doual Mode: in this case we have to insert 2 interfaces with the same entries except nicModel
                    XmlRpcStruct rtl8139Struct = new XmlRpcStruct();
                    addNetworkInterfaceProperties(rtl8139Struct, vmInterface, vmDevice, VmInterfaceType.rtl8139.name());
                    devices.add(rtl8139Struct);
                }
            } else {
                addNetworkInterfaceProperties(struct, vmInterface, vmDevice, ifaceType.toString());
            }
            devices.add(struct);
            addToManagedDevices(vmDevice);
        }
    }

    @Override
    protected void buildVmSoundDevices() {
        if (vm.getvm_type() == VmType.Desktop) {
            // get vm device for Sound device from DB
            List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .getVmDeviceByVmIdAndType(vm.getId(),
                                    VmDeviceType.SOUND.getName());
            for (VmDevice vmDevice : vmDevices) {
                XmlRpcStruct struct = new XmlRpcStruct();
                struct.add(VdsProperties.Type, vmDevice.getType());
                struct.add(VdsProperties.Device, vmDevice.getDevice());
                struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
                addAddress(vmDevice, struct);
                devices.add(struct);
            }
        }
    }

    @Override
    protected void buildUnmanagedDevices() {
        Map<String, String> customMap = new HashMap<String, String>();
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDAO()
                        .getUnmanagedDevicesByVmId(vm.getId());
        if (vmDevices.size() > 0) {
            XmlRpcStruct struct = new XmlRpcStruct();
            StringBuilder id = new StringBuilder();
            for (VmDevice vmDevice : vmDevices) {
                id.append(VdsProperties.Device);
                id.append("_");
                id.append(vmDevice.getDeviceId());
                if (VmDeviceCommonUtils.isInWhiteList(vmDevice.getType(), vmDevice.getDevice())) {
                    struct.add(VdsProperties.Type, vmDevice.getType());
                    struct.add(VdsProperties.Device, vmDevice.getDevice());
                    addAddress(vmDevice, struct);
                    struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
                    devices.add(struct);
                } else {
                    customMap.put(id.toString(), vmDevice.toString());
                }
            }
        }
        createInfo.add(VdsProperties.Custom, customMap);
        XmlRpcStruct[] devArray = new XmlRpcStruct[devices.size()];
        createInfo.add(DEVICES, devices.toArray(devArray));
    }

    @Override
    protected void buildVmBootSequence() {
        //Check if boot sequence in parameters is diffrent from default boot sequence
        if (managedDevices != null) {
            // recalculate boot order from source devices and set it to target devices
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm.getStaticData(),
                    managedDevices,
                    vm.getboot_sequence(),
                    VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version()));
            for (VmDevice vmDevice : managedDevices) {
                for (XmlRpcStruct struct : devices) {
                    Object o = struct.getItem(VdsProperties.SpecParams);
                    if (o instanceof Map<?, ?>) {
                        if (vmDevice.getBootOrder() > 0
                                && StringUtils.map2String((Map<String, String>) o).contains(vmDevice.getDeviceId()
                                        .toString())) {
                            struct.add(VdsProperties.BootOrder, String.valueOf(vmDevice.getBootOrder()));
                            break;
                        }
                    }
                    else {
                        log.errorFormat("Improper value of spec_params for VM {0} : {1}", vm.getvm_name(), o.toString());
                    }
                }
            }
        }
    }

    private static void addBootOrder(VmDevice vmDevice, XmlRpcStruct struct) {
        String s = new Integer(vmDevice.getBootOrder()).toString();
        if (!org.apache.commons.lang.StringUtils.isEmpty(s) && !s.equals("0")) {
            struct.add(VdsProperties.BootOrder, s);
        }
    }

    private static void addAddress(VmDevice vmDevice, XmlRpcStruct struct) {
        Map<String, String> addressMap = StringUtils.string2Map(vmDevice.getAddress());
        if (addressMap.size() > 0) {
            struct.add(VdsProperties.Address, addressMap);
        }
    }

    private static void addNetworkInterfaceProperties(XmlRpcStruct struct,
            VmNetworkInterface vmInterface,
            VmDevice vmDevice,
            String nicModel) {
        struct.add(VdsProperties.Type, vmDevice.getType());
        struct.add(VdsProperties.Device, vmDevice.getDevice());
        struct.add(VdsProperties.network, vmInterface.getNetworkName());
        addAddress(vmDevice, struct);
        struct.add(VdsProperties.mac_addr, vmInterface.getMacAddress());
        addBootOrder(vmDevice, struct);
        struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
        struct.add(VdsProperties.nic_type, nicModel);
    }

    private void addCdOrFloppyFileDetails(String file, XmlRpcStruct struct) {
        struct.add(VdsProperties.PoolId, vm.getstorage_pool_id().toString());
        struct.add(VdsProperties.DomainId,
                DbFacade.getInstance()
                        .getStorageDomainDAO()
                        .getIsoStorageDomainIdForPool(vm.getstorage_pool_id())
                        .toString());
        struct.add(VdsProperties.ImageId, VmDeviceCommonUtils.CDROM_IMAGE_ID);
        struct.add(VdsProperties.VolumeId, file.substring(file.lastIndexOf('/') + 1));
    }

    private void addFloppyDetails(VmDevice vmDevice, XmlRpcStruct struct) {
        struct.add(VdsProperties.Type, vmDevice.getType());
        struct.add(VdsProperties.Device, vmDevice.getDevice());
        struct.add(VdsProperties.Index, "0"); // IDE slot 2 is reserved by VDSM to CDROM
        struct.add(VdsProperties.Iface, VdsProperties.Fdc);
        struct.add(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
    }

    private void addCdDetails(VmDevice vmDevice, XmlRpcStruct struct) {
        struct.add(VdsProperties.Type, vmDevice.getType());
        struct.add(VdsProperties.Device, vmDevice.getDevice());
        struct.add(VdsProperties.Index, "2"); // IDE slot 2 is reserved by VDSM to CDROM
        struct.add(VdsProperties.Iface, VdsProperties.Ide);
        struct.add(VdsProperties.ReadOnly, Boolean.TRUE.toString());
    }

    private void addDevice(XmlRpcStruct struct, VmDevice vmDevice, String path) {
        struct.add(VdsProperties.Path, path);
        struct.add(VdsProperties.SpecParams, StringUtils.string2Map(vmDevice.getSpecParams()));
        addBootOrder(vmDevice, struct);
        devices.add(struct);
        addToManagedDevices(vmDevice);
    }

    private void addToManagedDevices(VmDevice vmDevice) {
        if (managedDevices != null) {
            managedDevices.add(vmDevice);
        }
    }

    private String getNewMonitorSpecParams() {
        StringBuilder sb = new StringBuilder();
        sb.append("vram=");
        sb.append(VmDeviceCommonUtils.HIGH_VIDEO_MEM);
        sb.append(",deviceId=");
        sb.append(String.valueOf(Guid.NewGuid()));
        return sb.toString();
    }
}
