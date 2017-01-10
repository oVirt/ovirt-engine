package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.AmendVolumeVDSCommandParameters;

public class AmendVolumeVDSCommand<P extends AmendVolumeVDSCommandParameters> extends VdsBrokerCommand<P> {

    private StatusOnlyReturn result;

    public AmendVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Map<String, Object> volume = new HashMap<>();
        volume.put("sd_id", getParameters().getStorageDomainId().toString());
        volume.put("img_id", getParameters().getImageId().toString());
        volume.put("vol_id", getParameters().getVolumeId().toString());
        volume.put("generation", getParameters().getGeneration());
        Map<String, Object> volumeAttributes = new HashMap<>();
        volumeAttributes.put("compat", getParameters().getQcowCompat().getCompatValue());
        result = getBroker().amendVolume(getParameters().getJobId().toString(), volume, volumeAttributes);
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
