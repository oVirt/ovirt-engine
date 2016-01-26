package org.ovirt.engine.core.bll;

import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmFromPoolRunner extends PrevalidatingMultipleActionsRunner {

    public RemoveVmFromPoolRunner(VdcActionType actionType, List<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void runCommands() {
        super.runCommands();

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

        Backend.getInstance().runInternalAction(VdcActionType.RemoveVmPool, removePoolParam, commandContext);
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
        Iterator<?> iterator = getParameters() == null ? null : getParameters().iterator();
        if (iterator != null && iterator.hasNext()) {
            Object param = iterator.next();
            if (param instanceof RemoveVmFromPoolParameters) {
                return (RemoveVmFromPoolParameters) param;
            }
        }

        return null;
    }

}
