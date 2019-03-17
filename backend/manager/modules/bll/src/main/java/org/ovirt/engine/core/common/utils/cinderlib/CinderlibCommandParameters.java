package org.ovirt.engine.core.common.utils.cinderlib;

import java.util.List;
import java.util.Objects;

public class CinderlibCommandParameters {
    private String driverInfo;
    private List<String> extraParams;
    private String correlationId;

    public CinderlibCommandParameters(String driverInfo, List<String> extraParams, String correlationId) {
        this.driverInfo = driverInfo;
        this.extraParams = extraParams;
        this.correlationId = correlationId;
    }

    public String getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(String driverInfo) {
        this.driverInfo = driverInfo;
    }

    public List<String> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(List<String> extraParams) {
        this.extraParams = extraParams;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CinderlibCommandParameters)) {
            return false;
        }
        CinderlibCommandParameters that = (CinderlibCommandParameters) o;
        return Objects.equals(driverInfo, that.driverInfo) &&
                Objects.equals(extraParams, that.extraParams) &&
                Objects.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverInfo, extraParams, correlationId);
    }
}
