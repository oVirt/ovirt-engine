package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;

public class PrepareImageVDSCommand<P extends ImageActionsVDSCommandParameters> extends ImageActionsVDSCommandBase<P> {
    public PrepareImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected PrepareImageReturnForXmlRpc executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId) {
        return getBroker().prepareImage(spId, sdId, imgGroupId, imgId);
    }
}
