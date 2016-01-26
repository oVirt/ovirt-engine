package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVolumeDescriptionVDSCommand<P extends SetVolumeDescriptionVDSCommandParameters> extends IrsBrokerCommand<P> {
    private static final Logger log = LoggerFactory.getLogger(SetVolumeDescriptionVDSCommand.class);

    public SetVolumeDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        log.info("-- executeIrsBrokerCommand: calling 'setVolumeDescription', parameters:");
        log.info("++ spUUID={}", getParameters().getStoragePoolId());
        log.info("++ sdUUID={}", getParameters().getStorageDomainId());
        log.info("++ imageGroupGUID={}", getParameters().getImageGroupId());
        log.info("++ volUUID={}", getParameters().getImageId());
        log.info("++ description={}", getParameters().getDescription());

        status = getIrsProxy().setVolumeDescription(
                getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getDescription());

        proceedProxyReturnValue();
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
    }
}
