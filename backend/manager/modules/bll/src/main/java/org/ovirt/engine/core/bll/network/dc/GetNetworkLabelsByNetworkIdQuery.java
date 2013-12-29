package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworkLabelsByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkLabelsByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Network network =
                getDbFacade().getNetworkDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(network == null || network.getLabel() == null
                ? Collections.<NetworkLabel> emptyList()
                : new ArrayList<NetworkLabel>(Arrays.asList((new NetworkLabel(network.getLabel())))));
    }
}
