package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.utils.AbstractPropertiesTestBase;

public class AuditLogMessagesTest extends AbstractPropertiesTestBase<AuditLogType> {
    public AuditLogMessagesTest() {
        super(AuditLogType.class, "src/main/resources/bundles/AuditLogMessages.properties");
    }
}
