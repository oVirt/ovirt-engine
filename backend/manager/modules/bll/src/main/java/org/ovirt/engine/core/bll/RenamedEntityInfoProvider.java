package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;

public interface RenamedEntityInfoProvider {

    String getEntityType();
    String getEntityOldName();
    String getEntityNewName();
    void setEntityId(AuditLogable logable);
}
