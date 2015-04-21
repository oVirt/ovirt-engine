package org.ovirt.engine.core.common.businessentities;


import org.ovirt.engine.core.common.utils.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "engine_backup_log")
@IdClass(EngineBackupLogId.class)
@NamedQueries({
        @NamedQuery(name = "EngineBackupLog.getLatest", query = "select e from EngineBackupLog e where e.dbName = :dbName and passed = true order by doneAt DESC")
})
public class EngineBackupLog extends IVdcQueryable implements Serializable, BusinessEntity<EngineBackupLogId> {

    @Id
    @Column(name="db_name")
    private String dbName;

    @Id
    @Column(name="done_at")
    private Date doneAt;

    @Column(name="is_passed")
    private boolean passed;

    @Column(name="output_message")
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
        if (!(obj instanceof EngineBackupLog)) {
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

    @Override
    public EngineBackupLogId getId() {
        EngineBackupLogId key = new EngineBackupLogId();
        key.setDbName(dbName);
        key.setDoneAt(doneAt);
        return key;
    }

    @Override
    public void setId(EngineBackupLogId id) {
        dbName = id.getDbName();
        doneAt = id.getDoneAt();
    }
}
