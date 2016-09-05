package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SPMGetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPMGetVolumeInfoVDSCommand<P extends SPMGetVolumeInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    private static final Logger log = LoggerFactory.getLogger(SPMGetVolumeInfoVDSCommand.class);

    public SPMGetVolumeInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        log.info("Executing GetVolumeInfo using the current SPM");

        GetVolumeInfoVDSCommandParameters params = new GetVolumeInfoVDSCommandParameters(
                getCurrentIrsProxy().getCurrentVdsId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId());
        params.setExpectedEngineErrors(getParameters().getExpectedEngineErrors());
        setVDSReturnValue(resourceManager.runVdsCommand(VDSCommandType.GetVolumeInfo, params));
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
    }
}
