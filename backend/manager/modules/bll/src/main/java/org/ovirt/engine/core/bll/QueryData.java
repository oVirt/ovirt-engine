package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;

/**
 * Concrete search data. Contains all search information and cache with last
 * search as well. This cache nesassary to compare with new search and send
 * notification to frontend with changes.
 */
public abstract class QueryData {
    public static QueryData CreateQueryData(Guid queryId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        RegisterableQueryReturnDataType type = RegisterableQueryTypes.GetReturnedDataTypeByQueryType(queryType,
                queryParams);

        switch (type) {
        case LIST_IQUERYABLE:
            ListIQueryableQueryData tempVar = new ListIQueryableQueryData();
            tempVar.setQueryId(queryId);
            tempVar.setQueryType(queryType);
            tempVar.setQueryParams(queryParams);
            return tempVar;

        case SEARCH:
            SearchData tempVar2 = new SearchData();
            tempVar2.setQueryId(queryId);
            tempVar2.setQueryType(queryType);
            tempVar2.setQueryParams(queryParams);
            return tempVar2;

        case IQUERYABLE:
            SingleIQueryableQueryData tempVar3 = new SingleIQueryableQueryData();
            tempVar3.setQueryId(queryId);
            tempVar3.setQueryType(queryType);
            tempVar3.setQueryParams(queryParams);
            return tempVar3;

        default:
            throw new CompatIllegalArgumentException(
                    String.format(
                            "QueryData::CreateQueryData: Failed to create query data for queryType '%1$s' - problem with its registerable query definition!",
                            queryType),
                    "queryType");
        }
    }

    private Guid privateQueryId = new Guid();

    public Guid getQueryId() {
        return privateQueryId;
    }

    public void setQueryId(Guid value) {
        privateQueryId = value;
    }

    private VdcQueryType privateQueryType = VdcQueryType.forValue(0);

    public VdcQueryType getQueryType() {
        return privateQueryType;
    }

    public void setQueryType(VdcQueryType value) {
        privateQueryType = value;
    }

    private VdcQueryParametersBase privateQueryParams;

    public VdcQueryParametersBase getQueryParams() {
        return privateQueryParams;
    }

    public void setQueryParams(VdcQueryParametersBase value) {
        privateQueryParams = value;
    }

    public boolean RefreshQuery(RefObject<IRegisterQueryUpdatedData> queryUpdatableData, RefObject<Boolean> changed) {
        QueriesCommandBase query = CommandsFactory.CreateQueryCommand(getQueryType(), getQueryParams());
        query.Execute();

        boolean validQuery = IsQueryValid(query.getQueryReturnValue());
        queryUpdatableData.argvalue = GetQueryUpdatedDataFromQueryReturnValue(query.getQueryReturnValue(), changed);

        return validQuery;
    }

    public abstract IRegisterQueryUpdatedData GetQueryUpdatedDataFromQueryReturnValue(
                                                                                      VdcQueryReturnValue QueryReturnValue,
                                                                                      RefObject<Boolean> changed);

    public boolean IsQueryValid(VdcQueryReturnValue queryRetValue) {
        return true;
    }

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
