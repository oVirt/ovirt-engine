package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DirectoryIdParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDAO;

public class AddUserCommand<T extends DirectoryIdParameters> extends CommandBase<T> {
    // We save a reference to the directory user to avoid looking it up once when checking the conditions and another
    // time when actually adding the user to the database:
    private LdapUser directoryUser;

    public AddUserCommand(T params) {
        super(params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean canDoAction() {
        // Get the name of the directory and the identifier of the user from the parameters:
        String directory = getParameters().getDirectory();
        ExternalId id = getParameters().getId();

        // Check that the user is available in the directory (and save the reference to avoid looking it up later when
        // actually adding the user to the database):
        directoryUser = (LdapUser) LdapFactory.getInstance(directory).runAdAction(
            AdActionType.GetAdUserByUserId,
            new LdapSearchByIdParameters(directory, id)
        ).getReturnValue();
        if (directoryUser == null) {
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        // Populate information for the audit log:
        addCustomValue("NewUserName", directoryUser.getUserName());

        return true;
    }

    @Override
    protected void executeCommand() {
        DbUserDAO dao = getDbUserDAO();

        // First check if the user is already in the database, if it is we need to update, if not we need to insert:
        DbUser dbUser = dao.getByExternalId(directoryUser.getDomainControler(), directoryUser.getUserId());
        if (dbUser == null) {
            dbUser = new DbUser(directoryUser);
            dbUser.setId(Guid.newGuid());
            dao.save(dbUser);
        }
        else {
            Guid id = dbUser.getId();
            dbUser = new DbUser(directoryUser);
            dbUser.setId(id);
            dao.update(dbUser);
        }

        // Return the identifier of the created user:
        setActionReturnValue(dbUser.getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
