package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddUserCommand<T extends AddUserParameters> extends CommandBase<T> {

    public AddUserCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean validate() {
        addCustomValue("NewUserName", getParameters().getUserToAdd().getLoginName());
        return true;

    }

    @Override
    protected void executeCommand() {
        DbUser user = getParameters().getUserToAdd();
        DbUser userFromDb =
                DbFacade.getInstance().getDbUserDao().getByExternalId(user.getDomain(), user.getExternalId());
        if (userFromDb == null) {
            DbFacade.getInstance().getDbUserDao().save(user);
        } else {
            user.setId(userFromDb.getId());
            DbFacade.getInstance().getDbUserDao().update(user);
        }
        setActionReturnValue(user.getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
