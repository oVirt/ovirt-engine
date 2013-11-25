package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class GetVmTemplateCountQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetVmTemplateCountQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getVmTemplateDao().getCount());
    }

    private VmTemplateDAO getVmTemplateDao() {
        return getDbFacade().getVmTemplateDao();
    }
}
