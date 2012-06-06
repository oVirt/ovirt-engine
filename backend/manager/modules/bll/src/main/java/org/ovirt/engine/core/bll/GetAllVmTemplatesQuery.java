package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmTemplatesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmTemplatesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> retval =
                getDbFacade().getVmTemplateDAO().getAll(getUserID(), getParameters().isFiltered());
        for (VmTemplate template : retval) {
            AnonymousMethod1(template);
        }
        getQueryReturnValue().setReturnValue(retval);
    }

    private void AnonymousMethod1(VmTemplate vmt) {
        VmTemplateHandler.UpdateDisksFromDb(vmt);
    }
}
