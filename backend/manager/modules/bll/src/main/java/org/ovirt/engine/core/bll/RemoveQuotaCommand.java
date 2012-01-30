package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RemoveQuotaCommand<T extends QuotaCRUDParameters> extends CommandBase<T> {

    /**
     * Generated serialization UUID.
     */
    private static final long serialVersionUID = 8037593564997497667L;

    public RemoveQuotaCommand(T parameters) {
        super(parameters);
        setQuotaId(getParameters().getQuotaId());
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters() == null || (getParameters().getQuotaId() == null)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
        }

        Quota quota = getQuota();
        if (quota == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }

        // Check if there is attempt to delete the default quota while storage pool enforcement type is disabled.
        if (getStoragePoolDAO().get(quota.getStoragePoolId()).getQuotaEnforcementType() == QuotaEnforcmentTypeEnum.DISABLED && quota.getIsDefaultQuota()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_CAN_NOT_HAVE_DEFAULT_INDICATION);
        }

        // Check If we try to delete the last quota in the DC.
        List<Quota> quotaList = getQuotaDAO().getQuotaByStoragePoolGuid(getParameters().getStoragePoolId());
        if (quotaList.size() <= 1) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DATA_CENTER_MUST_HAVE_AT_LEAST_ONE_QUOTA);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        getQuotaDAO().remove(getParameters().getQuotaId());
        getReturnValue().setSucceeded(true);
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getQuotaId() == null ? null : getQuotaId().getValue(),
                VdcObjectType.Quota);
    }

    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__QUOTA);
    }
}
