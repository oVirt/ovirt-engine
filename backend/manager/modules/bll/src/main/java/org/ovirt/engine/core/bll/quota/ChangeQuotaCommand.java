package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class ChangeQuotaCommand extends CommandBase<ChangeQuotaParameters> implements QuotaStorageDependent {

    public ChangeQuotaCommand(ChangeQuotaParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected boolean validate() {
        // check if SP exist
        if (getStoragePool() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }
        // Check if quota exist:
        if (getQuotaId() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        }
        if (getDbFacade().getQuotaDao().getById(getQuotaId()) == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getQuotaId(),
                VdcObjectType.Quota,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public abstract List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters();

    protected Guid getQuotaId() {
        return getParameters().getQuotaId();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // no implementation here already checked in getPermissionCheckSubjects
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ASSIGN);
        addValidationMessage(EngineMessage.VAR__TYPE__QUOTA);
    }
}
