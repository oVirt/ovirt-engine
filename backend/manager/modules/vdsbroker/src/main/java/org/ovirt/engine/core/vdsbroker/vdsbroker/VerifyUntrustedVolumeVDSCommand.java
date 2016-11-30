package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class VerifyUntrustedVolumeVDSCommand<P extends ImageActionsVDSCommandParameters> extends ImageActionsVDSCommandBase<P> {
    public VerifyUntrustedVolumeVDSCommand(P parameters) {
            super(parameters);
        }

    @Override
    protected StatusReturn executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId) {
        return getBroker().verifyUntrustedVolume(spId, sdId, imgGroupId, imgId);
    }
}
