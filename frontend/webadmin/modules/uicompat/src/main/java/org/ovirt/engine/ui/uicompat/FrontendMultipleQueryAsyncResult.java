package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public final class FrontendMultipleQueryAsyncResult {
    public FrontendMultipleQueryAsyncResult(List<VdcQueryType> queryTypes,
            List<VdcQueryParametersBase> parameters,
            List<VdcQueryReturnValue> returnValues) {
        setQueryTypes(queryTypes);
        setParameters(parameters);
        setReturnValues(returnValues);
    }

    public FrontendMultipleQueryAsyncResult() {
        setReturnValues(new ArrayList<VdcQueryReturnValue>());
    }

    private List<VdcQueryType> privateQueryTypes;

    public List<VdcQueryType> getQueryTypes() {
        return privateQueryTypes;
    }

    private void setQueryTypes(List<VdcQueryType> value) {
        privateQueryTypes = value;
    }

    private List<VdcQueryParametersBase> privateParameters;

    public List<VdcQueryParametersBase> getParameters() {
        return privateParameters;
    }

    public void setParameters(List<VdcQueryParametersBase> value) {
        privateParameters = value;
    }

    private List<VdcQueryReturnValue> privateReturnValues;

    public List<VdcQueryReturnValue> getReturnValues() {
        return privateReturnValues;
    }

    private void setReturnValues(List<VdcQueryReturnValue> value) {
        privateReturnValues = value;
    }
}
