package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Collections;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterLocalPhysicalVolumeListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P>  {
    private GlusterLocalPhysicalVolumeListReturn result;

    public GetGlusterLocalPhysicalVolumeListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override protected Object getReturnValueFromBroker() {
        if (getVDSReturnValue().getSucceeded()) {
            return result.getPhysicalVolumes();
        }
        return Collections.emptyList();
    }

    @Override protected void executeVdsBrokerCommand() {
        result = getBroker().glusterPhysicalVolumeList();
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getPhysicalVolumes());
        }
    }
}
