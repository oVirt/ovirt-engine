package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents variable in {@code dwh_history_timekeeping} table
 */
public class DwhHistoryTimekeeping implements Serializable {
    private static final long serialVersionUID = -6971859290406886614L;

    /**
     * Variable
     */
    private DwhHistoryTimekeepingVariable variable;

    /**
     * Variable value
     */
    private String value;

    /**
     * Variable timestamp
     */
    private Date dateTime;

    public DwhHistoryTimekeeping() {
        variable = DwhHistoryTimekeepingVariable.UNDEFINED;
        value = null;
        dateTime = null;
    }

    public DwhHistoryTimekeepingVariable getVariable() {
        return variable;
    }

    public void setVariable(DwhHistoryTimekeepingVariable variable) {
        if (variable == null) {
            this.variable = DwhHistoryTimekeepingVariable.UNDEFINED;
        } else {
            this.variable = variable;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date datetTime) {
        this.dateTime = datetTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DwhHistoryTimekeeping)) {
            return false;
        }
        return variable == ((DwhHistoryTimekeeping)obj).getVariable();
    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }
}
