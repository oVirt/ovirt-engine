package org.ovirt.engine.core.bll.network.template;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;

public class GetTemplateInterfacesByTemplateIdQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {
    public GetTemplateInterfacesByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVmNetworkInterfaceDao()
                .getAllForTemplate(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
