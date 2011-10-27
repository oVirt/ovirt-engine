package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplateQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {
    public GetVmTemplateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt = DbFacade.getInstance().getVmTemplateDAO()
                .get(getParameters().getId());

        VmTemplateHandler.UpdateDisksFromDb(vmt);
        getQueryReturnValue().setReturnValue(vmt);
    }
}
