package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.CopyVolumeDataVDSCommandParameters;

public class CopyVolumeDataVDSCommand<P extends CopyVolumeDataVDSCommandParameters> extends VdsBrokerCommand<P> {
    public CopyVolumeDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- executeVdsBrokerCommand: calling 'copyVolumeData'");

        status = getBroker().copyData(
                getParameters().getJobId().toString(),
                buildLocationInfo(getParameters().getSrcInfo()),
                buildLocationInfo(getParameters().getDstInfo()),
                getParameters().isCopyBitmaps());

        proceedProxyReturnValue();
    }

    private Map<?, ?> buildLocationInfo(LocationInfo info) {
        return LocationInfoHelper.prepareLocationInfoForVdsCommand(info);
    }
}
