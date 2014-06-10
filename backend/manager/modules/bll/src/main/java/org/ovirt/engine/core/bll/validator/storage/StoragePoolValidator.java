package org.ovirt.engine.core.bll.validator.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * CanDoAction validation methods for storage pool handling
 */
public class StoragePoolValidator {
    private StoragePool storagePool;

    public StoragePoolValidator(StoragePool storagePool) {
        this.storagePool = storagePool;
    }

    /**
     * Checks that the DC compatibility version supports Posix domains.
     * In case there is mismatch, a proper canDoAction message will be added
     *
     * @return The result of the validation
     */
    public ValidationResult isPosixSupportedInDC() {
        if (!storagePool.isLocal() &&
                !Config.<Boolean> getValue(ConfigValues.PosixStorageEnabled, storagePool.getcompatibility_version().toString())) {
            return new ValidationResult(VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return ValidationResult.VALID;
    }

    /**
     * Checks that the DC compatibility version supports Gluster domains.
     * In case there is mismatch, a proper canDoAction message will be added
     *
     * @return true if the version matches
     */
    public ValidationResult isGlusterSupportedInDC() {
        if (!storagePool.isLocal() &&
                !Config.<Boolean> getValue(ConfigValues.GlusterFsStorageEnabled, storagePool.getcompatibility_version().toString())) {
            return new ValidationResult(VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return ValidationResult.VALID;
    }

    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    public StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
        return DbFacade.getInstance().getStoragePoolIsoMapDao();
    }

    public ValidationResult isNotLocalfsWithDefaultCluster() {
        if (storagePool.isLocal() && containsDefaultCluster()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS);
        }
        return ValidationResult.VALID;
    }

    protected boolean containsDefaultCluster() {
        List<VDSGroup> clusters = getVdsGroupDao().getAllForStoragePool(storagePool.getId());
        boolean hasDefaultCluster = false;
        for (VDSGroup cluster : clusters) {
            if (cluster.getId().equals(Config.getValue(ConfigValues.AutoRegistrationDefaultVdsGroupID))) {
                hasDefaultCluster = true;
                break;
            }
        }
        return hasDefaultCluster;
    }

    public ValidationResult isUp() {
        if (storagePool == null || storagePool.getStatus() != StoragePoolStatus.Up) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isAnyDomainInProcess() {
        List<StoragePoolIsoMap> poolIsoMaps = getStoragePoolIsoMapDao().getAllForStoragePool(storagePool.getId());

        for (StoragePoolIsoMap domainIsoMap : poolIsoMaps) {
            if (domainIsoMap.getStatus() != null && domainIsoMap.getStatus().isStorageDomainInProcess()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                        String.format("$status %1$s", domainIsoMap.getStatus()));
            }
        }

        return ValidationResult.VALID;
    }
}
