package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;

public interface Command<T extends ActionParametersBase> {

    ActionReturnValue endAction();

    T getParameters();
}
