package org.ovirt.engine.core.common;

import java.util.Date;
import java.util.Objects;

/**
 * Holder to exchange data with external systems using db
 */
public class ExternalVariable {
    /**
     * Name of the variable
     */
    private String name;

    /**
     * Value of the variable
     */
    private String value;

    /**
     * Last record update, it's set by database and cannot be changed manually.
     */
    private Date updateDate;

    public ExternalVariable() {
        name = null;
        value = null;
        updateDate = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExternalVariable)) {
            return false;
        }

        ExternalVariable other = (ExternalVariable) obj;
        return Objects.equals(name, other.getName())
                && Objects.equals(value, other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                value
        );
    }
}
