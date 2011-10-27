package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllVmTemplatesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmTemplatesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> retval = DbFacade.getInstance().getVmTemplateDAO().getAll();
        for (VmTemplate template : retval) {
            AnonymousMethod1(template);
        }
        getQueryReturnValue().setReturnValue(retval);
    }

    private void AnonymousMethod1(VmTemplate vmt) {
        VmTemplateHandler.UpdateDisksFromDb(vmt);
    }
}
