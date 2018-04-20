package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.dao.DbGroupDao;

public class AddGroupCommand<T extends AddGroupParameters>
    extends CommandBase<T> {

    @Inject
    private DbGroupDao dbGroupDao;

    public AddGroupCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean validate() {
        addCustomValue("NewUserName", getParameters().getGroupToAdd().getName());

        return true;
    }

    @Override
    protected void executeCommand() {
        // First check if the group is already in the database, if it is we
        // need to update, if not we need to insert:
        DbGroup groupToAdd = getParameters().getGroupToAdd();
        DbGroup dbGroup = dbGroupDao.getByExternalId(groupToAdd.getDomain(), groupToAdd.getExternalId());
        if (dbGroup == null) {
            dbGroupDao.save(groupToAdd);
        } else {
            dbGroupDao.update(dbGroup);
            groupToAdd = dbGroup;
        }

        // Return the identifier of the created group:
        setActionReturnValue(groupToAdd.getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(
            new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup())
        );
    }
}
