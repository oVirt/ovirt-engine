package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.UserProfileDao;

public class RemoveUserProfilePropertyCommand<T extends IdParameters> extends CommandBase<T> {

    @Inject
    protected UserProfileDao userProfileDao;

    private UserProfileValidator validator = new UserProfileValidator();

    public RemoveUserProfilePropertyCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_PROFILE : AuditLogType.USER_UPDATE_PROFILE_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__USER_PROFILE_PROPERTY);
    }

    @Override
    protected boolean validate() {
        UserProfileProperty currentProp = userProfileDao.get(getParameters().getId());
        return validate(validator.propertyProvided(currentProp)) &&
                validate(validator.authorized(getCurrentUser(), currentProp.getUserId()));
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
