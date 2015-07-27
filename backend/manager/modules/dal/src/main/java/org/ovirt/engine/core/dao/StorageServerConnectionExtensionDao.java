package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;

public interface StorageServerConnectionExtensionDao extends GenericDao<StorageServerConnectionExtension, Guid> {
    List<StorageServerConnectionExtension> getByHostId(Guid hostId);

    StorageServerConnectionExtension getByHostIdAndTarget(Guid hostId, String target);
}
