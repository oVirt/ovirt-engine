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
        // First check if the user is already in the database, if it is we need to update, if not we need to insert:
        DbUser userToAdd = getParameters().getUserToAdd();
        SyncUsers.sync(Arrays.asList(userToAdd));
        setActionReturnValue(DbFacade.getInstance().getDbUserDao().getByExternalId(userToAdd.getDomain(), userToAdd.getExternalId()).getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
