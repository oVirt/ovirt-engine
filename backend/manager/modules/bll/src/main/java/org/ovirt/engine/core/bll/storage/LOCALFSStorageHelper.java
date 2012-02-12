package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class LOCALFSStorageHelper extends BaseFsStorageHelper {
    public LOCALFSStorageHelper() {
        this.storageType = StorageType.LOCALFS;
    }

    @Override
    protected Log getLog() {
        return log;
    }

    private static Log log = LogFactory.getLog(LOCALFSStorageHelper.class);
}
