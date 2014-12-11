package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetLatestTemplateInChainQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetLatestTemplateInChainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt;
        vmt = DbFacade.getInstance().getVmTemplateDao().getTemplateWithLatestVersionInChain(getParameters().getId());
        if (vmt != null) {
            VmTemplateHandler.updateDisksFromDb(vmt);
            VmHandler.updateVmInitFromDB(vmt, true);
        }
        getQueryReturnValue().setReturnValue(vmt);
    }
}
