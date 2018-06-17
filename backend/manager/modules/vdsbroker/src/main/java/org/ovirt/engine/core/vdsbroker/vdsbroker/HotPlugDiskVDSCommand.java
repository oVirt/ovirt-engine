package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

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
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public HotPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotplugDisk(buildSendDataToVdsm());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> buildSendDataToVdsm() {
        Map<String, Object> sendInfo = new HashMap<>();
        sendInfo.put("vmId", getParameters().getVmId().toString());
        if (FeatureSupported.isDomainXMLSupported(getParameters().getVm().getClusterCompatibilityVersion())) {
            try {
                sendInfo.put(VdsProperties.engineXml, generateDomainXml());
            } catch (JAXBException e) {
                log.error("failed to create xml for hot-(un)plug", e);
                throw new RuntimeException(e);
            }
        } else {
            sendInfo.put("drive", initDriveData());
        }
        return sendInfo;
    }

    private Map<String, Object> initDriveData() {
        Map<String, Object> drive = new HashMap<>();
        Disk disk = getParameters().getDisk();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.put(VdsProperties.Type, VmDeviceType.DISK.getName());
        drive.put(VdsProperties.Address, getParameters().getAddressMap() != null ?
                getParameters().getAddressMap() : StringUtils.EMPTY);
        drive.put(VdsProperties.INTERFACE, getParameters().getDiskInterface().getName());
        if (FeatureSupported.passDiscardSupported(getParameters().getVm().getCompatibilityVersion())) {
            drive.put(VdsProperties.DISCARD, getParameters().isPassDiscard());
        }

        int numOfIoThreads = getParameters().getVm().getNumOfIoThreads();
        if (numOfIoThreads != 0 && getParameters().getDiskInterface() == DiskInterface.VirtIO) {
            if (vmDevice.getSpecParams() == null) {
                vmDevice.setSpecParams(new HashMap<>());
            }
            int pinToIoThread = vmInfoBuildUtils.nextIoThreadToPinTo(getParameters().getVm());
            vmDevice.getSpecParams().put(VdsProperties.pinToIoThread, pinToIoThread);
        }
        drive.put(VdsProperties.Shareable, vmDevice.getSnapshotId() != null ?
                VdsProperties.Transient : String.valueOf(disk.isShareable()));
        drive.put(VdsProperties.Optional, Boolean.FALSE.toString());
        drive.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getReadOnly()));
        drive.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        switch (disk.getDiskStorageType()) {
            case IMAGE:
                DiskImage diskImage = (DiskImage) disk;
                drive.put(VdsProperties.DiskType, vmInfoBuildUtils.getDiskType(getParameters().getVm(), diskImage, vmDevice));
                drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                drive.put(VdsProperties.Format, diskImage.getVolumeFormat().toString().toLowerCase());
                drive.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
                drive.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
                drive.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
                drive.put(VdsProperties.ImageId, diskImage.getId().toString());
                drive.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString().toLowerCase());

                vmInfoBuildUtils.handleIoTune(vmDevice, vmInfoBuildUtils.loadStorageQos(diskImage));

                if (vmDevice.getSpecParams() != null) {
                    drive.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
                }
                break;
            case LUN:
                LunDisk lunDisk = (LunDisk) disk;

                // If SCSI pass-through is enabled (VirtIO-SCSI/DirectLUN disk and SGIO is defined),
                // set device type as 'lun' (instead of 'disk') and set the specified SGIO
                boolean isVirtioScsi = getParameters().getDiskInterface() == DiskInterface.VirtIO_SCSI;
                boolean isScsiPassthrough = getParameters().getDisk().isScsiPassthrough();
                if (isVirtioScsi && isScsiPassthrough) {
                    drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                    drive.put(VdsProperties.Sgio, getParameters().getDisk().getSgio().toString().toLowerCase());
                } else {
                    drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                }

                drive.put(VdsProperties.Guid, lunDisk.getLun().getLUNId());
                drive.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                drive.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString().toLowerCase());
                break;
            case CINDER:
                CinderDisk cinderDisk = (CinderDisk) disk;
                vmInfoBuildUtils.buildCinderDisk(cinderDisk, drive);
                drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
                break;
        }

        return drive;
    }

    protected String generateDomainXml() throws JAXBException {
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                getParameters().getVm(),
                getVds().getId(),
                getParameters().getDisk(),
                getParameters().getVmDevice(),
                vmInfoBuildUtils);
        String libvirtXml = builder.buildHotplugDisk();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("Disk hot-plug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
