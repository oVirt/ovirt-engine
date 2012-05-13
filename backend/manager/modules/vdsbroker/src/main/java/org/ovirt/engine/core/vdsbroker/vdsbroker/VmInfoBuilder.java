package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmPayload;
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

    private static final String SYSPREP_FILE_NAME = "sysprep.inf";
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
                struct.add(VdsProperties.SpecParams, getNewMonitorSpecParams());
                struct.add(VdsProperties.DeviceId, String.valueOf(Guid.NewGuid()));
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
                struct.add(VdsProperties.SpecParams, vmDevice.getSpecParams());
                struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
                addToManagedDevices(vmDevice);
                devices.add(struct);
            }
        }
    }

    @Override
    protected void buildVmCD() {
        XmlRpcStruct struct;
        // check if we have payload CD
        if (vm.getVmPayload() != null && vm.getVmPayload().getType() == VmDeviceType.CDROM) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.CDROM.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
                            true,
                            true,
                            true);
            struct = new XmlRpcStruct();
            addCdDetails(vmDevice, struct);
            addDevice(struct, vmDevice, "");
        }
        // check first if CD was given as a parameter
        else if (vm.isRunOnce() && !StringHelper.isNullOrEmpty(vm.getCdPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.CDROM.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
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
                // more then one device mean that we have payload and should use it
                // instead of the blank cd
                if (!VmPayload.isPayload(vmDevice.getSpecParams()) && vmDevices.size() > 1) {
                    continue;
                }
                struct = new XmlRpcStruct();
                String file = vm.getCdPath();
                addCdDetails(vmDevice, struct);
                addAddress(vmDevice, struct);
                addDevice(struct, vmDevice, file);
                break; // currently only one is supported, may change in future releases
            }
        }
    }

    @Override
    protected void buildVmFloppy() {
        // check if we have payload CD
        if (vm.getVmPayload() != null && vm.getVmPayload().getType() == VmDeviceType.FLOPPY) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
                            true,
                            true,
                            true);
            XmlRpcStruct struct = new XmlRpcStruct();
            addCdDetails(vmDevice, struct);
            addDevice(struct, vmDevice, "");
        }
        // check first if Floppy was given as a parameter
        else if (vm.isRunOnce() && !StringHelper.isNullOrEmpty(vm.getFloppyPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                            VmDeviceType.DISK.getName(),
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
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
                // more then one device mean that we have payload and should use it
                // instead of the blank cd
                if (!VmPayload.isPayload(vmDevice.getSpecParams()) && vmDevices.size() > 1) {
                    continue;
                }
                XmlRpcStruct struct = new XmlRpcStruct();
                String file = vm.getFloppyPath();
                addFloppyDetails(vmDevice, struct);
                addDevice(struct, vmDevice, file);
            }
        }
    }

    @Override
    protected void buildVmDrives() {
        // \\int ideCount = 0, pciCount = 0;
        List<Disk> disks = getSortedDisks();
        for (Disk disk : disks) {
            XmlRpcStruct struct = new XmlRpcStruct();
            // get vm device for this disk from DB
            VmDevice vmDevice =
                    DbFacade.getInstance()
                            .getVmDeviceDAO()
                            .get(new VmDeviceId(disk.getId(), disk.getvm_guid()));
            // skip unamanged devices (handled separtely)
            if (!vmDevice.getIsManaged()) {
                continue;
            }
            if (vmDevice.getIsPlugged()) {
                struct.add(VdsProperties.Type, vmDevice.getType());
                struct.add(VdsProperties.Device, vmDevice.getDevice());
                switch (disk.getDiskInterface()) {
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
                if (disk.isBoot()) {
                    struct.add(VdsProperties.Index, 0);
                }
                addAddress(vmDevice, struct);
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    struct.add(VdsProperties.PoolId, diskImage.getstorage_pool_id().toString());
                    struct.add(VdsProperties.DomainId, diskImage.getstorage_ids().get(0).toString());
                    struct.add(VdsProperties.ImageId, diskImage.getId().toString());
                    struct.add(VdsProperties.VolumeId, diskImage.getImageId().toString());
                    struct.add(VdsProperties.Format, diskImage.getvolume_format().toString()
                            .toLowerCase());
                } else {
                    LunDisk lunDisk = (LunDisk) disk;
                    struct.add("GUID", lunDisk.getLun().getLUN_id());
                }

                addBootOrder(vmDevice, struct);
                struct.add(VdsProperties.Shareable, String.valueOf(disk.isShareable()));
                struct.add(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString()
                        .toLowerCase());
                struct.add(VdsProperties.Optional, Boolean.FALSE.toString());
                struct.add(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
                struct.add(VdsProperties.SpecParams, vmDevice.getSpecParams());
                struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
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

        Map<VmDeviceId, VmDevice> devicesByDeviceId =
                Entities.businessEntitiesById(DbFacade.getInstance()
                        .getVmDeviceDAO()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                VmDeviceType.INTERFACE.getName(),
                                VmDeviceType.BRIDGE.getName()));

        for (VmNetworkInterface vmInterface : vm.getInterfaces()) {
            // get vm device for this disk from DB
            VmDevice vmDevice =
                    devicesByDeviceId.get(new VmDeviceId(vmInterface.getId(), vmInterface.getVmId().getValue()));

            if (vmDevice != null && vmDevice.getIsManaged() && vmDevice.getIsPlugged()) {

                XmlRpcStruct struct = new XmlRpcStruct();
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
                        addNetworkInterfaceProperties(rtl8139Struct,
                                vmInterface,
                                vmDevice,
                                VmInterfaceType.rtl8139.name());
                        devices.add(rtl8139Struct);
                    }
                } else {
                    addNetworkInterfaceProperties(struct, vmInterface, vmDevice, ifaceType.toString());
                }
                devices.add(struct);
                addToManagedDevices(vmDevice);
            }
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
                struct.add(VdsProperties.SpecParams, vmDevice.getSpecParams());
                struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
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
            StringBuilder id = new StringBuilder();
            for (VmDevice vmDevice : vmDevices) {
                XmlRpcStruct struct = new XmlRpcStruct();
                id.append(VdsProperties.Device);
                id.append("_");
                id.append(vmDevice.getDeviceId());
                if (VmDeviceCommonUtils.isInWhiteList(vmDevice.getType(), vmDevice.getDevice())) {
                    struct.add(VdsProperties.Type, vmDevice.getType());
                    struct.add(VdsProperties.Device, vmDevice.getDevice());
                    addAddress(vmDevice, struct);
                    struct.add(VdsProperties.SpecParams, vmDevice.getSpecParams());
                    struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
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
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm,
                    managedDevices,
                    VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version()));
            for (VmDevice vmDevice : managedDevices) {
                for (XmlRpcStruct struct : devices) {
                    String deviceId = (String) struct.getItem(VdsProperties.DeviceId);
                    if (deviceId != null && deviceId.equals(vmDevice.getDeviceId().toString())) {
                        if (vmDevice.getBootOrder() > 0) {
                            struct.add(VdsProperties.BootOrder, String.valueOf(vmDevice.getBootOrder()));
                        } else {
                            struct.getKeys().remove(VdsProperties.BootOrder);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void buildSysprepVmPayload(String sysPrepContent) {

        // We do not validate the size of the content being passed to the VM payload by VmPayload.isPayloadSizeLegal().
        // The sysprep file size isn't being verified for 3.0 clusters and below, so we maintain the same behavior here.
        VmPayload vmPayload = new VmPayload();
        vmPayload.setType(VmDeviceType.FLOPPY);
        vmPayload.setFileName(SYSPREP_FILE_NAME);
        vmPayload.setContent(Base64.encodeBase64String(sysPrepContent.getBytes()));

        VmDevice vmDevice =
                new VmDevice(new VmDeviceId(Guid.NewGuid(), vm.getId()),
                        VmDeviceType.DISK.getName(),
                        VmDeviceType.FLOPPY.getName(),
                        "",
                        0,
                        vmPayload.getSpecParams(),
                        true,
                        true,
                        true);
        XmlRpcStruct struct = new XmlRpcStruct();
        addFloppyDetails(vmDevice, struct);
        addDevice(struct, vmDevice, vm.getFloppyPath());
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
        struct.add(VdsProperties.SpecParams, vmDevice.getSpecParams());
        struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        struct.add(VdsProperties.nic_type, nicModel);
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
        Map<String, Object> specParams =
                (vmDevice.getSpecParams() == null) ? Collections.<String, Object> emptyMap() : vmDevice.getSpecParams();
        struct.add(VdsProperties.Path, path);
        struct.add(VdsProperties.SpecParams, specParams);
        struct.add(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        addBootOrder(vmDevice, struct);
        devices.add(struct);
        addToManagedDevices(vmDevice);
    }

    private void addToManagedDevices(VmDevice vmDevice) {
        if (managedDevices != null) {
            managedDevices.add(vmDevice);
        }
    }

    private HashMap<String, Object> getNewMonitorSpecParams() {
        HashMap<String, Object> specParams = new HashMap<String, Object>();
        specParams.put("vram", VmDeviceCommonUtils.HIGH_VIDEO_MEM);
        return specParams;
    }

}
