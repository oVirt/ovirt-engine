package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {

    protected Map<String, Object> sendInfo = new HashMap<>();

    public HotPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildSendDataToVdsm();
        status = getBroker().hotplugDisk(sendInfo);
        proceedProxyReturnValue();
    }

    protected void buildSendDataToVdsm() {
        sendInfo.put("vmId", getParameters().getVmId().toString());
        sendInfo.put("drive", initDriveData());
    }

    private Map<String, Object> initDriveData() {
        Map<String, Object> drive = new HashMap<>();
        Disk disk = getParameters().getDisk();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.put(VdsProperties.Type, VmDeviceType.DISK.getName());
        drive.put(VdsProperties.Address, getParameters().getAddressMap() != null ?
                getParameters().getAddressMap() : StringUtils.EMPTY);
        drive.put(VdsProperties.INTERFACE, disk.getDiskInterface().getName());

        int numOfIoThreads = getParameters().getVm().getNumOfIoThreads();
        if (numOfIoThreads != 0 && disk.getDiskInterface() == DiskInterface.VirtIO) {
            if (vmDevice.getSpecParams() == null) {
                vmDevice.setSpecParams(new HashMap<>());
            }

            List<Disk> allDisks = DbFacade.getInstance().getDiskDao().getAllForVm(getParameters().getVmId(), false);
            int numOfAttachedVirtioInterfaces = 0;
            for (Disk oneDisk : allDisks) {
                if (oneDisk.getPlugged() && oneDisk.getDiskInterface() == DiskInterface.VirtIO) {
                    numOfAttachedVirtioInterfaces ++;
                }
            }

            int pinToIoThread = numOfAttachedVirtioInterfaces % numOfIoThreads + 1;
            vmDevice.getSpecParams().put(VdsProperties.pinToIoThread, pinToIoThread);
        }
        drive.put(VdsProperties.Shareable,
                (vmDevice.getSnapshotId() != null && FeatureSupported.hotPlugDiskSnapshot(getParameters().getVm()
                        .getVdsGroupCompatibilityVersion())) ? VdsProperties.Transient
                        : String.valueOf(disk.isShareable()));
        drive.put(VdsProperties.Optional, Boolean.FALSE.toString());
        drive.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        drive.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        switch (disk.getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) disk;
                drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                drive.put(VdsProperties.Format, diskImage.getVolumeFormat().toString().toLowerCase());
                drive.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
                drive.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
                drive.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
                drive.put(VdsProperties.ImageId, diskImage.getId().toString());
                drive.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString().toLowerCase());

                VmInfoBuilder.handleIoTune(getParameters().getVm(), vmDevice, diskImage,
                        new HashMap<>(), new HashMap<>());
                if (vmDevice.getSpecParams() != null) {
                    drive.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
                }
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) disk;

                // If SCSI pass-through is enabled (VirtIO-SCSI/DirectLUN disk and SGIO is defined),
                // set device type as 'lun' (instead of 'disk') and set the specified SGIO
                boolean isVirtioScsi = getParameters().getDisk().getDiskInterface() == DiskInterface.VirtIO_SCSI;
                boolean isScsiPassthrough = getParameters().getDisk().isScsiPassthrough();
                if (isVirtioScsi) {
                    if (isScsiPassthrough) {
                        drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                        drive.put(VdsProperties.Sgio, getParameters().getDisk().getSgio().toString().toLowerCase());
                    }
                    else {
                        drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                    }
                }
                else {
                    drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                }

                drive.put(VdsProperties.Guid, lunDisk.getLun().getLUN_id());
                drive.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                drive.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString()
                        .toLowerCase());
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) disk;
                VmInfoBuilder.buildCinderDisk(cinderDisk, drive);
                drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                break;
        }

        return drive;
    }
}
