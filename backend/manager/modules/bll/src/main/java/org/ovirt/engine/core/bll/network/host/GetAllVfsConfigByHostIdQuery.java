package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllVfsConfigByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private HostNicVfsConfigHelper hostNicVfsConfigHelper;

    public GetAllVfsConfigByHostIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getHostNicVfsConfigHelper().getHostNicVfsConfigsWithNumVfsDataByHostId(getParameters().getId()));
    }

    public HostNicVfsConfigHelper getHostNicVfsConfigHelper() {
        return hostNicVfsConfigHelper;
    }
}
