package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public interface RenamedEntityInfoProvider {

    public String getEntityType();
    public String getEntityOldName();
    public String getEntityNewName();
    public void setEntityId(AuditLogableBase logable);
}
