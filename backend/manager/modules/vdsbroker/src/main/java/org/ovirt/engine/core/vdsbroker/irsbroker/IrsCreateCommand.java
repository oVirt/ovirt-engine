package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public abstract class IrsCreateCommand<P extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    protected String createdImageId = "";
    protected OneUuidReturn uuidReturn;

    @Override
    protected Status getReturnStatus() {
        return uuidReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return uuidReturn;
    }

    public IrsCreateCommand(P parameters) {
        super(parameters);
    }
}
