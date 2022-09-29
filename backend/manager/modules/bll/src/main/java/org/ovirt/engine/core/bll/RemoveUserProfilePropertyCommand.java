package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.PROFILE_PROPERTY;
import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.PROFILE_USER;
import static org.ovirt.engine.core.bll.AddUserProfilePropertyCommand.buildUserName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.UserProfileDao;

public class RemoveUserProfilePropertyCommand<T extends IdParameters> extends CommandBase<T> {

    @Inject
    protected UserProfileDao userProfileDao;

    @Inject
    protected DbUserDao userDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public RemoveUserProfilePropertyCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getCustomValues().containsKey(PROFILE_USER)) {
            return getSucceeded() ? AuditLogType.USER_PROFILE_REMOVE_PROPERTY
                    : AuditLogType.USER_PROFILE_REMOVE_PROPERTY_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_PROFILE_REMOVE_OWN_PROPERTY
                    : AuditLogType.USER_PROFILE_REMOVE_OWN_PROPERTY_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE_PROPERTY);
    }

    @Override
    protected boolean validate() {
        UserProfileProperty currentProp = userProfileDao.get(getParameters().getId());
        if (currentProp != null && !currentProp.getUserId().equals(getUserId())) {
            addCustomValue(PROFILE_USER, buildUserName(userDao, currentProp.getUserId()));
        }
        addCustomValue(PROFILE_PROPERTY,
                Optional.ofNullable(currentProp)
                        .map(UserProfileProperty::getName)
                        .orElse(getParameters().getId().toString()));
        return validate(validator.propertyProvided(currentProp)) &&
                validate(validator.authorized(
                        getCurrentUser(),
                        Optional.ofNullable(currentProp).map(UserProfileProperty::getUserId).orElse(null),
                        isSystemSuperUser()));
    }

    @Override
    protected void executeCommand() {
        userProfileDao.remove(getParameters().getId());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getUserId(),
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
