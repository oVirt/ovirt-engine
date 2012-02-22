package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
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

    private Map<String, String> initDriveData() {
        Map<String, String> drive = new HashMap<String, String>();
        DiskImage diskImage = getParameters().getDiskImage();
        VmDevice vmDevice = getParameters().getVmDevice();

        drive.put("type", "disk");
        // drive.put("address", getParameters().getVmDevice().getAddress());
        drive.put("format", diskImage.getvolume_format().toString().toLowerCase());
        drive.put("propagateErrors", diskImage.getpropagate_errors().toString().toLowerCase());
        drive.put("iface", diskImage.getdisk_interface().toString().toLowerCase());
        drive.put("shared", Boolean.FALSE.toString());
        drive.put("optional", Boolean.FALSE.toString());
        drive.put("readonly", String.valueOf(vmDevice.getIsReadOnly()));

        drive.put("domainID", diskImage.getstorage_ids().get(0).toString());
        drive.put("poolID", diskImage.getstorage_pool_id().toString());
        drive.put("volumeID", diskImage.getId().toString());
        drive.put("imageID", diskImage.getimage_group_id().toString());
        return drive;
    }
}
