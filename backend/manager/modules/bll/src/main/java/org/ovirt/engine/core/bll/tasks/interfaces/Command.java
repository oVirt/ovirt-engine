package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface Command<T extends VdcActionParametersBase> {

    VdcReturnValueBase endAction();

    T getParameters();
}
