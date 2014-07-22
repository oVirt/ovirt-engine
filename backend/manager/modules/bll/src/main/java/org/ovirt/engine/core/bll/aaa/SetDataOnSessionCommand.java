package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.SetDataOnSessionParameters;

public class SetDataOnSessionCommand<T extends SetDataOnSessionParameters> extends CommandBase<T> {

    public SetDataOnSessionCommand(T params) {
        super(params);
    }

    @Override
    protected void executeCommand() {
        SessionDataContainer.getInstance().setData(
                getParameters().getSessionId(),
                getParameters().getKey(),
                getParameters().getValue()
                );
        setSucceeded(true);
    }

    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    protected boolean canDoAction() {
        return SessionDataContainer.getInstance().isSessionExists(getParameters().getSessionId());
    }


}
