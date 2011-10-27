package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class ListImageIdsVDSCommand<P extends ListImageIdsVDSCommandParameters> extends IrsBrokerCommand<P> {
    public ListImageIdsVDSCommand(P parameters) {
        super(parameters);
    }

    private UuidListReturnForXmlRpc uuidListReturn;

    @Override
    protected void ExecuteIrsBrokerCommand() {
        uuidListReturn = getIrsProxy().getVolumesList(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString());
        ProceedProxyReturnValue();
        setReturnValue(convertStringGuidArray(uuidListReturn.mUuidList));
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return uuidListReturn;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return uuidListReturn.mStatus;
    }
}
