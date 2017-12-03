package org.ovirt.engine.core.bll.storage.repoimage;


import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * This query retrieves repo files for a storage pool.
 * Note that there are no permissions on non-data storage domains, so this query is filtered according to the permissions of the storage pool
 */
public class GetImagesListByStoragePoolIdQuery<P extends GetImagesListByStoragePoolIdParameters> extends GetImagesListQueryBase<P> {
    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private DiskImageDao diskImageDao;

    public GetImagesListByStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override protected void executeQueryCommand() {
        super.executeQueryCommand();
        if (ImageFileType.ISO == getParameters().getImageType()) {
            ((List<RepoImage>) getQueryReturnValue().getReturnValue()).addAll(
                    diskImageDao.getIsoDisksForStoragePoolAsRepoImages(getParameters().getStoragePoolId()));
        }
    }

    /**
     * @return The storage domain to get the images from
     */
    @Override
    protected Guid getStorageDomainIdForQuery() {
        if (doesUserHavePermissionsOnStoragePool()) {
            return storageDomainDao.getIsoStorageDomainIdForPool(getStoragePoolId(), StorageDomainStatus.Active);
        }
        return null;
    }

    /**
     * Checks if the user have query permissions on the storage <b>pool</b>.
     * @return <code>true</code> if the user has permissions on the storage pool, <code>false</code> if not.
     */
    private boolean doesUserHavePermissionsOnStoragePool() {
        return storagePoolDao.get(getStoragePoolId(), getUserID(), getParameters().isFiltered()) != null;
    }

    /**
     * @return The Storage Pool ID from the parameters
     */
    private Guid getStoragePoolId() {
        return getParameters().getStoragePoolId();
    }
}
