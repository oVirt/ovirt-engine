package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveQuotaCommand extends QuotaCRUDCommand {

    public RemoveQuotaCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getParameters() == null || (getParameters().getQuotaId() == null)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        Quota quota = getQuota();

        if (quota == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        // If the quota is in use by ether VM or image - return false
        if (!QuotaEnforcementTypeEnum.DISABLED.equals(quota.getQuotaEnforcementType()) && getQuotaDao().isQuotaInUse(quota)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IN_USE_BY_VM_OR_DISK);
            return false;
        }

        // Otherwise
        return true;
    }

    @Override
    protected void executeCommand() {
        getQuotaDao().remove(getParameters().getQuotaId());
        getQuotaManager().removeQuotaFromCache(getQuota().getStoragePoolId(), getParameters().getQuotaId());
        getReturnValue().setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getQuotaId(),
                VdcObjectType.Quota, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__QUOTA);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DELETE_QUOTA : AuditLogType.USER_FAILED_DELETE_QUOTA;
    }
}
