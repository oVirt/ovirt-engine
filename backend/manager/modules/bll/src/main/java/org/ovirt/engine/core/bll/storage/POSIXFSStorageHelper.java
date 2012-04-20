package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Storage helper for Posix FS connections
 */
public class POSIXFSStorageHelper extends BaseFsStorageHelper {

    public POSIXFSStorageHelper() {
        this.storageType = storageType.POSIXFS;
    }

    @Override
    protected Log getLog() {
        return log;
    }

    private static Log log = LogFactory.getLog(POSIXFSStorageHelper.class);
}
