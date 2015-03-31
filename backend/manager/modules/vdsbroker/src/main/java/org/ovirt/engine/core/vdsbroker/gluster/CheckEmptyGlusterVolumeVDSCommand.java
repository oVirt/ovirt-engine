package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BooleanReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class CheckEmptyGlusterVolumeVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private BooleanReturnForXmlRpc returnValue;

    public CheckEmptyGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return returnValue.getXmlRpcStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        returnValue = getBroker().glusterVolumeEmptyCheck(volumeName);
        proceedProxyReturnValue();
        setReturnValue(returnValue.isVolumeEmpty());
    }

}
