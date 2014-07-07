package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DirectoryIdParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class AddUserCommand<T extends DirectoryIdParameters> extends CommandBase<T> {
    // We save a reference to the directory user to avoid looking it up once when checking the conditions and another
    // time when actually adding the user to the database:
    private DirectoryUser directoryUser;

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
        // Check that the directory name has been provided:
        String directoryName = getParameters().getDirectory();
        if (directoryName == null) {
            log.error(
                "Can't add user because directory name hasn't been provided."
            );
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        // Check that the identifier of the directory user has been provided:
        String id = getParameters().getId();
        if (id == null) {
            log.errorFormat(
                "Can't add user from directory \"{0}\" because the user identifier hasn't been provided.",
                directoryName
            );
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        // Check that the directory exists:
        ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(directoryName);
        if (authz == null) {
            log.errorFormat(
                "Can't add user with id \"{0}\" because directory \"{1}\" doesn't exist.",
                id, directoryName
            );
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        // Check that the user is available in the directory (and save the reference to avoid looking it up later when
        // actually adding the user to the database):
        boolean foundUser = false;
        for (String namespace : getParameters().getNamespace() != null ? Arrays.asList(getParameters().getNamespace())
                : authz.getContext().<List<String>> get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
            directoryUser = DirectoryUtils.findDirectoryUserById(authz, namespace, id, true, true);
            if (directoryUser != null) {
                foundUser = true;
                break;
            }
        }
        if (!foundUser) {
            log.errorFormat(
                "Can't add user with id \"{0}\" because it doesn't exist in directory \"{1}\".",
                id, directoryName
            );
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }


        // Populate information for the audit log:
        addCustomValue("NewUserName", directoryUser.getName());

        return true;

    }

    @Override
    protected void executeCommand() {
        DbUserDAO dao = getDbUserDAO();

        // First check if the user is already in the database, if it is we need to update, if not we need to insert:
        HashSet<Guid> groupIds = DirectoryUtils.getGroupIdsFromUser(directoryUser);
        DbUser dbUser = dao.getByExternalId(directoryUser.getDirectoryName(), directoryUser.getId());
        if (dbUser == null) {
            dbUser = new DbUser(directoryUser);
            dbUser.setId(Guid.newGuid());
            dbUser.setGroupIds(groupIds);
            dao.save(dbUser);
        }
        else {
            Guid id = dbUser.getId();
            dbUser = new DbUser(directoryUser);
            dbUser.setId(id);
            dbUser.setGroupIds(groupIds);
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
