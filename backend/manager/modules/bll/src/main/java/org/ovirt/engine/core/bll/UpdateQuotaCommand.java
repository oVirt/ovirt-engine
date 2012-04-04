package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;

public class UpdateQuotaCommand<T extends QuotaCRUDParameters> extends CommandBase<T> {

    /**
     * Generated serialization UUID.
     */
    private static final long serialVersionUID = 8037593564998496651L;

    public UpdateQuotaCommand(T parameters) {
        super(parameters);
        setStoragePoolId(getParameters().getQuota() != null ? getParameters().getQuota().getStoragePoolId() : null);
        setQuota(getParameters().getQuota());
    }

    @Override
    protected boolean canDoAction() {
        if (!QuotaHelper.getInstance().checkQuotaValidationForAddEdit(getParameters().getQuota(),
                getReturnValue().getCanDoActionMessages())) {
            return false;
        } else if (getParameters().getQuota().getId() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        } else if (getQuotaDAO().getById(getParameters().getQuota().getId()) == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        setQuotaParameter();
        QuotaDAO dao = DbFacade.getInstance().getQuotaDAO();
        dao.update(getParameters().getQuota());
        getReturnValue().setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getQuotaId() == null ? null
                : getQuotaId().getValue(),
                VdcObjectType.Quota, getActionType().getActionGroup()));
    }

    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__QUOTA);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
          return getSucceeded() ? AuditLogType.USER_UPDATE_QUOTA : AuditLogType.USER_FAILED_UPDATE_QUOTA;
    }

    /**
     * Set quota from the parameter
     *
     * @param parameters
     * @return
     */
    private void setQuotaParameter() {
        Quota quotaParameter = getParameters().getQuota();
        setStoragePoolId(quotaParameter.getStoragePoolId());
        setQuotaName(quotaParameter.getQuotaName());
        if (quotaParameter.getQuotaStorages() != null) {
            for (QuotaStorage quotaStorage : quotaParameter.getQuotaStorages()) {
                quotaStorage.setQuotaId(getQuotaId());
                quotaStorage.setQuotaStorageId(Guid.NewGuid());
            }
        }
        if (quotaParameter.getQuotaVdsGroups() != null) {
            for (QuotaVdsGroup quotaVdsGroup : quotaParameter.getQuotaVdsGroups()) {
                quotaVdsGroup.setQuotaId(getQuotaId());
                quotaVdsGroup.setQuotaVdsGroupId(Guid.NewGuid());
            }
        }
        setQuota(quotaParameter);
    }
}
