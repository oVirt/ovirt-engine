package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ImageActionsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class VerifyUntrustedVolumeVDSCommand<P extends ImageActionsVDSCommandParameters> extends ImageActionsVDSCommandBase<P> {
    public VerifyUntrustedVolumeVDSCommand(P parameters) {
            super(parameters);
        }

    @Override
    protected StatusReturnForXmlRpc executeImageActionVdsBrokerCommand(String spId,
            String sdId,
            String imgGroupId,
            String imgId) {
        return getBroker().verifyUntrustedVolume(spId, sdId, imgGroupId, imgId);
    }
}
