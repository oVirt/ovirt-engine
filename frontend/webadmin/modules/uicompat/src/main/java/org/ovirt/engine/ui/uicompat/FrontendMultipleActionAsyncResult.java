package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public final class FrontendMultipleActionAsyncResult
{
    private VdcActionType privateActionType;
    public VdcActionType getActionType()
    {
        return privateActionType;
    }
    private void setActionType(VdcActionType value)
    {
        privateActionType = value;
    }
    private java.util.List<VdcActionParametersBase> privateParameters;
    public java.util.List<VdcActionParametersBase> getParameters()
    {
        return privateParameters;
    }
    public void setParameters(java.util.List<VdcActionParametersBase> value)
    {
        privateParameters = value;
    }
    private java.util.List<VdcReturnValueBase> privateReturnValue;
    public java.util.List<VdcReturnValueBase> getReturnValue()
    {
        return privateReturnValue;
    }
    private void setReturnValues(java.util.List<VdcReturnValueBase> value)
    {
        privateReturnValue = value;
    }
    private Object privateState;
    public Object getState()
    {
        return privateState;
    }
    private void setState(Object value)
    {
        privateState = value;
    }

    public FrontendMultipleActionAsyncResult(VdcActionType actionType, java.util.List<VdcActionParametersBase> parameters, java.util.List<VdcReturnValueBase> returnValue)
    {
        setActionType(actionType);
        setParameters(parameters);
        setReturnValues(returnValue);
    }

    public FrontendMultipleActionAsyncResult(VdcActionType actionType, java.util.List<VdcActionParametersBase> parameters, java.util.List<VdcReturnValueBase> returnValue, Object state)
    {
        this(actionType, parameters, returnValue);
        setState(state);
    }
}
