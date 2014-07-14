package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.dao.DbGroupDAO;

public class AddGroupCommand<T extends AddGroupParameters>
    extends CommandBase<T> {

    public AddGroupCommand(T params) {
        this(params, null);
    }

    public AddGroupCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean canDoAction() {
        addCustomValue("NewUserName", getParameters().getGroupToAdd().getName());

        return true;
    }

    @Override
    protected void executeCommand() {
        // First check if the group is already in the database, if it is we
        // need to update, if not we need to insert:
        DbGroupDAO dao = getAdGroupDAO();
        DbGroup groupToAdd = getParameters().getGroupToAdd();
        DbGroup dbGroup = dao.getByExternalId(groupToAdd.getDomain(), groupToAdd.getExternalId());
        if (dbGroup == null) {
            dao.save(groupToAdd);
        }
        else {
            dao.update(dbGroup);
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
