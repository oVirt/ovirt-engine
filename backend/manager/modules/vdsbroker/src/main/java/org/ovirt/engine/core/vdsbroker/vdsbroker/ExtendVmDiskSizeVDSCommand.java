package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ExtendVmDiskSizeVDSCommandParameters;

public class ExtendVmDiskSizeVDSCommand <P extends ExtendVmDiskSizeVDSCommandParameters> extends VdsBrokerCommand<P> {

    private ImageSizeReturn result;

    public ExtendVmDiskSizeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().diskSizeExtend(
                getParameters().getVmId().toString(),
                getParameters().getDriveSpecs(),
                String.valueOf(getParameters().getNewSize())
        );

        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getImageSize());
        }
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }
}
