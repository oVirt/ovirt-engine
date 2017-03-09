package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsInterfacesByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private HostNicsUtil hostNicsUtil;

    public GetVdsInterfacesByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VdsNetworkInterface> interfaces =
                hostNicsUtil.findHostNics(getParameters().getId(), getUserID(), getParameters().isFiltered());

        getQueryReturnValue().setReturnValue(interfaces);
    }

}
