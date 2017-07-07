package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;

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
    private ActionReturnValue privateReturnValue;
    public ActionReturnValue getReturnValue() {
        return privateReturnValue;
    }
    private void setReturnValue(ActionReturnValue value) {
        privateReturnValue = value;
    }
    private Object state;
    public Object getState() {
        return state;
    }
    private void setState(Object value) {
        state = value;
    }

    public FrontendActionAsyncResult(ActionType actionType, ActionParametersBase parameters, ActionReturnValue returnValue) {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValue(returnValue);
    }

    public FrontendActionAsyncResult(ActionType actionType, ActionParametersBase parameters, ActionReturnValue returnValue, Object state) {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValue(returnValue);
        setState(state);
    }
}
