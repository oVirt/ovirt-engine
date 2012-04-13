package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Storage helper for Posix FS connections
 */
public class POSIXFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected Log getLog() {
        return log;
    }

    private static final Log log = LogFactory.getLog(POSIXFSStorageHelper.class);

    @Override
    protected StorageType getType() {
        return StorageType.POSIXFS;
    }
}
