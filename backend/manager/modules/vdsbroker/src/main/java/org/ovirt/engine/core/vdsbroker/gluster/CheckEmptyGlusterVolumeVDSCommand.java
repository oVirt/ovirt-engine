package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BooleanReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class CheckEmptyGlusterVolumeVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private BooleanReturn returnValue;

    public CheckEmptyGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        returnValue = getBroker().glusterVolumeEmptyCheck(volumeName);
        proceedProxyReturnValue();
        setReturnValue(returnValue.isVolumeEmpty());
    }

}
