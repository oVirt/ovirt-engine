package org.ovirt.engine.core.common.utils.cinderlib;

import java.util.List;
import java.util.Objects;

public class CinderlibCommandParameters {
    private String driverInfo;
    private List<String> extraParams;

    public CinderlibCommandParameters(String driverInfo, List<String> extraParams) {
        this.driverInfo = driverInfo;
        this.extraParams = extraParams;
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
                Objects.equals(extraParams, that.extraParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverInfo, extraParams);
    }
}
