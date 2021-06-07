package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VolumeBitmapVDSCommandParameters;

public class ClearVolumeBitmapsVDSCommand<P extends VolumeBitmapVDSCommandParameters> extends VdsBrokerCommand<P> {

    private StatusOnlyReturn result;

    public ClearVolumeBitmapsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Map<String, Object> volume = new HashMap<>();
        volume.put("sd_id", getParameters().getStorageDomainId().toString());
        volume.put("img_id", getParameters().getImageGroupId().toString());
        volume.put("vol_id", getParameters().getImageId().toString());
        volume.put("generation", getParameters().getGeneration());
        result = getBroker().clearBitmaps(getParameters().getJobId().toString(), volume);
        proceedProxyReturnValue();
    }

    @Override
    protected Status getReturnStatus() {
        return result.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
