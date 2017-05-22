package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.DiscoverSendTargetsQueryParameters;
import org.ovirt.engine.core.common.vdscommands.DiscoverSendTargetsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class DiscoverSendTargetsQuery<P extends DiscoverSendTargetsQueryParameters> extends QueriesCommandBase<P> {

    public DiscoverSendTargetsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                runVdsCommand(
                        VDSCommandType.DiscoverSendTargets,
                        new DiscoverSendTargetsVDSCommandParameters(getParameters().getVdsId(),
                                getParameters().getConnection())).getReturnValue());
    }
}
