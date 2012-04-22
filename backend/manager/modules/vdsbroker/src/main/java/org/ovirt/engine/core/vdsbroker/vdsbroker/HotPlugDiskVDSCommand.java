package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.utils.StringUtils;
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

        drive.add("type", "disk");
        drive.add("device", "disk");
        addAddress(drive, getParameters().getVmDevice().getAddress());
        drive.add("propagateErrors", disk.getPropagateErrors().toString().toLowerCase());
        drive.add("iface", disk.getDiskInterface().toString().toLowerCase());
        drive.add("shared", Boolean.FALSE.toString());
        drive.add("optional", Boolean.FALSE.toString());
        drive.add("readonly", String.valueOf(vmDevice.getIsReadOnly()));

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage)disk;
            drive.add("format", diskImage.getvolume_format().toString().toLowerCase());
            drive.add("domainID", diskImage.getstorage_ids().get(0).toString());
            drive.add("poolID", diskImage.getstorage_pool_id().toString());
            drive.add("volumeID", diskImage.getImageId().toString());
            drive.add("imageID", diskImage.getId().toString());
        }
        return drive;
    }

    private void addAddress(XmlRpcStruct map, String address) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(address)) {
            map.add("address", StringUtils.string2Map(getParameters().getVmDevice().getAddress()));
        }
    }
}
