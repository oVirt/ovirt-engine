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
                getDbFacade().getVmTemplateDao().getAll(getUserID(), getParameters().isFiltered());
        for (VmTemplate template : retval) {
            VmTemplateHandler.UpdateDisksFromDb(template);
        }
        getQueryReturnValue().setReturnValue(retval);
    }
}
