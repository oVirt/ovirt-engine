package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class NFSStorageHelper extends BaseFsStorageHelper {

    @Override
    protected Log getLog() {
        return log;
    }

    private static Log log = LogFactory.getLog(NFSStorageHelper.class);

    @Override
    protected StorageType getType() {
        return StorageType.NFS;
    }
}
