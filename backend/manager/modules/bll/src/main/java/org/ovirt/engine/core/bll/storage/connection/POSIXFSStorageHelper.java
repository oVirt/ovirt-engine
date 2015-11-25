package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

/**
 * Storage helper for Posix FS connections
 */
public class POSIXFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected StorageType getType() {
        return StorageType.POSIXFS;
    }
}
