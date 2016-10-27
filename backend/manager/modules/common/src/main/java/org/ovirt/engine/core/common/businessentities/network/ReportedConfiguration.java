package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

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

    public ReportedConfiguration(ReportedConfigurationType type, Object actualValue, Object expectedValue, boolean inSync) {
        this(type, Objects.toString(actualValue, null), Objects.toString(expectedValue, null), inSync);

    }

    public ReportedConfiguration(ReportedConfigurationType type) {
        this(type, null, null, true);
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
        ReportedConfiguration other = (ReportedConfiguration) o;
        return inSync == other.inSync
                && type == other.type
                && Objects.equals(actualValue, other.actualValue)
                && Objects.equals(expectedValue, other.expectedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                actualValue,
                expectedValue,
                inSync
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("actualValue", actualValue)
                .append("expectedValue", expectedValue)
                .append("inSync", inSync)
                .append("type", type)
                .build();
    }
}
