package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.PROFILE_PROPERTY;
import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.PROFILE_USER;
import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.buildUserName;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.UserProfileDao;

public class UpdateUserProfilePropertyCommand<T extends UserProfilePropertyParameters> extends CommandBase<T> {

    @Inject
    protected UserProfileDao userProfileDao;

    @Inject
    protected DbUserDao userDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public UpdateUserProfilePropertyCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getCustomValues().containsKey(PROFILE_USER)) {
            return getSucceeded() ?
                    AuditLogType.USER_PROFILE_REPLACE_PROPERTY :
                    AuditLogType.USER_PROFILE_REPLACE_PROPERTY_FAILED;
        } else {
            return getSucceeded() ?
                    AuditLogType.USER_PROFILE_REPLACE_OWN_PROPERTY :
                    AuditLogType.USER_PROFILE_REPLACE_OWN_PROPERTY_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE_PROPERTY);
    }

    @Override
    protected boolean validate() {
        UserProfileProperty propertyToUpdate = getParameters().getUserProfileProperty();
        if (!propertyToUpdate.getUserId().equals(getUserId())) {
            addCustomValue(PROFILE_USER, buildUserName(userDao, propertyToUpdate.getUserId()));
        }
        addCustomValue(PROFILE_PROPERTY, propertyToUpdate.getName());
        return validator.validateUpdate(
                userProfileDao.get(propertyToUpdate.getPropertyId()),
                getCurrentUser(),
                this::validate,
                propertyToUpdate,
                isSystemSuperUser()
        );
    }

    @Override
    protected void executeCommand() {
        setActionReturnValue(userProfileDao.update(getParameters().getUserProfileProperty()));
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getUserId(),
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
