package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T>  implements RenamedEntityInfoProvider{

    private VmPool oldPool;
    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected UpdateVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmPoolWithVmsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected Guid getPoolId() {
        getVmPoolDAO().update(getVmPool());
        return getVmPool().getVmPoolId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();
        oldPool = getVmPoolDAO().get(getVmPool().getVmPoolId());
        if (returnValue && oldPool == null) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_UPDATE_POOL_NOT_FOUND);
            returnValue = false;
        } else if (returnValue && getParameters().getVmsCount() < 0) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_DECREASE_VMS_FROM_POOL);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getAddVmsSucceded() ? AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS
                : AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        Backend.getInstance().triggerPoolMonitoringJob();
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VmPool.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldPool.getVmPoolName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVmPool().getVmPoolName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
    }
}
