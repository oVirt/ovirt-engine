package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.PrepareImageVDSCommandParameters;

public class PrepareImageVDSCommand<P extends PrepareImageVDSCommandParameters> extends ImageActionsVDSCommandBase<P> {
    private PrepareImageReturn prepareImageReturn;

    public PrepareImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected PrepareImageReturn executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId) {
        prepareImageReturn =
                getBroker().prepareImage(spId, sdId, imgGroupId, imgId, getParameters().getAllowIllegal());
        proceedProxyReturnValue();
        return prepareImageReturn;
    }

    @Override
    protected Status getReturnStatus() {
        return prepareImageReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return prepareImageReturn;
    }
}
