package org.ovirt.engine.core.bll.network.template;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

public class GetTemplateInterfacesByTemplateIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public GetTemplateInterfacesByTemplateIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmNetworkInterfaceDao
                .getAllForTemplate(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
