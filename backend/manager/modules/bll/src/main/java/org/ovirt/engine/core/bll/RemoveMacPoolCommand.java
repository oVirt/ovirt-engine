package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolPerDcSingleton;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveMacPoolByIdParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class RemoveMacPoolCommand extends MacPoolCommandBase<RemoveMacPoolByIdParameters> {

    private MacPool oldMacPool;

    public RemoveMacPoolCommand(RemoveMacPoolByIdParameters parameters) {
        super(parameters);
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    protected void executeCommand() {

        getMacPoolDao().remove(getParameters().getMacPoolId());
        MacPoolPerDcSingleton.getInstance().removePool(getParameters().getMacPoolId());

        getReturnValue().setSucceeded(true);
    }

    //used by introspector
    public Guid getMacPoolId() {
        return oldMacPool.getId();
    }

    //used by introspector
    public String getMacPoolName() {
        return oldMacPool.getName();
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        oldMacPool = getMacPoolDao().get(getParameters().getMacPoolId());
        final MacPoolValidator validator = new MacPoolValidator(oldMacPool);

        return validate(validator.macPoolExists()) &&
                validate(validator.notRemovingUsedPool()) &&
                validate(validator.notRemovingDefaultPool());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System, ActionGroup.CONFIGURE_ENGINE));
    }


    @Override
    public void rollback() {
        super.rollback();
        MacPoolPerDcSingleton.getInstance().createPool(oldMacPool);
    }
}
