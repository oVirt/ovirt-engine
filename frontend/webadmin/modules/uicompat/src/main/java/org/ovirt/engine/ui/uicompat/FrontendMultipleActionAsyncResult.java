package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public final class FrontendMultipleActionAsyncResult {
    private ActionType privateActionType;
    public ActionType getActionType() {
        return privateActionType;
    }
    private void setActionType(ActionType value) {
        privateActionType = value;
    }
    private java.util.List<ActionParametersBase> privateParameters;
    public java.util.List<ActionParametersBase> getParameters() {
        return privateParameters;
    }
    public void setParameters(java.util.List<ActionParametersBase> value) {
        privateParameters = value;
    }
    private java.util.List<VdcReturnValueBase> privateReturnValue;
    public java.util.List<VdcReturnValueBase> getReturnValue() {
        return privateReturnValue;
    }
    private void setReturnValues(java.util.List<VdcReturnValueBase> value) {
        privateReturnValue = value;
    }
    private Object privateState;
    public Object getState() {
        return privateState;
    }
    private void setState(Object value) {
        privateState = value;
    }

    public FrontendMultipleActionAsyncResult(ActionType actionType, java.util.List<ActionParametersBase> parameters, java.util.List<VdcReturnValueBase> returnValue) {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValues(returnValue);
    }

    public FrontendMultipleActionAsyncResult(ActionType actionType, java.util.List<ActionParametersBase> parameters, java.util.List<VdcReturnValueBase> returnValue, Object state) {
        this(actionType, parameters, returnValue);
        setState(state);
    }
}
