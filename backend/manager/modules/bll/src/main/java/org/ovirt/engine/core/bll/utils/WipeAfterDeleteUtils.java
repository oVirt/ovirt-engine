package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class WipeAfterDeleteUtils {

    private static final boolean WIPE_AFTER_DELETE_FILE_DOMAIN = false;

    public static boolean getDefaultWipeAfterDeleteFlag(final StorageType storageType) {
        if (storageType.isBlockDomain()) {
            return Config.<Boolean> getValue(ConfigValues.SANWipeAfterDelete);
        } else {
            return WIPE_AFTER_DELETE_FILE_DOMAIN;
        }
    }
}
