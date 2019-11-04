package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveMacPoolByIdParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionRollbackListener;

public class RemoveMacPoolCommand extends MacPoolCommandBase<RemoveMacPoolByIdParameters> {

    private MacPool oldMacPool;

    public RemoveMacPoolCommand(RemoveMacPoolByIdParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.MAC_POOL_REMOVE_SUCCESS;
        } else {
            return AuditLogType.MAC_POOL_REMOVE_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected void executeCommand() {
        registerRollbackHandler((TransactionRollbackListener)() -> macPoolPerCluster.createPool(getOldMacPool()));

        macPoolDao.remove(getMacPoolId());
        macPoolPerCluster.removePool(getMacPoolId());

        getReturnValue().setSucceeded(true);
    }

    //used by introspector
    public Guid getMacPoolId() {
        return getParameters().getMacPoolId();
    }

    //used by introspector
    public String getMacPoolName() {
        return getOldMacPool().getName();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        final MacPoolValidator validator = new MacPoolValidator(macPoolDao.getAll(), getOldMacPool());

        return validate(validator.macPoolExists()) &&
                validate(validator.notRemovingDefaultPool()) &&
                validate(validator.notRemovingUsedPool());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getMacPoolId(),
                VdcObjectType.MacPool,
                ActionGroup.DELETE_MAC_POOL));
    }

    private MacPool getOldMacPool() {
        if (oldMacPool == null) {
            oldMacPool = macPoolDao.get(getMacPoolId());
        }
        return oldMacPool;
    }
}
