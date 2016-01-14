package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.utils.NetworkUtils;

public class GetNetworkLabelsByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNetworkLabelsByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Network network =
                getDbFacade().getNetworkDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(network == null || !NetworkUtils.isLabeled(network)
                ? Collections.<NetworkLabel> emptyList()
                : new ArrayList<>(Arrays.asList(new NetworkLabel(network.getLabel()))));
    }
}
