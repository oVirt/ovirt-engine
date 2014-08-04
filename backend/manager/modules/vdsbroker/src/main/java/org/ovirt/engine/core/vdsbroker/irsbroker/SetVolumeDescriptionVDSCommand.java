package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.SetVolumeDescriptionVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class SetVolumeDescriptionVDSCommand<P extends SetVolumeDescriptionVDSCommandParameters> extends IrsBrokerCommand<P> {
    private final static Log log = LogFactory.getLog(SetVolumeDescriptionVDSCommand.class);

    public SetVolumeDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        log.info("-- executeIrsBrokerCommand: calling 'setVolumeDescription' ");
        log.infoFormat("-- setVolumeDescription parameters:" + "\r\n"
                + "                spUUID={0}" + "\r\n"
                + "                sdUUID={1}" + "\r\n"
                + "                imageGroupGUID={2}" + "\r\n"
                + "                volUUID={3}" + "\r\n"
                + "                description={4}" + "\r\n",
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getDescription());

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
