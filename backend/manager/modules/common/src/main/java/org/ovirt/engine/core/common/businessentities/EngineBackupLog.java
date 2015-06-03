package org.ovirt.engine.core.common.businessentities;


import org.ovirt.engine.core.common.utils.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "engine_backup_log")
@IdClass(EngineBackupLogId.class)
@NamedQueries({
        @NamedQuery(name = "EngineBackupLog.getLatest", query = "select e from EngineBackupLog e where e.scope = :scope and passed = true order by doneAt DESC")
})
public class EngineBackupLog implements IVdcQueryable, BusinessEntity<EngineBackupLogId> {

    @Id
    @Column(name="scope")
    private String scope;

    @Id
    @Column(name="done_at")
    private Date doneAt;

    @Column(name="is_passed")
    private boolean passed;

    @Column(name="output_message")
    private String outputMessage;

    @Column(name="fqdn")
    private String fqdn;

    @Column(name = "log_path")
    private String logPath;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    @Override
    public int hashCode() {
        return  Objects.hash(scope, doneAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EngineBackupLog)) {
            return false;
        }
        EngineBackupLog other = (EngineBackupLog) obj;
        return Objects.equals(scope, other.scope)
               && Objects.equals(doneAt, other.doneAt);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("scope", scope)
                .append("doneAt", doneAt)
                .append("passed", passed)
                .append("outputMessage", outputMessage)
                .append("fqdn", fqdn)
                .append("logPath", logPath)
                .build();
    }

    @Override
    public EngineBackupLogId getId() {
        EngineBackupLogId key = new EngineBackupLogId();
        key.setScope(scope);
        key.setDoneAt(doneAt);
        return key;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public void setId(EngineBackupLogId id) {
        scope = id.getScope();
        doneAt = id.getDoneAt();
    }
}
