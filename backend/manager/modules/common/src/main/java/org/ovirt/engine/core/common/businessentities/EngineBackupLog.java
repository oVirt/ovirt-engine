package org.ovirt.engine.core.common.businessentities;


import org.ovirt.engine.core.common.utils.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class EngineBackupLog extends IVdcQueryable implements Serializable {

    private String dbName;
    private Date doneAt;
    private boolean passed;
    private String outputMessage;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(Date doneAt) {
        this.doneAt = doneAt;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(String outputMessage) {
        this.outputMessage = outputMessage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
        result = prime * result + ((doneAt == null) ? 0 : doneAt.hashCode());
        result = prime * result + (passed ? 0 : 1);
        result = prime * result + ((outputMessage == null) ? 0 : outputMessage.hashCode());
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
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        EngineBackupLog other = (EngineBackupLog) obj;
        return (Objects.equals(dbName, other.dbName)
                && Objects.equals(doneAt, other.doneAt)
                && this.passed == other.passed
                && Objects.equals(outputMessage, other.outputMessage));
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("dbName", dbName)
                .append("doneAt", doneAt)
                .append("passed", passed)
                .append("outputMessage", outputMessage)
                .build();
    }
}
