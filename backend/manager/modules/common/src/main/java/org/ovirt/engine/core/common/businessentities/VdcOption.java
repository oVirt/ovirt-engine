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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VdcOption other = (VdcOption) obj;
        return (Objects.equals(name, other.name)
                && Objects.equals(value, other.value));
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
