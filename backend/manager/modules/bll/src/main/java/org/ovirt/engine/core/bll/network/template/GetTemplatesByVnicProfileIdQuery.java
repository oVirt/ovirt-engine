package org.ovirt.engine.core.bll.network.template;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetTemplatesByVnicProfileIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetTemplatesByVnicProfileIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVmTemplateDao()
                .getAllForVnicProfile(getParameters().getId()));
    }
}
