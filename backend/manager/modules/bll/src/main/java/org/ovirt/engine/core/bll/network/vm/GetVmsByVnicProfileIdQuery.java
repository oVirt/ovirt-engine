package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsByVnicProfileIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    public GetVmsByVnicProfileIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmDao.getAllForVnicProfile(getParameters().getId()));
    }
}
