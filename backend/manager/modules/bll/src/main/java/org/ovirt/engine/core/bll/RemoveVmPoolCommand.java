package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmPoolCommand<T extends VmPoolParametersBase> extends VmPoolCommandBase<T> {
    public RemoveVmPoolCommand(T parameters) {
        this(parameters, null);
    }

    public RemoveVmPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        // set group id for logging and job
        if (getVmPool() != null) {
            setVdsGroupId(getVmPool().getVdsGroupId());
        }
    }

    @Override
    protected boolean canDoAction() {
        return canRemoveVmPool(getParameters().getVmPoolId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null && canRemoveVmPoolWithoutReasons(getVmPoolId())) {
            DbFacade.getInstance().getVmPoolDao().remove(getVmPoolId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_POOL : AuditLogType.USER_REMOVE_VM_POOL_FAILED;
    }

    public static boolean canRemoveVmPoolWithoutReasons(Guid vmPoolId) {
        ArrayList<String> reasons = new ArrayList<String>();
        return (canRemoveVmPool(vmPoolId, reasons));
    }

    public static boolean canRemoveVmPool(Guid vmPoolId, ArrayList<String> reasons) {
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
