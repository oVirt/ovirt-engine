package org.ovirt.engine.core.bll.network.host;


import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

public class GetOutOfSyncHostNamesForClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkImplementationDetailsUtils util;

    public GetOutOfSyncHostNamesForClusterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<String> outOfSyncVdsNames = util.getAllInterfacesOutOfSync(getParameters().getId())
            .stream()
            .map(VdsNetworkInterface::getVdsName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        getQueryReturnValue().setReturnValue(
            outOfSyncVdsNames.stream().collect(Collectors.joining("\n"))
        );
    }
}
