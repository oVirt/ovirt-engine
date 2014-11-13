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
        VmTemplate vmt;
        if (getParameters().getName() != null) {
            vmt = DbFacade.getInstance().getVmTemplateDao()
                .getByName(getParameters().getName(), getUserID(), getParameters().isFiltered());
        } else {
            vmt = DbFacade.getInstance().getVmTemplateDao()
                .get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        }
        if (vmt != null) {
            VmTemplateHandler.updateDisksFromDb(vmt);
            VmHandler.updateVmInitFromDB(vmt, true);
        }
        getQueryReturnValue().setReturnValue(vmt);
    }
}
