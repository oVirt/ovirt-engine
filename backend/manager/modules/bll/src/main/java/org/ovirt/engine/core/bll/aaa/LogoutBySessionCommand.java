package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class LogoutBySessionCommand<T extends VdcActionParametersBase> extends CommandBase<T> {

    private DbUser user;

    public LogoutBySessionCommand(T parameters) {
        super(parameters);
        user = SessionDataContainer.getInstance().getUser(parameters.getSessionId(), false);
    }

    protected boolean canDoAction() {
        return user != null;
    }


    @Override
    protected void executeCommand() {
        VdcActionParametersBase params = new VdcActionParametersBase();
        params.setSessionId(getParameters().getSessionId());
        setReturnValue(Backend.getInstance().logoff(params));
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
