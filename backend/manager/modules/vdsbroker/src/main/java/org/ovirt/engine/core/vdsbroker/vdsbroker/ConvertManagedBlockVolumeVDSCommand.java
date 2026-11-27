package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ConvertManagedBlockVolumeVDSCommandParameters;

public class ConvertManagedBlockVolumeVDSCommand extends VdsBrokerCommand<ConvertManagedBlockVolumeVDSCommandParameters> {

    public ConvertManagedBlockVolumeVDSCommand(ConvertManagedBlockVolumeVDSCommandParameters parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().convertManagedBlockVolume(
                getParameters().getStorageDomainId(),
                getParameters().getSrcVolId(),
                getParameters().getDstVolId(),
                getParameters().getSrcFormat(),
                getParameters().getDstFormat());
        proceedProxyReturnValue();
        setReturnValue(status);
    }
}
