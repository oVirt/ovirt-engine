package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

public class ReportedConfiguration implements Serializable {

    private static final long serialVersionUID = 5723310765332966613L;
    private ReportedConfigurationType type;
    private String value;
    private boolean inSync;

    //hide me!
    private ReportedConfiguration() {
    }

    public ReportedConfiguration(ReportedConfigurationType type, String value, boolean inSync) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        this.type = type;
        this.value = value;
        this.inSync = inSync;
    }


    public ReportedConfigurationType getType() {
        return type;
    }

    public void setType(ReportedConfigurationType name) {
        this.type = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isInSync() {
        return inSync;
    }

    public void setInSync(boolean inSync) {
        this.inSync = inSync;
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
            Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue(), isInSync());
    }
}
