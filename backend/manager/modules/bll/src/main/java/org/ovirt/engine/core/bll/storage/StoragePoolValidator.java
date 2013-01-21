package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * CanDoAction validation methods for storage pool handling
 */
public class StoragePoolValidator {
    private storage_pool storagePool;
    private List<String> canDoActionMessages;

    public StoragePoolValidator(storage_pool storagePool, List<String> canDoActionMessages) {
        this.storagePool = storagePool;
        this.canDoActionMessages = canDoActionMessages;
    }

    /**
     * Checks in case the DC is of POSIX type that the compatibility version matches. In case there is mismatch, a
     * proper canDoAction message will be added
     *
     * @return true if the version matches
     */
    public boolean isPosixDcAndMatchingCompatiblityVersion() {
        if (storagePool.getstorage_pool_type() == StorageType.POSIXFS
                && !Config.<Boolean> GetValue
                        (ConfigValues.PosixStorageEnabled, storagePool.getcompatibility_version().toString())) {
            canDoActionMessages.add(VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION.toString());
            return false;
        }
        return true;
    }

    public List<String> getCanDoActionMessages() {
        return canDoActionMessages;
    }

    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    public boolean isNotLocalfsWithDefaultCluster() {
        if (storagePool.getstorage_pool_type() == StorageType.LOCALFS && containsDefaultCluster()) {
            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS.toString());
            return false;
        }
        return true;
    }

    protected boolean containsDefaultCluster() {
        List<VDSGroup> clusters = getVdsGroupDao().getAllForStoragePool(storagePool.getId());
        boolean hasDefaultCluster = false;
        for (VDSGroup cluster : clusters) {
            if (cluster.getId().equals(VDSGroup.DEFAULT_VDS_GROUP_ID)) {
                hasDefaultCluster = true;
                break;
            }
        }
        return hasDefaultCluster;
    }

}
