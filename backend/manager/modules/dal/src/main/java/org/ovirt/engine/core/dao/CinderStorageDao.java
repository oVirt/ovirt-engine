package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.compat.Guid;

public interface CinderStorageDao extends GenericDao<ManagedBlockStorage, Guid> {

    /**
     * Gets the Managed block storage with the specified drivers.
     *
     * @param driverOptions
     *            the driver options
     * @return managed block domain with the same drivers
     */
    List<ManagedBlockStorage> getCinderStorageByDrivers(Map<String, Object> driverOptions);
}
