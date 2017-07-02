package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;


public class EngineBackupLog implements Queryable, BusinessEntity<EngineBackupLogId> {

    private String scope;
    private Date doneAt;
    private boolean passed;
    private String outputMessage;
    private String fqdn;
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
        return Objects.hash(
                scope,
                doneAt
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
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
    public Object getQueryableId() {
        return new EngineBackupLogId(getScope(), getDoneAt());
    }

    @Override
    public EngineBackupLogId getId() {
        EngineBackupLogId key = new EngineBackupLogId(scope, doneAt);
        return key;
    }

    @Override
    public void setId(EngineBackupLogId id) {
        doneAt = id.getDoneAt();
        scope = id.getScope();
    }
}
