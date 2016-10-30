package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplateCountQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetVmTemplateCountQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmTemplateDao.getCount());
    }
}
