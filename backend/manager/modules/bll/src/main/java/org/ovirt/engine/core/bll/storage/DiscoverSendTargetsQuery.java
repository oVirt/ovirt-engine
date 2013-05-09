package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.vdscommands.DiscoverSendTargetsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class DiscoverSendTargetsQuery<P extends DiscoverSendTargetsQueryParameters> extends QueriesCommandBase<P> {
    public DiscoverSendTargetsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.DiscoverSendTargets,
                                new DiscoverSendTargetsVDSCommandParameters(getParameters().getVdsId(),
                                        getParameters().getConnection())).getReturnValue());
    }
}
