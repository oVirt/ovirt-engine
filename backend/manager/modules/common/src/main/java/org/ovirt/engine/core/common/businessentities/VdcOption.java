package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

public class VdcOption implements Serializable {
    private static final long serialVersionUID = 5489148306184781421L;

    private int id;
    private String name;
    private String value;
    private String defaultValue;
    private String version;

    public VdcOption() {
        version = "general";
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                value
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdcOption)) {
            return false;
        }
        VdcOption other = (VdcOption) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(value, other.value)
                && Objects.equals(defaultValue, other.defaultValue);
    }

    public String getOptionName() {
        return this.name;
    }

    public void setOptionName(String value) {
        this.name = value;
    }

    public String getOptionValue() {
        return this.value;
    }

    public void setOptionValue(String value) {
        this.value = value;
    }

    public String getOptionDefaultValue() {
        return this.defaultValue;
    }

    public void setOptionDefaultValue(String value) {
        this.defaultValue = value;
    }

    public int getOptionId() {
        return this.id;
    }

    public void setOptionId(int value) {
        this.id = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }
}
