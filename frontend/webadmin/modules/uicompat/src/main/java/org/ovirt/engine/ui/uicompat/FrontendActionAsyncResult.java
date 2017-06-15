package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public final class FrontendActionAsyncResult {
    private ActionType privateActionType;
    public ActionType getActionType() {
        return privateActionType;
    }
    private void setActionType(ActionType value) {
        privateActionType = value;
    }
    private ActionParametersBase privateParameters;
    public ActionParametersBase getParameters() {
        return privateParameters;
    }
    public void setParameters(ActionParametersBase value) {
        privateParameters = value;
    }
    private VdcReturnValueBase privateReturnValue;
    public VdcReturnValueBase getReturnValue() {
        return privateReturnValue;
    }
    private void setReturnValue(VdcReturnValueBase value) {
        privateReturnValue = value;
    }
    private Object state;
    public Object getState() {
        return state;
    }
    private void setState(Object value) {
        state = value;
    }

    public FrontendActionAsyncResult(ActionType actionType, ActionParametersBase parameters, VdcReturnValueBase returnValue) {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValue(returnValue);
    }

    public FrontendActionAsyncResult(ActionType actionType, ActionParametersBase parameters, VdcReturnValueBase returnValue, Object state) {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValue(returnValue);
        setState(state);
    }
}
