package org.ovirt.engine.core.common.queries;

/** A base class for parameters of queries that retrieve ISO lists */
public abstract class GetAllImagesListParametersBase extends VdcQueryParametersBase {

    private static final long serialVersionUID = 2562476365144558247L;
    private boolean forceRefresh;

    public GetAllImagesListParametersBase() {
    }

    public boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }
}
