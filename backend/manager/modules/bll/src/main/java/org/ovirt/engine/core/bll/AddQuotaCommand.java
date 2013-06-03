package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AddQuotaCommand extends QuotaCRUDCommand {

    public AddQuotaCommand(QuotaCRUDParameters parameters) {
        super(parameters);
        setStoragePoolId(getParameters().getQuota() != null ? getParameters().getQuota().getStoragePoolId() : null);
    }

    @Override
    protected boolean canDoAction() {
        return (checkQuotaValidationForAdd(getParameters().getQuota(),
                getReturnValue().getCanDoActionMessages()));
    }

    public boolean checkQuotaValidationForAdd(Quota quota, List<String> messages) {
        if (!checkQuotaValidationCommon(quota, messages)) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        setQuotaParameter();
        getQuotaDAO().save(getQuota());
        getReturnValue().setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__QUOTA);
    }

    /**
     * Set quota from the parameter
     *
     * @param parameters
     * @return
     */
    private void setQuotaParameter() {
        Quota quotaParameter = getParameters().getQuota();
        quotaParameter.setId(Guid.NewGuid());
        setStoragePoolId(quotaParameter.getStoragePoolId());
        if (quotaParameter.getQuotaStorages() != null) {
            for (QuotaStorage quotaStorage : quotaParameter.getQuotaStorages()) {
                quotaStorage.setQuotaId(quotaParameter.getId());
                quotaStorage.setQuotaStorageId(Guid.NewGuid());
            }
        }
        if (quotaParameter.getQuotaVdsGroups() != null) {
            for (QuotaVdsGroup quotaVdsGroup : quotaParameter.getQuotaVdsGroups()) {
                quotaVdsGroup.setQuotaId(quotaParameter.getId());
                quotaVdsGroup.setQuotaVdsGroupId(Guid.NewGuid());
            }
        }
        setQuota(quotaParameter);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_QUOTA : AuditLogType.USER_FAILED_ADD_QUOTA;
    }
}
