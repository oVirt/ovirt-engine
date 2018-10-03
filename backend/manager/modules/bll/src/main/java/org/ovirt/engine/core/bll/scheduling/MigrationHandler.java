package org.ovirt.engine.core.bll.scheduling;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public interface MigrationHandler {
    /**
     * delegate for migrating a vm to a set of hosts provided by the scheduler
     */
    boolean migrateVM(List<Guid> initialHosts, Guid vmToMigrate, String reason);
}
