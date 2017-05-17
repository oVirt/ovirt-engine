package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;

public interface RenamedEntityInfoProvider {

    public String getEntityType();
    public String getEntityOldName();
    public String getEntityNewName();
    public void setEntityId(AuditLogable logable);
}
