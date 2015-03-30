package org.ovirt.engine.core.bll;

import java.util.List;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T>  implements RenamedEntityInfoProvider{

    private VmPool oldPool;

    /**
     * Constructor for command creation when compensation is applied on startup
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
        if (!super.canDoAction()) {
            return false;
        }

        oldPool = getVmPoolDAO().get(getVmPool().getVmPoolId());
        if (oldPool == null) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_UPDATE_POOL_NOT_FOUND);
        }

        if (getParameters().getVmsCount() < 0) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_DECREASE_VMS_FROM_POOL);
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return isAddVmsSucceded() ? AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS
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
        return oldPool.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVmPool().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
