package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.ColdVmBackupVDSCommandParameters;

public class AddVolumeBitmapVDSCommand<P extends ColdVmBackupVDSCommandParameters> extends VdsBrokerCommand<P> {

    private StatusOnlyReturn result;

    public AddVolumeBitmapVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Map<String, Object> volume = new HashMap<>();
        volume.put("sd_id", getParameters().getStorageDomainId().toString());
        volume.put("img_id", getParameters().getImageGroupId().toString());
        volume.put("vol_id", getParameters().getImageId().toString());
        volume.put("generation", getParameters().getGeneration());
        result = getBroker().addBitmap(getParameters().getJobId().toString(), volume, getParameters().getBitmapName());
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
