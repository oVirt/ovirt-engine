package org.ovirt.engine.core.bll.quota;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.QuotaDao;

public class UpdateQuotaCommand extends QuotaCRUDCommand {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private QuotaDao quotaDao;

    public UpdateQuotaCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getQuota() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        if (getQuota().isDefault()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_DEFAULT_CANNOT_BE_CHANGED);
            return false;
        }

        return super.validate();
    }

    @Override
    protected void executeCommand() {
        removeQuotaFromCache();
        setQuota(getParameters().getQuota());
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
