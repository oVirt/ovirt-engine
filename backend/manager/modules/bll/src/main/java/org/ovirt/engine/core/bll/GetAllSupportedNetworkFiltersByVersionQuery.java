package org.ovirt.engine.core.bll;

import java.util.Collection;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.VersionQueryParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class GetAllSupportedNetworkFiltersByVersionQuery extends QueriesCommandBase<VersionQueryParameters> {

    @Inject
    private NetworkFilterDao networkFilterDao;

    public GetAllSupportedNetworkFiltersByVersionQuery(VersionQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Version version = getParameters().getVersion();
        Collection<NetworkFilter> networkFilters = networkFilterDao.getAllSupportedNetworkFiltersByVersion(version);
        getQueryReturnValue().setReturnValue(networkFilters);
    }
}
