package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.MeasureVolumeVDSCommandParameters;

public class MeasureVolumeVDSCommand<P extends MeasureVolumeVDSCommandParameters> extends VdsBrokerCommand<P> {
    private MeasureReturn result;

    public MeasureVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    protected MeasureVolumeVDSCommand(P parameters, VDS vds) {
        super(parameters, vds);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().measureVolume(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getDstVolFormat(),
                getParameters().isWithBacking());
        proceedProxyReturnValue();
        setReturnValue(result.getVolumeRequiredSize());
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }
}
