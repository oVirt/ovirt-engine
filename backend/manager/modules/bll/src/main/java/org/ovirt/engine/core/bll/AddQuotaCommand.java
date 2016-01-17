package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddQuotaCommand extends QuotaCRUDCommand {

    public AddQuotaCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getQuota() != null ? getParameters().getQuota().getStoragePoolId() : null);
    }

    @Override
    protected boolean validate() {
        return checkQuotaValidationForAdd(getParameters().getQuota(),
                getReturnValue().getValidationMessages());
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
            TransactionSupport.executeInNewTransaction(() -> executeAddQutoa());
        } else {
            executeAddQutoa();
        }
    }

    private Void executeAddQutoa() {
        getQuotaDao().save(getQuota());
        if (getParameters().isCopyPermissions()) {
            copyQuotaPermissions();
        }
        getReturnValue().setSucceeded(true);
        setActionReturnValue(getQuota().getId());
        return null;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionsSubject = new ArrayList<>();
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
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__QUOTA);
    }

    /**
     * Set quota from the parameter
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
        if (quotaParameter.getQuotaClusters() != null) {
            for (QuotaCluster quotaCluster : quotaParameter.getQuotaClusters()) {
                quotaCluster.setQuotaId(quotaParameter.getId());
                quotaCluster.setQuotaClusterId(Guid.newGuid());
            }
        }
        setQuotaThresholdDefaults(quotaParameter);
        setQuota(quotaParameter);
    }

    // Setting defaults for hard and soft limits, for REST
    private void setQuotaThresholdDefaults(Quota quotaParameter) {
        if (quotaParameter.getGraceStoragePercentage() == 0) {
            quotaParameter.setGraceStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaGraceStorage));
        }
        if (quotaParameter.getGraceClusterPercentage() == 0) {
            quotaParameter.setGraceClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaGraceCluster));
        }
        if (quotaParameter.getThresholdStoragePercentage() == 0) {
            quotaParameter.setThresholdStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaThresholdStorage));
        }
        if (quotaParameter.getThresholdClusterPercentage() == 0) {
            quotaParameter.setThresholdClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaThresholdCluster));
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_QUOTA : AuditLogType.USER_FAILED_ADD_QUOTA;
    }

    private void copyQuotaPermissions() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        List<Permission> vmPermissions =
                getDbFacade().getPermissionDao().getAllForEntity(getParameters().getQuotaId(),
                        getEngineSessionSeqId(),
                        false);
        for (Permission vmPermission : vmPermissions) {
            permissionsToAdd.addPermission(vmPermission.getAdElementId(), vmPermission.getRoleId(),
                    getQuotaId(), vmPermission.getObjectType());
        }
        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            MultiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }
}
