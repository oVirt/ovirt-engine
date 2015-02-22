package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.linq.Predicate;

import static org.ovirt.engine.core.utils.linq.LinqUtils.filter;
import static org.ovirt.engine.core.utils.linq.LinqUtils.not;

public class GetManagementNetworkCandidatesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    @Named
    private Predicate<Network> externalNetworkPredicate;

    public GetManagementNetworkCandidatesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<Network> allDcNetworks = getNetworkDao().getAllForDataCenter(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered());
        final List<Network> managementNetworkCandidates =
                filter(allDcNetworks, getManagementNetworkCandidatePredicate());
        getQueryReturnValue().setReturnValue(managementNetworkCandidates);
    }

    private Predicate<Network> getManagementNetworkCandidatePredicate() {
        return not(getExternalNetworkPredicate());
    }

    NetworkDao getNetworkDao() {
        return networkDao;
    }

    Predicate<Network> getExternalNetworkPredicate() {
        return externalNetworkPredicate;
    }

}
