package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {

    protected XmlRpcStruct sendInfo = new XmlRpcStruct();

    public HotPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        buildSendDataToVdsm();
        status = getBroker().hotplugDisk(sendInfo);
        ProceedProxyReturnValue();
    }

    protected void buildSendDataToVdsm() {
        sendInfo.add("vmId", getParameters().getVmId().toString());
        sendInfo.add("drive", initDriveData());
    }

    private XmlRpcStruct initDriveData() {
        XmlRpcStruct drive = new XmlRpcStruct();
        Disk disk = getParameters().getDisk();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.add(VdsProperties.Type, "disk");
        drive.add(VdsProperties.Device, "disk");
        addAddress(drive, getParameters().getVmDevice().getAddress());
        drive.add(VdsProperties.NETWORK_INTERFACE, disk.getDiskInterface().toString().toLowerCase());
        drive.add(VdsProperties.Shareable, String.valueOf(disk.isShareable()));
        drive.add(VdsProperties.Optional, Boolean.FALSE.toString());
        drive.add(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        drive.add(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            drive.add(VdsProperties.Format, diskImage.getVolumeFormat().toString().toLowerCase());
            drive.add(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            drive.add(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
            drive.add(VdsProperties.VolumeId, diskImage.getImageId().toString());
            drive.add(VdsProperties.ImageId, diskImage.getId().toString());
            drive.add(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString().toLowerCase());
        } else {
            LunDisk lunDisk = (LunDisk) disk;
            drive.add(VdsProperties.Guid, lunDisk.getLun().getLUN_id());
            drive.add(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
            drive.add(VdsProperties.PropagateErrors, PropagateErrors.Off.toString()
                    .toLowerCase());
        }

        return drive;
    }

    private void addAddress(XmlRpcStruct map, String address) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(address)) {
            map.add("address", XmlRpcStringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }
}
