package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetLatestTemplateInChainQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    public GetLatestTemplateInChainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VmTemplate vmt;
        vmt = vmTemplateDao.getTemplateWithLatestVersionInChain(getParameters().getId());
        if (vmt != null) {
            vmTemplateHandler.updateDisksFromDb(vmt);
            vmHandler.updateVmInitFromDB(vmt, true);
        }
        getQueryReturnValue().setReturnValue(vmt);
    }
}
