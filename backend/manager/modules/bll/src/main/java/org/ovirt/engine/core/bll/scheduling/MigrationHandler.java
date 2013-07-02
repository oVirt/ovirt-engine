package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public interface MigrationHandler {
    /**
     * delegate for migrating a vm to a set of hosts provided by the scheduler
     * @param initialHosts
     * @param vmToMigrate
     */
    void migrateVM(ArrayList<Guid> initialHosts, Guid vmToMigrate);
}
