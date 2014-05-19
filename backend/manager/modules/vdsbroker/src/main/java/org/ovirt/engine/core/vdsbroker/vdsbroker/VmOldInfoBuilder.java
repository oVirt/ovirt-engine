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
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SuppressWarnings("unchecked")
public class VmOldInfoBuilder extends VmInfoBuilderBase {

    public VmOldInfoBuilder(VM vm, Map<String, Object> createInfo) {
        this.vm = vm;
        this.createInfo = createInfo;
    }

    @Override
    protected void buildVmVideoCards() {
        createInfo.put(VdsProperties.display, vm.getDisplayType().toString()); // vnc,qxl
        createInfo.put(VdsProperties.num_of_monitors, String.valueOf(vm.getNumOfMonitors()));
    }

    @Override
    protected void buildVmCD() {
        if (!StringUtils.isEmpty(vm.getCdPath())) {
            createInfo.put(VdsProperties.CDRom, vm.getCdPath());
        }
    }

    @Override
    protected void buildVmFloppy() {
        if (!StringUtils.isEmpty(vm.getFloppyPath())) {
            createInfo.put(VdsProperties.Floppy, vm.getFloppyPath());
        }
    }

    @Override
    protected void buildVmDrives() {
        List<Map<String, String>> drives = new ArrayList<Map<String, String>>(vm.getDiskMap().size());
        int ideCount = 0, pciCount = 0;
        List<Disk> disks = getSortedDisks();
        List<VmDevice> vmDiskDevices = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdTypeAndDevice(
                vm.getId(), VmDeviceGeneralType.DISK, VmDeviceType.DISK.getName());
        for (Disk temp : disks) {

            DiskImage disk = (DiskImage) temp;
            // Get the VM device for this disk
            VmDevice vmDevice = findVmDeviceForDisk(disk.getId(), vmDiskDevices);
            if (vmDevice == null || vmDevice.getIsPlugged()) {
                Map<String, String> drive = new HashMap<String, String>();
                drive.put("domainID", disk.getStorageIds().get(0).toString());
                drive.put("poolID", disk.getStoragePoolId().toString());
                drive.put("volumeID", disk.getImageId().toString());
                drive.put("imageID", disk.getId().toString());
                drive.put("format", disk.getVolumeFormat().toString()
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

        Map<String, String>[] drivesArray = new Map[drives.size()];
        createInfo.put("drives", drives.toArray(drivesArray));
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

        createInfo.put(VdsProperties.sysprepInf, binarySysPrep);
    }

    @Override
    protected void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent) {
        // Not supported in old code
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
            VmNetworkInterface nic = vm.getInterfaces().get(i);
            macs.append(nic.getMacAddress());
            networks.append(nic.getNetworkName());

            VmInterfaceType ifaceType = VmInterfaceType.rtl8139;
            if (nic.getType() != null) {
                ifaceType = VmInterfaceType.forValue(nic.getType());
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

            if (nic.getVnicProfileId() != null) {
                VnicProfile profile = DbFacade.getInstance().getVnicProfileDao().get(nic.getVnicProfileId());

                if (profile != null) {
                    List<VNIC_PROFILE_PROPERTIES> unsupportedFeatures = new ArrayList<>();
                    if (profile.isPortMirroring()) {
                        unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.PORT_MIRRORING);
                    }

                    if (profile.getNetworkQosId() != null) {
                        unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.NETWORK_QOS);
                    }

                    if (profile.getCustomProperties() != null && !profile.getCustomProperties().isEmpty()) {
                        unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.CUSTOM_PROPERTIES);
                    }

                    reportUnsupportedVnicProfileFeatures(vm, nic, profile, unsupportedFeatures);
                }
            }
        }

        if (!StringUtils.isEmpty(macs.toString().trim())) {
            createInfo.put(VdsProperties.MAC_ADDR, macs.toString());
            createInfo.put(VdsProperties.NIC_TYPE, nics.toString());
            createInfo.put(VdsProperties.BRIDGE, networks.toString());
        }
    }

    @Override
    protected void buildVmSoundDevices() {
        List<VmDevice> vmSoundDevices = DbFacade.getInstance().getVmDeviceDao().getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.SOUND);
        if (!vmSoundDevices.isEmpty()) {
            createInfo.put(VdsProperties.soundDevice,
                    osRepository.getSoundDevice(vm.getStaticData().getOsId(),
                            vm.getVdsGroupCompatibilityVersion()));
        }
    }

    @Override
    protected void buildVmConsoleDevice() {
        // Not supported in old code
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
            createInfo.put(VdsProperties.Boot, bootSeqInDB);
        else
            // run once
            createInfo.put(VdsProperties.Boot, bootSeqInBE);

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

    @Override
    protected void buildVmWatchdog() {
        // Not supported in old code
    }

    @Override
    protected void buildVmVirtioScsi() {
        // Not supported in old code
    }

    @Override
    protected void buildVmRngDevice() {
        // Not supported in old code
    }

    @Override
    protected void buildVmVirtioSerial() {
        // Not supported in old code
    }

    @Override
    protected void buildVmNumaProperties() {
        // Not supported in old code
    }
}
