package org.ovirt.engine.core.bll.network.dc;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetManagementNetworkCandidatesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    @Named
    private Predicate<Network> managementNetworkCandidatePredicate;

    public GetManagementNetworkCandidatesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final List<Network> allDcNetworks = networkDao.getAllForDataCenter(getParameters().getId());
        final List<Network> managementNetworkCandidates =
                allDcNetworks.stream().filter(managementNetworkCandidatePredicate).collect(Collectors.toList());
        getQueryReturnValue().setReturnValue(managementNetworkCandidates);
    }
}
