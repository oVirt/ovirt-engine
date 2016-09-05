package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetImageInfoVDSCommand<P extends GetImageInfoVDSCommandParameters> extends IrsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(GetImageInfoVDSCommand.class);

    public GetImageInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        GetVolumeInfoVDSCommandParameters p = new GetVolumeInfoVDSCommandParameters(getCurrentIrsProxy()
                .getCurrentVdsId(), getParameters().getStoragePoolId(), getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(), getParameters().getImageId());
        DiskImage di = (DiskImage) resourceManager.runVdsCommand(VDSCommandType.GetVolumeInfo, p).getReturnValue();
        // if couldn't parse image then succeeded should be false
        getVDSReturnValue().setSucceeded(di != null);
        if (!getVDSReturnValue().getSucceeded()) {
            log.error("Failed to get the volume information, marking as FAILED");
        }
        setReturnValue(di);
    }
}
