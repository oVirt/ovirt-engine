package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddRoleWithActionGroupsCommand<T extends RoleWithActionGroupsParameters> extends
        RolesOperationCommandBase<T> {

    @Inject
    private RoleDao roleDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddRoleWithActionGroupsCommand(Guid commandId) {
        super(commandId);
    }

    public AddRoleWithActionGroupsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected boolean validate() {
        if (getParameters().getActionGroups().isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_LIST_CANNOT_BE_EMPTY);
            return false;
        }
        if (roleDao.getByName(getRoleName()) != null) {
            addValidationMessage(EngineMessage.VAR__ACTION__ADD);
            addValidationMessage(EngineMessage.VAR__TYPE__ROLE);
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }
        RoleType roleType = getRole().getType();
        if (roleType == null) {
            addValidationMessage(EngineMessage.ROLE_TYPE_CANNOT_BE_EMPTY);
            return false;
        }
        if (roleType != RoleType.ADMIN) {
            List<ActionGroup> actionGroups = getParameters().getActionGroups();
            for (ActionGroup group : actionGroups) {
                if (group.getRoleType() == RoleType.ADMIN) {
                    addValidationMessage(EngineMessage.CANNOT_ADD_ACTION_GROUPS_TO_ROLE_TYPE);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        prepareRoleForCommand();
        TransactionSupport.executeInNewTransaction(() -> {
            roleDao.save(getRole());
            getCompensationContext().snapshotNewEntity(getRole());
            getCompensationContext().stateChanged();
            return null;
        });

        ActionReturnValue attachAction = runInternalAction(
                ActionType.AttachActionGroupsToRole,
                new ActionGroupsToRoleParameter(getRole().getId(), getParameters().getActionGroups()));
        if (!attachAction.isValid() || !attachAction.getSucceeded()) {
            List<String> failedMsgs = getReturnValue().getExecuteFailedMessages();
            for (String msg : attachAction.getValidationMessages()) {
                failedMsgs.add(msg);
            }
            setSucceeded(false);
            return;
        }
        setSucceeded(true);
        getReturnValue().setActionReturnValue(getRole().getId());
    }

    /**
     *
     */
    protected void prepareRoleForCommand() {
        // Note that the role is take from the parameters
        Role role = getRole();
        role.setId(Guid.newGuid());
        role.setAllowsViewingChildren(false);
        // Set the application mode as 255 - AllModes by default
        getRole().setAppMode(ApplicationMode.AllModes);

        for (ActionGroup group : getParameters().getActionGroups()) {
            if (group.allowsViewingChildren()) {
                role.setAllowsViewingChildren(true);
                break;
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_ROLE_WITH_ACTION_GROUP
                : AuditLogType.USER_ADD_ROLE_WITH_ACTION_GROUP_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
