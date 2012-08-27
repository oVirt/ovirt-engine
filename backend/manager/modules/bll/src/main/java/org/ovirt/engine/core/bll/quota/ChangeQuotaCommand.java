package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public abstract class ChangeQuotaCommand extends CommandBase<ChangeQuotaParameters> implements Quotable {


    private static final long serialVersionUID = 2919605640067763395L;

    public ChangeQuotaCommand(ChangeQuotaParameters params) {
        super(params);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected boolean canDoAction() {
        // check if SP exist
        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }
        // Check if quota exist:
        if (getQuotaId() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        }
        if (getDbFacade().getQuotaDAO().getById(getQuotaId()) == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
            return false;
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getQuotaId(),
                VdcObjectType.Quota,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public abstract boolean validateAndSetQuota();

    @Override
    public abstract void rollbackQuota();

    @Override
    public Guid getQuotaId() {
        return getParameters().getQuotaId();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
        // no implementation here already checked in getPermissionCheckSubjects
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ASSIGN);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__QUOTA);
    }
}
