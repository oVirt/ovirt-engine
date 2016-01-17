package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;

public class UpdateMacPoolCommand extends MacPoolCommandBase<MacPoolParameters> {

    private MacPool oldMacPool;

    public UpdateMacPoolCommand(MacPoolParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        addValidationGroup(UpdateEntity.class);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.MAC_POOL_EDIT_SUCCESS;
        } else {
            return AuditLogType.MAC_POOL_EDIT_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        oldMacPool = getMacPoolDao().get(getMacPoolId());
        return validate(new MacPoolValidator(oldMacPool).macPoolExists()) &&
                validate(new MacPoolValidator(getMacPoolEntity()).hasUniqueName()) &&
                validate(validateDefaultFlagIsNotChanged(oldMacPool, getMacPoolEntity()));
    }

    protected MacPool getMacPoolEntity() {
        return getParameters().getMacPool();
    }

    //used by introspector
    public Guid getMacPoolId() {
        return getMacPoolEntity().getId();
    }

    //used by introspector
    public String getMacPoolName() {
        return getMacPoolEntity().getName();
    }

    @Override
    protected void executeCommand() {
        registerRollbackHandler(new CustomTransactionCompletionListener());

        getMacPoolDao().update(getMacPoolEntity());
        poolPerDc.modifyPool(getMacPoolEntity());

        setSucceeded(true);
        getReturnValue().setActionReturnValue(getMacPoolId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid macPoolIdToUse = getParameters().getMacPool() == null ? null : getParameters().getMacPool().getId();

        return Collections.singletonList(new PermissionSubject(macPoolIdToUse,
                VdcObjectType.MacPool,
                ActionGroup.EDIT_MAC_POOL));
    }

    static ValidationResult validateDefaultFlagIsNotChanged(MacPool macPoolFromDb, MacPool newMacPool) {
        if (macPoolFromDb == null || newMacPool == null) {
            throw new IllegalArgumentException();
        }

        final boolean defaultChanged = macPoolFromDb.isDefaultPool() != newMacPool.isDefaultPool();
        return ValidationResult.failWith(
                EngineMessage.ACTION_TYPE_FAILED_CHANGING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED)
                .when(defaultChanged);
    }

    private class CustomTransactionCompletionListener extends NoOpTransactionCompletionListener {

        @Override
        public void onRollback() {
            poolPerDc.modifyPool(oldMacPool);
        }
    }
}
