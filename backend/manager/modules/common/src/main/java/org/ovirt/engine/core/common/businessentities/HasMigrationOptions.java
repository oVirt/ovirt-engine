package org.ovirt.engine.core.common.businessentities;

public interface HasMigrationOptions {
    Boolean getAutoConverge();
    void setAutoConverge(Boolean value);

    Boolean getMigrateCompressed();
    void setMigrateCompressed(Boolean value);
}
