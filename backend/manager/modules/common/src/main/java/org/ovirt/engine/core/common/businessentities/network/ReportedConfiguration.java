package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

public class ReportedConfiguration implements Serializable {

    private static final long serialVersionUID = 5723310765332966613L;
    private ReportedConfigurationType type;
    private String actualValue; //host's configuration value
    private String expectedValue; //network's data center configuration value
    private boolean inSync;

    //hide me!
    private ReportedConfiguration() {
    }

    public ReportedConfiguration(ReportedConfigurationType type, String actualValue, String expectedValue, boolean inSync) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.actualValue = actualValue;
        this.expectedValue = expectedValue;
        this.inSync = inSync;
    }


    public ReportedConfigurationType getType() {
        return type;
    }

    public void setType(ReportedConfigurationType name) {
        this.type = name;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public boolean isInSync() {
        return inSync;
    }

    public void setInSync(boolean inSync) {
        this.inSync = inSync;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String clusterValue) {
        this.expectedValue = clusterValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReportedConfiguration)) {
            return false;
        }
        ReportedConfiguration that = (ReportedConfiguration) o;
        return Objects.equals(isInSync(), that.isInSync()) &&
            Objects.equals(getType(), that.getType()) &&
            Objects.equals(getActualValue(), that.getActualValue()) &&
            Objects.equals(getExpectedValue(), that.getExpectedValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getActualValue(), getExpectedValue(), isInSync());
    }
}
