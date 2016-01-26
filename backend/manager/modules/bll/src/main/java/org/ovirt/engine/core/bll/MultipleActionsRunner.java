package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface MultipleActionsRunner {
    ArrayList<VdcReturnValueBase> execute();

    void setIsRunOnlyIfAllValidatePass(boolean isRunOnlyIfAllValidationPass);

    void setIsWaitForResult(boolean waitForResult);
}
