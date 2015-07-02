package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.SetSesssionSoftLimitCommandParameters;

public class SetSesssionSoftLimitCommand<T extends SetSesssionSoftLimitCommandParameters> extends CommandBase<T> {

    public SetSesssionSoftLimitCommand(T params) {
        super(params);
    }

    @Override
    protected void executeCommand() {
        if (SessionDataContainer.getInstance().isSessionExists(getParameters().getSessionId())) {
            SessionDataContainer.getInstance().setSoftLimitInterval(getParameters().getSessionId(),
                    getParameters().getSoftLimit());
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }

    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

}
