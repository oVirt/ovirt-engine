package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetLatestTemplateInChainQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetLatestTemplateInChainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt;
        vmt = vmTemplateDao.getTemplateWithLatestVersionInChain(getParameters().getId());
        if (vmt != null) {
            VmTemplateHandler.updateDisksFromDb(vmt);
            VmHandler.updateVmInitFromDB(vmt, true);
        }
        getQueryReturnValue().setReturnValue(vmt);
    }
}
