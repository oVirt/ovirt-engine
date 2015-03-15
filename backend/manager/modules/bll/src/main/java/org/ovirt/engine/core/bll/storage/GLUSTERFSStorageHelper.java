package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;

/**
 * Storage helper for Posix FS connections
 */
public class GLUSTERFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected StorageType getType() {
        return StorageType.GLUSTERFS;
    }
}
