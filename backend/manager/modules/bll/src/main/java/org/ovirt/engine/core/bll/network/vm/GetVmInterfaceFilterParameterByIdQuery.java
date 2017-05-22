package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;

public class GetVmInterfaceFilterParameterByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    public GetVmInterfaceFilterParameterByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmNicFilterParameterDao.get(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
