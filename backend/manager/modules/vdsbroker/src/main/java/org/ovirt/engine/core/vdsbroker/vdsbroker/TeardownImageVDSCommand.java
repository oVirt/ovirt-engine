package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class TeardownImageVDSCommand<P extends ImageActionsVDSCommandParameters> extends ImageActionsVDSCommandBase<P> {
    public TeardownImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusReturn executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId) {
        return getBroker().teardownImage(spId, sdId, imgGroupId, imgId);
    }
}
