package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {

    protected Map<String, Object> sendInfo = new HashMap<String, Object>();

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
        Map<String, Object> drive = new HashMap<String, Object>();
        Disk disk = getParameters().getDisk();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.put(VdsProperties.Type, VmDeviceType.DISK.getName());
        addAddress(drive, getParameters().getVmDevice().getAddress());
        drive.put(VdsProperties.INTERFACE, disk.getDiskInterface().getName());
        drive.put(VdsProperties.Shareable, String.valueOf(disk.isShareable()));
        drive.put(VdsProperties.Optional, Boolean.FALSE.toString());
        drive.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        drive.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            drive.put(VdsProperties.Device, VmDeviceType.DISK.getName());
            drive.put(VdsProperties.Format, diskImage.getVolumeFormat().toString().toLowerCase());
            drive.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            drive.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
            drive.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
            drive.put(VdsProperties.ImageId, diskImage.getId().toString());
            drive.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString().toLowerCase());
        } else {
            LunDisk lunDisk = (LunDisk) disk;
            drive.put(VdsProperties.Device, VmDeviceType.LUN.getName());
            drive.put(VdsProperties.Guid, lunDisk.getLun().getLUN_id());
            drive.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
            drive.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString()
                    .toLowerCase());
        }

        return drive;
    }

    private void addAddress(Map<String, Object> map, String address) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(address)) {
            map.put("address", XmlRpcStringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }
}
