package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class EngineBackupLogId implements Serializable {
    private static final long serialVersionUID = 1740373688528083410L;
    private String scope;
    private Date doneAt;

    public EngineBackupLogId() {
    }

    public EngineBackupLogId(String scope, Date doneAt) {
        this.scope = scope;
        this.doneAt = doneAt;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String dbName) {
        this.scope = dbName;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(Date doneAt) {
        this.doneAt = doneAt;
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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EngineBackupLogId)) {
            return false;
        }
        EngineBackupLogId other = (EngineBackupLogId) obj;
        return Objects.equals(scope, other.scope)
                && Objects.equals(doneAt, other.doneAt);
    }


}


