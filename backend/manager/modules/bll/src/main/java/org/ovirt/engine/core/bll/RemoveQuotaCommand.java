package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RemoveQuotaCommand extends QuotaCRUDCommand {

    /**
     * Generated serialization UUID.
     */
    private static final long serialVersionUID = 8037593564997497667L;

    public RemoveQuotaCommand(QuotaCRUDParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters() == null || (getParameters().getQuotaId() == null)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        Quota quota = getQuota();

        if (quota == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        // If the quota is in use by ether VM or image - return false
        if (!QuotaEnforcementTypeEnum.DISABLED.equals(quota.getQuotaEnforcementType()) && getQuotaDAO().isQuotaInUse(quota)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IN_USE_BY_VM_OR_DISK);
            return false;
        }

        // Otherwise
        return true;
    }

    @Override
    protected void executeCommand() {
        getQuotaDAO().remove(getParameters().getQuotaId());
        QuotaManager.getInstance().removeQuotaFromCache(getQuota().getStoragePoolId(), getParameters().getQuotaId());
        getReturnValue().setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getQuotaId() == null ? null
                : getParameters().getQuotaId().getValue(),
                VdcObjectType.Quota, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__QUOTA);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DELETE_QUOTA : AuditLogType.USER_FAILED_DELETE_QUOTA;
    }
}
