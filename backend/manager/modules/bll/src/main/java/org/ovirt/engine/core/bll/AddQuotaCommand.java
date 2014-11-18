package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

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
        if (getParameters().isCopyPermissions()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    return executeAddQutoa();
                }
            });
        } else {
            executeAddQutoa();
        }
    }

    private Void executeAddQutoa() {
        getQuotaDAO().save(getQuota());
        if (getParameters().isCopyPermissions()) {
            copyQuotaPermissions();
        }
        getReturnValue().setSucceeded(true);
        return null;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionsSubject = new ArrayList<PermissionSubject>();
        permissionsSubject.add(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
        if (getParameters().isCopyPermissions()) {
            permissionsSubject.add(new PermissionSubject(getStoragePoolId(),
                    VdcObjectType.StoragePool,
                    ActionGroup.MANIPULATE_PERMISSIONS));
        }
        return permissionsSubject;
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
        quotaParameter.setId(Guid.newGuid());
        setStoragePoolId(quotaParameter.getStoragePoolId());
        if (quotaParameter.getQuotaStorages() != null) {
            for (QuotaStorage quotaStorage : quotaParameter.getQuotaStorages()) {
                quotaStorage.setQuotaId(quotaParameter.getId());
                quotaStorage.setQuotaStorageId(Guid.newGuid());
            }
        }
        if (quotaParameter.getQuotaVdsGroups() != null) {
            for (QuotaVdsGroup quotaVdsGroup : quotaParameter.getQuotaVdsGroups()) {
                quotaVdsGroup.setQuotaId(quotaParameter.getId());
                quotaVdsGroup.setQuotaVdsGroupId(Guid.newGuid());
            }
        }
        setQuota(quotaParameter);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_QUOTA : AuditLogType.USER_FAILED_ADD_QUOTA;
    }

    private void copyQuotaPermissions() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        List<Permissions> vmPermissions =
                getDbFacade().getPermissionDao().getAllForEntity(getParameters().getQuotaId(),
                        getEngineSessionSeqId(),
                        false);
        for (Permissions vmPermission : vmPermissions) {
            permissionsToAdd.addPermission(vmPermission.getad_element_id(), vmPermission.getrole_id(),
                    getQuotaId(), vmPermission.getObjectType());
        }
        if (!permissionsToAdd.isEmpty()) {
            List<Permissions> permissionsList = permissionsToAdd.asPermissionList();
            MultiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permissions[permissionsList.size()]));
        }
    }
}
