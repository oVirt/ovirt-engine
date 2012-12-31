package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class VmOldInfoBuilder extends VmInfoBuilderBase {

    public VmOldInfoBuilder(VM vm, XmlRpcStruct createInfo) {
        this.vm = vm;
        this.createInfo = createInfo;
    }

    @Override
    protected void buildVmVideoCards() {
        createInfo.add(VdsProperties.display, vm.getDisplayType().toString()); // vnc,qxl
        createInfo.add(VdsProperties.num_of_monitors, String.valueOf(vm.getNumOfMonitors()));
    }

    @Override
    protected void buildVmCD() {
        if (!StringUtils.isEmpty(vm.getCdPath())) {
            createInfo.add(VdsProperties.CDRom, vm.getCdPath());
        }
    }

    @Override
    protected void buildVmFloppy() {
        if (!StringUtils.isEmpty(vm.getFloppyPath())) {
            createInfo.add(VdsProperties.Floppy, vm.getFloppyPath());
        }
    }

    @Override
    protected void buildVmDrives() {
        List<Map<String, String>> drives = new ArrayList<Map<String, String>>(vm.getDiskMap().size());
        int ideCount = 0, pciCount = 0;
        List<Disk> disks = getSortedDisks();
        List<VmDevice> vmDiskDevices = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                vm.getId(), VmDeviceType.DISK.getName(), VmDeviceType.DISK.getName());
        for (Disk temp : disks) {

            DiskImage disk = (DiskImage) temp;
            // Get the VM device for this disk
            VmDevice vmDevice = findVmDeviceForDisk(disk.getId(), vmDiskDevices);
            if (vmDevice == null || vmDevice.getIsPlugged()) {
                Map<String, String> drive = new HashMap<String, String>();
                drive.put("domainID", disk.getstorage_ids().get(0).toString());
                drive.put("poolID", disk.getstorage_pool_id().toString());
                drive.put("volumeID", disk.getImageId().toString());
                drive.put("imageID", disk.getId().toString());
                drive.put("format", disk.getvolume_format().toString()
                        .toLowerCase());
                drive.put("propagateErrors", disk.getPropagateErrors().toString()
                        .toLowerCase());
                switch (disk.getDiskInterface()) {
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
                    drive.put("boot", String.valueOf(disk.isBoot()).toLowerCase());
                    pciCount++;
                    break;
                default:
                    // ISCI not supported
                    logUnsupportedInterfaceType();
                    break;
                }

                drives.add(drive);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, String>[] drivesArray = new Map[drives.size()];
        createInfo.add("drives", drives.toArray(drivesArray));
    }

    private static final String UTF8_CHARSET_ENCODING = "UTF8";

    @Override
    protected void buildSysprepVmPayload(String strSysPrepContent) {
        byte[] binarySysPrep;

        try {
            binarySysPrep = strSysPrepContent.getBytes(UTF8_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported charset while building VM sysprep", e);
        }

        createInfo.add(VdsProperties.sysprepInf, binarySysPrep);
    }

    /**
     * Find VM device for this disk from the list of VM devices.
     *
     * @param diskId
     *            The disk ID to find the device for.
     * @param vmDiskDevices
     *            The list of all VM's disk devices.
     * @return The device, or null if none found.
     */
    private static VmDevice findVmDeviceForDisk(final Guid diskId, List<VmDevice> vmDiskDevices) {
        return (VmDevice) CollectionUtils.find(vmDiskDevices, new Predicate() {

            @Override
            public boolean evaluate(Object device) {
                return diskId.equals(((VmDevice) device).getDeviceId());
            }
        });
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
                if (vm.getHasAgent()) {
                    nics.append("pv");
                } else {
                    nics.append("rtl8139");
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
        if (!StringUtils.isEmpty(macs.toString().trim())) {
            createInfo.add(VdsProperties.mac_addr, macs.toString());
            createInfo.add(VdsProperties.nic_type, nics.toString());
            createInfo.add(VdsProperties.bridge, networks.toString());
        }
    }

    @Override
    protected void buildVmSoundDevices() {
        if (vm.getVmType() == VmType.Desktop) {
            createInfo.add(VdsProperties.soundDevice, getSoundDevice(vm.getStaticData(), vm.getVdsGroupCompatibilityVersion()));
        }
    }

    @Override
    protected void buildVmBootSequence() {
        // get device list for the VM
        List<VmDevice> devices = DbFacade.getInstance().getVmDeviceDao()
                .getVmDeviceByVmId(vm.getId());
        String bootSeqInDB = VmDeviceCommonUtils.getBootSequence(devices)
                .toString().toLowerCase();
        String bootSeqInBE = vm.getBootSequence().toString().toLowerCase();
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

    @Override
    protected void buildVmUsbDevices() {
        // Not supported in old code
    }

    @Override
    protected void buildVmMemoryBalloon() {
        // Not supported in old code
    }
}
