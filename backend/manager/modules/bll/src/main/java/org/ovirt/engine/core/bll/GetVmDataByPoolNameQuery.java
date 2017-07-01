package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.dao.VmPoolDao;


public class GetVmDataByPoolNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmPoolDao vmPoolDao;

    public GetVmDataByPoolNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = null;
        VmPool vmpool = vmPoolDao.getByName(getParameters().getName());
        if (vmpool != null) {
            QueryReturnValue getVmRet = backend.runInternalQuery(QueryType.GetVmDataByPoolId,
                    new IdQueryParameters(vmpool.getVmPoolId()));

            if (getVmRet != null) {
                vm = getVmRet.getReturnValue();
            }
        }

        getQueryReturnValue().setReturnValue(vm);
    }
}
