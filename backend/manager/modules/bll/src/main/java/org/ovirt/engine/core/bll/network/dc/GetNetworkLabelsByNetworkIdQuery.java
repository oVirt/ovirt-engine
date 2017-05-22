package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class GetNetworkLabelsByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    public GetNetworkLabelsByNetworkIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Network network = networkDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(network == null || !NetworkUtils.isLabeled(network)
                ? Collections.<NetworkLabel> emptyList()
                : new ArrayList<>(Arrays.asList(new NetworkLabel(network.getLabel()))));
    }
}
