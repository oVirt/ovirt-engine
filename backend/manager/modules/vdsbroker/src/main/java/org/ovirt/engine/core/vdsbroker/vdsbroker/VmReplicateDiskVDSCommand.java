package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public abstract class VmReplicateDiskVDSCommand<P extends VmReplicateDiskParameters> extends VdsBrokerCommand<P> {

    public VmReplicateDiskVDSCommand(P parameters) {
        super(parameters);
    }

    protected XmlRpcStruct getDstDiskXmlRpc() {
        XmlRpcStruct dstDisk = new XmlRpcStruct();
        dstDisk.add(VdsProperties.Device, "disk");
        dstDisk.add(VdsProperties.PoolId, getParameters().getStoragePoolId().toString());
        dstDisk.add(VdsProperties.DomainId, getParameters().getTargetStorageDomainId().toString());
        dstDisk.add(VdsProperties.ImageId, getParameters().getImageGroupId().toString());
        dstDisk.add(VdsProperties.VolumeId, getParameters().getImageId().toString());
        return dstDisk;
    }

    protected XmlRpcStruct getSrcDiskXmlRpc() {
        XmlRpcStruct srcDisk = new XmlRpcStruct();
        srcDisk.add(VdsProperties.Device, "disk");
        srcDisk.add(VdsProperties.PoolId, getParameters().getStoragePoolId().toString());
        srcDisk.add(VdsProperties.DomainId, getParameters().getSrcStorageDomainId().toString());
        srcDisk.add(VdsProperties.ImageId, getParameters().getImageGroupId().toString());
        srcDisk.add(VdsProperties.VolumeId, getParameters().getImageId().toString());
        return srcDisk;
    }
}

