package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmToAdElementParameters;

public abstract class VmToUserCommand<T extends VmToAdElementParameters> extends UserCommandBase<T> {
    public VmToUserCommand(T parameters) {
        super(parameters);
    }

}
