package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Storage helper for Posix FS connections
 */
public class POSIXStorageHelper extends BaseFsStorageHelper {

    public POSIXStorageHelper() {
        this.storageType = storageType.POSIX;
    }

    @Override
    protected Log getLog() {
        return log;
    }

    private static Log log = LogFactory.getLog(POSIXStorageHelper.class);
}
