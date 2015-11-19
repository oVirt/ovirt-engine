package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

public class VdcOption implements Serializable {
    private static final long serialVersionUID = 5489148306184781421L;

    private int id;
    private String name;
    private String value;
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
                && Objects.equals(value, other.value);
    }

    public String getoption_name() {
        return this.name;
    }

    public void setoption_name(String value) {
        this.name = value;
    }

    public String getoption_value() {
        return this.value;
    }

    public void setoption_value(String value) {
        this.value = value;
    }

    public int getoption_id() {
        return this.id;
    }

    public void setoption_id(int value) {
        this.id = value;
    }

    public String getversion() {
        return version;
    }

    public void setversion(String value) {
        version = value;
    }
}
