package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public abstract class IrsCreateCommand<P extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    protected String mCreatedImageId = "";
    protected OneUuidReturnForXmlRpc uuidReturn;

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return uuidReturn.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return uuidReturn;
    }

    protected void BaseRollback() {
        super.Rollback();
    }

    @Override
    public void Rollback() {
        try {
            // todo - omer sending false for postZero, check that is correct
            // always (and not parameter from user)
            getIrsProxy().deleteVolume(getParameters().getStorageDomainId().toString(),
                                       getParameters().getStoragePoolId().toString(),
                                       getParameters().getImageGroupId().toString(),
                                       new String[] { mCreatedImageId },
                                       "false",
                                       "false");
        } catch (java.lang.Exception e) {
        }

        BaseRollback();
    }

    public IrsCreateCommand(P parameters) {
        super(parameters);
    }
}
