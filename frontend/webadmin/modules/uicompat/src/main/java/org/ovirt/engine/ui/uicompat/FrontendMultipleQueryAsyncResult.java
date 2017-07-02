package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public final class FrontendMultipleQueryAsyncResult {
    public FrontendMultipleQueryAsyncResult(List<QueryType> queryTypes,
            List<VdcQueryParametersBase> parameters,
            List<VdcQueryReturnValue> returnValues) {
        setQueryTypes(queryTypes);
        setParameters(parameters);
        setReturnValues(returnValues);
    }

    public FrontendMultipleQueryAsyncResult() {
        setReturnValues(new ArrayList<VdcQueryReturnValue>());
    }

    private List<QueryType> privateQueryTypes;

    public List<QueryType> getQueryTypes() {
        return privateQueryTypes;
    }

    private void setQueryTypes(List<QueryType> value) {
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
