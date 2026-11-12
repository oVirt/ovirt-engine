package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.DeleteImageUnusedLinksVDSCommandParameters;

public class DeleteImageUnusedLinksVDSCommand<P extends DeleteImageUnusedLinksVDSCommandParameters>
        extends VdsBrokerCommand<P> {

    protected StatusOnlyReturn statusReturn;

    public DeleteImageUnusedLinksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return statusReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return statusReturn;
    }

    @Override
    public Object getReturnValue() {
        return statusReturn;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        DeleteImageUnusedLinksVDSCommandParameters params = getParameters();
        statusReturn = getBroker().deleteImageUnusedLinks(
                params.getSdUUID().toString(),
                params.getSpUUID().toString(),
                params.getImgUUID().toString());

        proceedProxyReturnValue();
    }
}
