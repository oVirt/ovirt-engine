package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DirectoryIdParameters;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class AddGroupCommand<T extends DirectoryIdParameters>
    extends CommandBase<T> {

    // We save a reference to the directory group to avoid looking it up once when checking the conditions and another
    // time when actually adding the group to the database:
    private DirectoryGroup directoryGroup;

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
        String directoryName = getParameters().getDirectory();
        String id = getParameters().getId();
        ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(directoryName);
        if (authz == null) {
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        boolean foundGroup = false;
        for (String namespace : getParameters().getNamespace() != null ? Arrays.asList(getParameters().getNamespace())
                : authz.getContext().<List<String>> get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
            directoryGroup = DirectoryUtils.findDirectoryGroupById(authz, namespace, id, true, true);
            if (directoryGroup != null) {
                foundGroup = true;
                break;
            }
        }

        if (!foundGroup) {
            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
            return false;
        }

        addCustomValue("NewUserName", directoryGroup.getName());

        return true;
    }

    @Override
    protected void executeCommand() {
        // First check if the group is already in the database, if it is we
        // need to update, if not we need to insert:
        DbGroupDAO dao = getAdGroupDAO();
        DbGroup dbGroup = dao.getByExternalId(directoryGroup.getDirectoryName(), directoryGroup.getId());
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

        // Return the identifier of the created group:
        setActionReturnValue(dbGroup.getId());
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
