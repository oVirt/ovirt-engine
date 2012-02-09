package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class LOCALFSStorageHelper extends BaseFsStorageHelper {
    public LOCALFSStorageHelper() {
        this.storageType = StorageType.LOCALFS;
    }

    @Override
    protected LogCompat getLog() {
        return log;
    }

    private static LogCompat log = LogFactoryCompat.getLog(LOCALFSStorageHelper.class);
}
