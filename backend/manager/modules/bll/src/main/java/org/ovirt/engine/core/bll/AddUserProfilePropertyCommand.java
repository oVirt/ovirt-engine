package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UserProfilePropertyParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.UserProfileDao;

public class AddUserProfilePropertyCommand<T extends UserProfilePropertyParameters> extends CommandBase<T> {

    public static final String PROFILE_USER = "profile_user";
    public static final String PROFILE_PROPERTY = "profile_property";

    @Inject
    protected UserProfileDao userProfileDao;

    @Inject
    protected DbUserDao userDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public AddUserProfilePropertyCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        UserProfileProperty newProp = getParameters().getUserProfileProperty();
        if (!newProp.getUserId().equals(getUserId())) {
            addCustomValue(PROFILE_USER, buildUserName(userDao, newProp.getUserId()));
        }
        addCustomValue(PROFILE_PROPERTY, newProp.getName());
        return validator.validateAdd(
                userProfileDao.getProfile(newProp.getUserId()),
                newProp,
                getCurrentUser(),
                this::validate,
                isSystemSuperUser());
    }

    public static String buildUserName(DbUserDao userDao, Guid userId) {
        return Optional.ofNullable(userDao.get(userId))
                .map(dbUser -> String.format("%s@%s", dbUser.getLoginName(), dbUser.getDomain()))
                .orElse(userId.toString());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getCustomValues().containsKey(PROFILE_USER)) {
            return getSucceeded() ?
                    AuditLogType.USER_PROFILE_CREATE_PROPERTY :
                    AuditLogType.USER_PROFILE_CREATE_PROPERTY_FAILED;
        } else {
            return getSucceeded() ?
                    AuditLogType.USER_PROFILE_CREATE_OWN_PROPERTY :
                    AuditLogType.USER_PROFILE_CREATE_OWN_PROPERTY_FAILED;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE_PROPERTY);
    }

    @Override
    protected void executeCommand() {
        setActionReturnValue(userProfileDao.save(getParameters().getUserProfileProperty()));
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getUserId(),
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
