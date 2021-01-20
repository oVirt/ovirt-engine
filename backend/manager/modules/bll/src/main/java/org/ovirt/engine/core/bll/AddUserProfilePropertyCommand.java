package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.dao.UserProfileDao;

public class AddUserProfilePropertyCommand<T extends UserProfilePropertyParameters> extends CommandBase<T> {

    @Inject
    protected UserProfileDao userProfileDao;
    private final UserProfileValidator validator = new UserProfileValidator();

    public AddUserProfilePropertyCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        UserProfileProperty newProp = getParameters().getUserProfileProperty();
        return validator.validateAdd(
                userProfileDao.getProfile(newProp.getUserId()),
                newProp,
                getCurrentUser(),
                this::validate);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_PROFILE : AuditLogType.USER_UPDATE_PROFILE_FAILED;
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
