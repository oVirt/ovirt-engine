package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.StorageDomainVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class CleanStorageDomainMetaDataVDSCommand<P extends StorageDomainVdsCommandParameters> extends VdsBrokerCommand<P> {

    public CleanStorageDomainMetaDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().cleanStorageDomainMetaData(getParameters().getStorageDomainId().toString(),
                        Guid.Empty.toString());
        proceedProxyReturnValue();
    }
}
