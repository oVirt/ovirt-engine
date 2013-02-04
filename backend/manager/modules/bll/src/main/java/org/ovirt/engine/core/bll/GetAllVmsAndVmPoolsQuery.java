package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class GetAllVmsAndVmPoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmsAndVmPoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        boolean isSucceeded = true;
        List<Object> retValList = new ArrayList<Object>();
        VdcQueryReturnValue queryResult = Backend.getInstance().runInternalQuery(VdcQueryType.GetAllVms, getParameters());
        if (queryResult != null && queryResult.getSucceeded()) {
            retValList.addAll((List<VM>) queryResult.getReturnValue());
        } else {
            isSucceeded = false;
        }
        if (isSucceeded) {
            queryResult =
                    Backend.getInstance().runInternalQuery(VdcQueryType.GetAllVmPoolsAttachedToUser,
                            new GetAllVmPoolsAttachedToUserParameters(getUserID()));
            if (queryResult != null && queryResult.getSucceeded()) {
                retValList.addAll((List<VmPool>) queryResult.getReturnValue());
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
