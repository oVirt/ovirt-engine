package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CreateVolumeContainerVDSCommand<P extends CreateVolumeVDSCommandParameters> extends StorageDomainMetadataCommand<P> {

    public CreateVolumeContainerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeDomainCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeDomainCommand: calling 'createVolumeContainer'");

        status = getBroker().createVolumeContainer(
                getParameters().getStorageDomainId().toString(),
                getParameters().getNewImageGroupId().toString(),
                (Long.valueOf(getParameters().getImageSizeInBytes())).toString(),
                getParameters().getVolumeFormat().getValue(),
                2,
                getParameters().getNewImageID().toString(),
                getParameters().getDescription(),
                getParameters().getSrcImageGroupId() != null ? getParameters().getSrcImageGroupId().toString() : Guid.Empty.toString(),
                getParameters().getSrcImageId() != null ? getParameters().getSrcImageId().toString() : Guid.Empty.toString());

        proceedProxyReturnValue();

        setReturnValue(getParameters().getNewImageID().toString());
    }
}
