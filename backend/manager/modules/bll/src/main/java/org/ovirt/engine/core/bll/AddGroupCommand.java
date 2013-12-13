package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DirectoryIdParameters;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDAO;

public class AddGroupCommand<T extends DirectoryIdParameters>
    extends CommandBase<T> {

    // We save a reference to the directory group to avoid looking it up once when checking the conditions and another
    // time when actually adding the group to the database:
    private LdapGroup directoryGroup;

    public AddGroupCommand(T params) {
        super(params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean canDoAction() {
        String directory = getParameters().getDirectory();
        Guid id = getParameters().getId();
        directoryGroup = (LdapGroup) LdapFactory.getInstance(directory).runAdAction(
            AdActionType.GetAdGroupByGroupId,
            new LdapSearchByIdParameters(directory, id)
        ).getReturnValue();
        if (directoryGroup == null) {
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        addCustomValue("NewUserName", directoryGroup.getname());

        return true;
    }

    @Override
    protected void executeCommand() {
        DbGroupDAO dao = getAdGroupDAO();
        DbGroup dbGroup = dao.get(directoryGroup.getid());
        if (dbGroup == null) {
            dbGroup = new DbGroup(directoryGroup);
            dbGroup.setId(Guid.newGuid());
            dao.save(dbGroup);
        }
        else {
            Guid id = dbGroup.getId();
            dbGroup = new DbGroup(directoryGroup);
            dbGroup.setId(id);
            dao.update(dbGroup);
        }
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
