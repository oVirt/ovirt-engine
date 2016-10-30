package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class GetVnicProfilesByDataCenterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VnicProfileViewDao vnicProfileViewDao;

    public GetVnicProfilesByDataCenterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vnicProfileViewDao
                .getAllForDataCenter(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
