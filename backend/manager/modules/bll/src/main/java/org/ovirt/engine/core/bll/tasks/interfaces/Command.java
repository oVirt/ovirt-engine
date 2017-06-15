package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface Command<T extends ActionParametersBase> {

    VdcReturnValueBase endAction();

    T getParameters();
}
