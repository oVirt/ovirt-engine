package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.MacPoolDao;

public abstract class MacPoolCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {
    public MacPoolCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__TYPE__MAC__POOL);
    }

    public MacPoolDao getMacPoolDao() {
        return getDbFacade().getMacPoolDao();
    }
}
