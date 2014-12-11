package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetVmDataByPoolNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetVmDataByPoolNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = null;
        VmPool vmpool = DbFacade.getInstance().getVmPoolDao().getByName(getParameters().getName());
        if (vmpool != null) {
            VdcQueryReturnValue getVmRet = getBackend().runInternalQuery(VdcQueryType.GetVmDataByPoolId,
                    new IdQueryParameters(vmpool.getVmPoolId()));

            if (getVmRet != null) {
                vm = getVmRet.getReturnValue();
            }
        }

        getQueryReturnValue().setReturnValue(vm);
    }
}
