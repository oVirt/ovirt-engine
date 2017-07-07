package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.tasks.interfaces.Command;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;

public class DecoratedCommand<T extends ActionParametersBase> implements Command<T> {

    private Command<T> innerCommand;

    public DecoratedCommand(Command<T> innerCommand) {
        this.innerCommand = innerCommand;
    }

    @Override
    public ActionReturnValue endAction() {
        return innerCommand.endAction();
    }

    @Override
    public T getParameters() {
        return innerCommand.getParameters();
    }
}
