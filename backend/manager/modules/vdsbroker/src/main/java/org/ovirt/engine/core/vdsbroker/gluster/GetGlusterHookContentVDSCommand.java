package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterHookContentVDSCommand<P extends GlusterHookVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private GlusterHookContentInfoReturnForXmlRpc returnValue;

    public GetGlusterHookContentVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
                 return returnValue.getXmlRpcStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        returnValue =
                getBroker().glusterHookRead(getParameters().getGlusterCommand(),
                        getParameters().getHookStage().toString(),
                        getParameters().getHookName());
        setReturnValue(returnValue.getHookcontent());
        proceedProxyReturnValue();
    }
}
