package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class MoveMultipleImageGroupsVDSCommand<P extends MoveMultipleImageGroupsVDSCommandParameters>
        extends IrsCreateCommand<P> {
    public MoveMultipleImageGroupsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        XmlRpcStruct imageDict = BuildImageDict();
        uuidReturn = getIrsProxy().moveMultipleImages(getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(), getParameters().getDstDomainId().toString(),
                imageDict, getParameters().getContainerId().toString());
        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.moveMultipleImages, getParameters()
                        .getStoragePoolId()));
    }

    private XmlRpcStruct BuildImageDict() {
        XmlRpcStruct imagesDict = new XmlRpcStruct();
        for (DiskImage disk : getParameters().getImagesList()) {
            imagesDict.add(disk.getimage_group_id().getValue().toString(),
                    (new Boolean(disk.getwipe_after_delete())).toString());
        }
        return imagesDict;
    }
}
