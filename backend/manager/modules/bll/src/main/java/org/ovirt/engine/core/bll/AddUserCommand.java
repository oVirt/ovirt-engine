package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddUserCommand<T extends AddUserParameters> extends CommandBase<T> {

    public AddUserCommand(T params) {
        this(params, null);
    }

    public AddUserCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean canDoAction() {
        addCustomValue("NewUserName", getParameters().getUserToAdd().getLoginName());
        return true;

    }

    @Override
    protected void executeCommand() {
        DbUser userToAdd = getParameters().getUserToAdd();
        for (DbUser syncedUser : SyncUsers.sync(Arrays.asList(userToAdd))) {
            if (Guid.isNullOrEmpty(syncedUser.getId())) {
                if (syncedUser.isActive()) {
                    DbFacade.getInstance().getDbUserDao().save(syncedUser);
                }
            } else {
                DbFacade.getInstance().getDbUserDao().update(syncedUser);
            }
        }
        DbUser userFromDb =
                DbFacade.getInstance().getDbUserDao().getByExternalId(userToAdd.getDomain(), userToAdd.getExternalId());
        setActionReturnValue(userFromDb.getId());
        setSucceeded(userFromDb.isActive());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
