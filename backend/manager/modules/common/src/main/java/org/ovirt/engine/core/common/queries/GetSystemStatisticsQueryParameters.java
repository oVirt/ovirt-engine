package org.ovirt.engine.core.common.queries;

public class GetSystemStatisticsQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = -1449030042803469926L;
    private int privateMax;

    public int getMax() {
        return privateMax;
    }

    protected void setMax(int value) {
        privateMax = value;
    }

    public GetSystemStatisticsQueryParameters(int max) {
        this();
        setMax(max);
    }

    public GetSystemStatisticsQueryParameters() {
        setRefresh(false);
    }
}
