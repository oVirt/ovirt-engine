package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetNetworksByIscsiBondIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    @Inject
    private IscsiBondDao iscsiBondDao;

    public GetNetworksByIscsiBondIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Network> networks = new ArrayList<>();

        for (Guid id : getNetworksIds()) {
            networks.add(networkDao.get(id));
        }

        getQueryReturnValue().setReturnValue(networks);
    }

    private List<Guid> getNetworksIds() {
        return iscsiBondDao.getNetworkIdsByIscsiBondId(getParameters().getId());
    }
}
