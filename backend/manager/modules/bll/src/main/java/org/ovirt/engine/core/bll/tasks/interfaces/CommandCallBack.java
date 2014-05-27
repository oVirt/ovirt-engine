package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface CommandCallBack {
    public void executed(VdcReturnValueBase result);
}
