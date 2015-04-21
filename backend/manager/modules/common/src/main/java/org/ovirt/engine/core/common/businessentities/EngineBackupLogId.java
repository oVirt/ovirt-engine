package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

public class EngineBackupLogId implements Serializable {
    private static final long serialVersionUID = 1740373688528083410L;
    private String dbName;
    private Date doneAt;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dbName.hashCode();
        result = prime * result + doneAt.hashCode();
        return  result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EngineBackupLogId)) {
            return false;
        }
        EngineBackupLogId other = (EngineBackupLogId)obj;
        return (dbName.equals(other.getDbName()) && doneAt.equals(other.getDoneAt()));
    }


}


