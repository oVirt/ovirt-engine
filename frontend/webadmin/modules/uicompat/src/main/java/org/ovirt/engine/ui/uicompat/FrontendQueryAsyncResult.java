package org.ovirt.engine.ui.uicompat;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public final class FrontendQueryAsyncResult
{
    private VdcQueryType privateQueryType;
    public VdcQueryType getQueryType()
    {
        return privateQueryType;
    }
    private void setQueryType(VdcQueryType value)
    {
        privateQueryType = value;
    }
    private VdcQueryParametersBase privateParameters;
    public VdcQueryParametersBase getParameters()
    {
        return privateParameters;
    }
    public void setParameters(VdcQueryParametersBase value)
    {
        privateParameters = value;
    }
    private VdcQueryReturnValue privateReturnValue;
    public VdcQueryReturnValue getReturnValue()
    {
        return privateReturnValue;
    }
    private void setReturnValue(VdcQueryReturnValue value)
    {
        privateReturnValue = value;
    }

    public FrontendQueryAsyncResult(VdcQueryType actionType, VdcQueryParametersBase parameters, VdcQueryReturnValue returnValue)
    {
        setQueryType(actionType);
        setParameters(parameters);
        setReturnValue(returnValue);
    }
}