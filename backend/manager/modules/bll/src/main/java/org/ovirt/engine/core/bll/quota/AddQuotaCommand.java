package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.UniquePermissionsSet;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddQuotaCommand extends QuotaCRUDCommand {

    @Inject
    private PermissionDao permissionDao;
    @Inject
    private QuotaDao quotaDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    public AddQuotaCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        getParameters().getQuota().setId(Guid.newGuid());
        super.init();
    }

    @Override
    protected void executeCommand() {
        setQuota(getParameters().getQuota());
        if (getParameters().isCopyPermissions()) {
            TransactionSupport.executeInNewTransaction(() -> executeAddQuota());
        } else {
            executeAddQuota();
        }
    }

    private Void executeAddQuota() {
        quotaDao.save(getQuota());
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

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_QUOTA : AuditLogType.USER_FAILED_ADD_QUOTA;
    }

    private void copyQuotaPermissions() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        List<Permission> vmPermissions =
                permissionDao.getAllForEntity(getParameters().getQuotaId(), getEngineSessionSeqId(), false);
        for (Permission vmPermission : vmPermissions) {
            permissionsToAdd.addPermission(vmPermission.getAdElementId(), vmPermission.getRoleId(),
                    getQuotaId(), vmPermission.getObjectType());
        }
        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            multiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }
}
