package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;

public class GetTemplateInterfacesByTemplateIdQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {
    public GetTemplateInterfacesByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVmNetworkInterfaceDAO()
                .getAllForTemplate(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
