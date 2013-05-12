package org.ovirt.engine.core.bll.scheduling;

import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface MigrationHandler {
    /**
     * this method holds a list of pairs VM id and Host id. each VM should be migrated to the specified Host
     * @param list
     */
    void migrateVMs(List<Pair<Guid, Guid>> list);
}
