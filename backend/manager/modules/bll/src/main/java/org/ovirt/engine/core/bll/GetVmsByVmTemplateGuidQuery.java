package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVmsByVmTemplateGuidQuery<P extends GetVmsByVmTemplateGuidParameters> extends QueriesCommandBase<P> {
    public GetVmsByVmTemplateGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVmDao()
                .getAllWithTemplate(getParameters().getId()));
    }
}
