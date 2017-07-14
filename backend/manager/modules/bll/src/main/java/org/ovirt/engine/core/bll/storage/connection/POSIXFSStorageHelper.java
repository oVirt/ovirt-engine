package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

/**
 * Storage helper for Posix FS connections
 */
@Singleton
public class POSIXFSStorageHelper extends BaseFsStorageHelper {

    @Override
    public StorageType getType() {
        return StorageType.POSIXFS;
    }
}
