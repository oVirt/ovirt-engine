package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class VmOldInfoBuilder extends VmInfoBuilderBase {

    public VmOldInfoBuilder(VM vm, XmlRpcStruct createInfo) {
        this.vm = vm;
        this.createInfo = createInfo;
    }

    @Override
    protected void buildVmVideoCards() {
        createInfo.add(VdsProperties.display, vm.getdisplay_type().toString()); // vnc,qxl
        createInfo.add(VdsProperties.num_of_monitors, String.valueOf(vm.getnum_of_monitors()));
    }

    @Override
    protected void buildVmCD() {
        if (!StringHelper.isNullOrEmpty(vm.getCdPath())) {
            createInfo.add(VdsProperties.CDRom, vm.getCdPath());
        }
    }

    @Override
    protected void buildVmFloppy() {
        if (!StringHelper.isNullOrEmpty(vm.getFloppyPath())) {
            createInfo.add(VdsProperties.Floppy, vm.getFloppyPath());
        }
    }

    @Override
    protected void buildVmDrives() {
        Map<String, String>[] drives = new Map[vm.getDiskMap().size()];
        int ideCount = 0, pciCount = 0;
        int i = 0;
        List<DiskImage> diskImages = getSortedDiskImages();
        for (DiskImage disk : diskImages) {
            Map<String, String> drive = new HashMap<String, String>();
            drive.put("domainID", disk.getstorage_ids().get(0).toString());
            drive.put("poolID", disk.getstorage_pool_id().toString());
            drive.put("volumeID", disk.getId().toString());
            drive.put("imageID", disk.getimage_group_id().toString());
            drive.put("format", disk.getvolume_format().toString()
                        .toLowerCase());
            drive.put("propagateErrors", disk.getpropagate_errors().toString()
                        .toLowerCase());
            switch (disk.getdisk_interface()) {
            case IDE:
                try {
                    drive.put("if", "ide");
                    drive.put("index", String.valueOf(ideIndexSlots[ideCount]));
                    ideCount++;
                } catch (IndexOutOfBoundsException e) {
                    log.errorFormat("buildVmDrives throws IndexOutOfBoundsException for index {0}, IDE slots are limited to 4.",
                                ideCount);
                    throw e;
                }
                break;
            case VirtIO:
                drive.put("if", "virtio");
                drive.put("index", String.valueOf(pciCount));
                drive.put("boot", String.valueOf(disk.getboot()).toLowerCase());
                pciCount++;
                break;
            default:
                // ISCI not supported
                logUnsupportedInterfaceType();
                break;
            }

            drives[i] = drive;
            i++;
        }
        createInfo.add("drives", drives);
    }

    @Override
    protected void buildVmNetworkInterfaces() {
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

    @Override
    protected void buildVmSoundDevices() {
        if (vm.getvm_type() == VmType.Desktop) {
            createInfo.add(VdsProperties.soundDevice, getSoundDevice());
        }
    }

    protected void buildVmBootSequence() {
        // get device list for the VM
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDAO()
                .getVmDeviceByVmId(vm.getId());
        String bootSeqInDB = VmDeviceCommonUtils.getBootSequence(devices)
                .toString().toLowerCase();
        String bootSeqInBE = vm.getboot_sequence().toString().toLowerCase();
        // TODO : find another way to distinguish run vs. run-once
        if (bootSeqInBE.equals(bootSeqInDB))
            createInfo.add(VdsProperties.Boot, bootSeqInDB);
        else
            // run once
            createInfo.add(VdsProperties.Boot, bootSeqInBE);

    }
    @Override
    protected void buildUnmanagedDevices() {
        // Not supported in old code
    }
}
