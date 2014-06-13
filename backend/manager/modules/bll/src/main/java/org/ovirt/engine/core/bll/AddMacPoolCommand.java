package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolPerDcSingleton;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AddMacPoolCommand extends MacPoolCommandBase<MacPoolParameters> {

    public AddMacPoolCommand(MacPoolParameters parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.CONFIGURE_ENGINE));
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
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
        getMacPoolEntity().setId(Guid.newGuid());
        getMacPoolDao().save(getMacPoolEntity());

        MacPoolPerDcSingleton.getInstance().createPool(getMacPoolEntity());
        setSucceeded(true);
        getReturnValue().setActionReturnValue(getMacPoolEntity().getId());
    }

    @Override
    public void rollback() {
        super.rollback();
        MacPoolPerDcSingleton.getInstance().removePool(getMacPoolEntity().getId());
    }

}
