package org.ovirt.engine.core.bll;

import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmPoolCommand<T extends VmPoolParametersBase> extends VmPoolCommandBase<T> {
    public RemoveVmPoolCommand(T parameters) {
        super(parameters);
        // set group id for logging and job
        if (getVmPool() != null) {
            setVdsGroupId(getVmPool().getVdsGroupId());
        }
    }

    @Override
    protected boolean canDoAction() {
        return CanRemoveVmPool(getParameters().getVmPoolId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null && CanRemoveVmPoolWithoutReasons(getVmPoolId())) {
            DbFacade.getInstance().getVmPoolDao().remove(getVmPoolId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_POOL : AuditLogType.USER_REMOVE_VM_POOL_FAILED;
    }

    public static boolean CanRemoveVmPoolWithoutReasons(NGuid vmPoolId) {
        java.util.ArrayList<String> reasons = new java.util.ArrayList<String>();
        return (CanRemoveVmPool(vmPoolId, reasons));
    }

    public static boolean CanRemoveVmPool(NGuid vmPoolId, java.util.ArrayList<String> reasons) {
        boolean returnValue = true;
        if (DbFacade.getInstance().getVmPoolDao().getVmPoolsMapByVmPoolId(vmPoolId).size() != 0) {
            returnValue = false;
            reasons.add(VdcBllMessages.VM_POOL_CANNOT_REMOVE_VM_POOL_WITH_VMS.toString());
        }
        return returnValue;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VdsGroups.name().toLowerCase(), getVdsGroupName());
        }
        return jobProperties;
    }
}
