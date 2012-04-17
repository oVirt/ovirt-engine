package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTemplateInterfacesByTemplateIdQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {
    public GetTemplateInterfacesByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVmNetworkInterfaceDAO()
                .getAllForTemplate(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
