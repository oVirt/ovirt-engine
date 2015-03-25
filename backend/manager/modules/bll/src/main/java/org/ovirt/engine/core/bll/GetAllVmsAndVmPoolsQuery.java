package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class GetAllVmsAndVmPoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmsAndVmPoolsQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsAndVmPoolsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        boolean isSucceeded = true;
        List<Object> retValList = new ArrayList<>();

        // Add all VMs that the user has direct or indirect privileges on
        // that do not belong to a VM Pool
        VdcQueryReturnValue queryResult =
                runInternalQuery(VdcQueryType.GetAllVms, getParameters());
        if (queryResult != null && queryResult.getSucceeded()) {
            for (VM vm : queryResult.<List<VM>>getReturnValue()) {
                if (Guid.isNullOrEmpty(vm.getVmPoolId())) {
                    retValList.add(vm);
                }
            }
        } else {
            isSucceeded = false;
        }


        // Query for all VMs the user has direct permissions on, if
        // a user has taken a VM from VM Pool it should be returned here
        queryResult = runInternalQuery(VdcQueryType.GetAllVmsForUser, getParameters());
        if (queryResult != null && queryResult.getSucceeded()) {
            for (VM vm : queryResult.<List<VM>>getReturnValue()) {
                if (!retValList.contains(vm)) {
                    retValList.add(vm);
                }
            }
        } else {
            isSucceeded = false;
        }

        if (isSucceeded) {
            queryResult =
                    runInternalQuery(VdcQueryType.GetAllVmPoolsAttachedToUser,
                            new VdcQueryParametersBase());
            if (queryResult != null && queryResult.getSucceeded()) {
                retValList.addAll(queryResult.<List<VmPool>>getReturnValue());
            } else {
                isSucceeded = false;
            }
        }

        if (!isSucceeded) {
            getQueryReturnValue().setSucceeded(false);
            return;
        }
        getQueryReturnValue().setReturnValue(retValList);
    }
}
