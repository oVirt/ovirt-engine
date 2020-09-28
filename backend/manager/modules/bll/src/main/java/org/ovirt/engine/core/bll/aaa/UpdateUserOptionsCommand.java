package org.ovirt.engine.core.bll.aaa;

import static org.ovirt.engine.core.common.AuditLogType.USER_OPTIONS_FAILED_UPDATE;
import static org.ovirt.engine.core.common.AuditLogType.USER_OPTIONS_UPDATE;
import static org.ovirt.engine.core.common.errors.EngineMessage.ACTION_TYPE_FAILED_USER_NOT_EXISTS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.dao.DbUserDao;

public class UpdateUserOptionsCommand<T extends UpdateUserParameters> extends CommandBase<T> {

    private static final String ID_OF_USER_TO_UPDATE = "IdOfUserToUpdate";
    private static final String NAME_OF_USER_TO_UPDATE = "NameOfUserToUpdate";

    @Inject
    private DbUserDao dbUserDao;

    public UpdateUserOptionsCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue(ID_OF_USER_TO_UPDATE, String.valueOf(getParameters().getUserToUpdate().getId()));
        addCustomValue(NAME_OF_USER_TO_UPDATE, String.valueOf(getParameters().getUserToUpdate().getName()));
        return getSucceeded() ? USER_OPTIONS_UPDATE : USER_OPTIONS_FAILED_UPDATE;
    }

    @Override
    protected void executeCommand() {
        DbUser userToUpdate = getParameters().getUserToUpdate();
        boolean mergeOptions = getParameters().isMergeOptions();
        DbUser current = dbUserDao.getByExternalId(userToUpdate.getDomain(), userToUpdate.getExternalId());
        if (current == null) {
            addValidationMessage(ACTION_TYPE_FAILED_USER_NOT_EXISTS);
            setSucceeded(false);
            return;
        }

        // if user was modified in the meantime then take all immutable fields from the newest version
        if (!mergeOptions) {
            // pick the value of mutable field (userOptions) from parameters and overwrite existing value
            current.setUserOptions(userToUpdate.getUserOptions());
        } else {
            // merge strategy: override existing keys only if they are present in the parameters
            // this allows easy interoperability between different clients (i.e. Web Admin and VM Portal)
            // each client can maintain its own slice/namespace in JSON blob
            Map<String, String> currentOptions = new HashMap<>(current.getUserOptions());
            currentOptions.putAll(userToUpdate.getUserOptions());
            current.setUserOptions(currentOptions);
        }
        dbUserDao.update(current);
        setActionReturnValue(current.getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

}
