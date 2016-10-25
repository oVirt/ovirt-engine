package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class UpdateQuotaCommand extends QuotaCRUDCommand {

    public UpdateQuotaCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getQuota() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        if (getQuota().isDefault()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_DEFAULT_CANNOT_BE_CHANGED);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        removeQuotaFromCache();
        setQuotaParameter();
        quotaDao.update(getParameters().getQuota());
        getReturnValue().setSucceeded(true);
        afterUpdate();
    }

    protected void removeQuotaFromCache() {
        getQuotaManager().removeQuotaFromCache(getQuota().getStoragePoolId(), getQuota().getId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getQuota().getId(),
                VdcObjectType.Quota, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__QUOTA);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_QUOTA : AuditLogType.USER_FAILED_UPDATE_QUOTA;
    }

    /**
     * Set quota from the parameter
     *
     */
    private void setQuotaParameter() {
        Quota quotaParameter = getParameters().getQuota();
        if (!quotaParameter.isEmptyStorageQuota()) {
            for (QuotaStorage quotaStorage : quotaParameter.getQuotaStorages()) {
                quotaStorage.setQuotaId(getQuotaId());
                quotaStorage.setQuotaStorageId(Guid.newGuid());
            }
        }
        if (!quotaParameter.isEmptyClusterQuota()) {
            for (QuotaCluster quotaCluster : quotaParameter.getQuotaClusters()) {
                quotaCluster.setQuotaId(getQuotaId());
                quotaCluster.setQuotaClusterId(Guid.newGuid());
            }
        }
        setQuota(quotaParameter);
    }

    protected void afterUpdate() {
        boolean newSizeUnderCurrentConsumption =
                getQuotaManager().isStorageQuotaExceeded(getQuota().getId());

        if (newSizeUnderCurrentConsumption) {
            auditLogDirector.log(this, AuditLogType.QUOTA_STORAGE_RESIZE_LOWER_THEN_CONSUMPTION);
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
