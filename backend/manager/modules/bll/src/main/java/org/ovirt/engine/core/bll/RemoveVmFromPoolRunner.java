package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmFromPoolRunner extends MultipleActionsRunner {

    public RemoveVmFromPoolRunner(VdcActionType actionType, List<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    protected void RunCommands() {
        super.RunCommands();

        // remove also the pool if there is no VMs left in it
        removePoolIfNeeded();
    }

    /**
     * If there is no VMs left in the pool, removes the pool itself
     */
    private void removePoolIfNeeded() {
        if (!isPoolRemovalEnabled()) {
            return;
        }

        Guid poolId = getPoolId();

        if (poolId == null) {
            return;
        }

        boolean allVmsRemoved = DbFacade.getInstance().getVmPoolDao().getVmPoolsMapByVmPoolId(poolId).size() == 0;
        if (!allVmsRemoved) {
            return;
        }

        VmPoolParametersBase removePoolParam = new VmPoolParametersBase(poolId);
        removePoolParam.setSessionId(getSessionId());

        Backend.getInstance().runInternalAction(VdcActionType.RemoveVmPool, removePoolParam);
    }

    private boolean isPoolRemovalEnabled() {
        return getFirstParam() != null ? getFirstParam().isRemovePoolUponDetachAllVMs() : false;
    }

    private String getSessionId() {
        return getFirstParam() != null ? getFirstParam().getSessionId() : null;
    }

    private Guid getPoolId() {
        return getFirstParam() != null ? getFirstParam().getVmPoolId() : null;
    }

    private RemoveVmFromPoolParameters getFirstParam() {
        List<VdcActionParametersBase> parameters = getParameters();
        if (parameters != null && parameters.size() != 0) {
            VdcActionParametersBase param = parameters.get(0);
            if (param != null && param instanceof RemoveVmFromPoolParameters) {
                return ((RemoveVmFromPoolParameters) param);
            }
        }

        return null;
    }

}
