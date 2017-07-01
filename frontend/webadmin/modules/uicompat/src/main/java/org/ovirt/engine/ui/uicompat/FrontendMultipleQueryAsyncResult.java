package org.ovirt.engine.ui.uicompat;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public final class FrontendMultipleQueryAsyncResult {
    public FrontendMultipleQueryAsyncResult(List<QueryType> queryTypes,
            List<QueryParametersBase> parameters,
            List<QueryReturnValue> returnValues) {
        setQueryTypes(queryTypes);
        setParameters(parameters);
        setReturnValues(returnValues);
    }

    public FrontendMultipleQueryAsyncResult() {
        setReturnValues(new ArrayList<QueryReturnValue>());
    }

    private List<QueryType> privateQueryTypes;

    public List<QueryType> getQueryTypes() {
        return privateQueryTypes;
    }

    private void setQueryTypes(List<QueryType> value) {
        privateQueryTypes = value;
    }

    private List<QueryParametersBase> privateParameters;

    public List<QueryParametersBase> getParameters() {
        return privateParameters;
    }

    public void setParameters(List<QueryParametersBase> value) {
        privateParameters = value;
    }

    private List<QueryReturnValue> privateReturnValues;

    public List<QueryReturnValue> getReturnValues() {
        return privateReturnValues;
    }

    private void setReturnValues(List<QueryReturnValue> value) {
        privateReturnValues = value;
    }
}
