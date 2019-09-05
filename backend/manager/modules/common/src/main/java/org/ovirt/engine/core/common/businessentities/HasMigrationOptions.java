package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public interface HasMigrationOptions {
    Boolean getAutoConverge();
    void setAutoConverge(Boolean value);

    Boolean getMigrateCompressed();
    void setMigrateCompressed(Boolean value);

    Guid getMigrationPolicyId();
    void setMigrationPolicyId(Guid value);

    Boolean getMigrateEncrypted();
    void setMigrateEncrypted(Boolean value);
}
