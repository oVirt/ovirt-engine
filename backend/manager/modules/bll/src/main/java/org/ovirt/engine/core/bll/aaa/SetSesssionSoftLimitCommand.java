package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.SetSesssionSoftLimitCommandParameters;

public class SetSesssionSoftLimitCommand<T extends SetSesssionSoftLimitCommandParameters> extends CommandBase<T> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public SetSesssionSoftLimitCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (sessionDataContainer.isSessionExists(getParameters().getSessionId())) {
            sessionDataContainer.setSoftLimitInterval(getParameters().getSessionId(),
                    getParameters().getSoftLimit());
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

}
