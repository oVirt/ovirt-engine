package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.SparsifyImageVDSCommandParameters;

public class SparsifyImageVDSCommand<P extends SparsifyImageVDSCommandParameters> extends VdsBrokerCommand<P> {

    private StatusOnlyReturn result;

    public SparsifyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Map<String, Object> volumeAddress = new HashMap<>();
        volumeAddress.put("sd_id", getParameters().getStorageDomainId().toString());
        volumeAddress.put("img_id", getParameters().getImageId().toString());
        volumeAddress.put("vol_id", getParameters().getVolumeId().toString());

        result = getBroker().sparsifyVolume(getParameters().getJobId().toString(), volumeAddress);
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
