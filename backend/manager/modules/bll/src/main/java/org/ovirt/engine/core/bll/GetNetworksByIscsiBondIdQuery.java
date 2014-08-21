package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetNetworksByIscsiBondIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetNetworksByIscsiBondIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Network> networks = new ArrayList<>();

        for (Guid id : getNetworksIds()) {
            networks.add(getDbFacade().getNetworkDao().get(id));
        }

        getQueryReturnValue().setReturnValue(networks);
    }

    private List<Guid> getNetworksIds() {
        return getDbFacade().getIscsiBondDao().getNetworkIdsByIscsiBondId(getParameters().getId());
    }
}
