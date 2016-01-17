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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;

public class AddMacPoolCommand extends MacPoolCommandBase<MacPoolParameters> {

    public AddMacPoolCommand(MacPoolParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.MAC_POOL_ADD_SUCCESS;
        } else {
            return AuditLogType.MAC_POOL_ADD_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_MAC_POOL));
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        final MacPoolValidator validator = new MacPoolValidator(getMacPoolEntity());
        return validate(validator.defaultPoolFlagIsNotSet()) && validate(validator.hasUniqueName());
    }

    private MacPool getMacPoolEntity() {
        return getParameters().getMacPool();
    }

    @Override
    protected void executeCommand() {
        registerRollbackHandler(new CustomTransactionCompletionListener());

        getMacPoolEntity().setId(Guid.newGuid());
        getMacPoolDao().save(getMacPoolEntity());
        addPermission(getCurrentUser().getId(), getMacPoolEntity().getId());

        poolPerDc.createPool(getMacPoolEntity());
        setSucceeded(true);
        getReturnValue().setActionReturnValue(getMacPoolId());
    }

    //used by introspector
    public Guid getMacPoolId() {
        return getMacPoolEntity().getId();
    }

    //used by introspector
    public String getMacPoolName() {
        return getMacPoolEntity().getName();
    }

    private void addPermission(Guid userId, Guid macPoolId) {
        MultiLevelAdministrationHandler.addPermission(userId, macPoolId, PredefinedRoles.MAC_POOL_ADMIN, VdcObjectType.MacPool);
    }

    private class CustomTransactionCompletionListener extends NoOpTransactionCompletionListener {

        @Override
        public void onRollback() {
            poolPerDc.removePool(getMacPoolId());
        }
    }
}
