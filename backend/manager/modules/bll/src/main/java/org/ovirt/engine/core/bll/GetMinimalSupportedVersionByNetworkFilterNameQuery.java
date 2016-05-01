package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class GetMinimalSupportedVersionByNetworkFilterNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkFilterDao networkFilterDao;

    public GetMinimalSupportedVersionByNetworkFilterNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String filterName = getParameters().getName();
        final Version minimalSupportedVersion = networkFilterDao.getMinimalSupportedVersionByNetworkFilterName(filterName);
        getQueryReturnValue().setReturnValue(minimalSupportedVersion);
    }
}
