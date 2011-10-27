package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;

public abstract class ConfigCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {
    protected ConfigCommandBase(T parameters) {
        super(parameters);
    }
}
