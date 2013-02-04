package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * This query retrieves repo files for a storage pool.
 * Note that there are no permissions on non-data storage domains, so this query is filtered according to the permissions of the storage pool
 */
public abstract class AbstractGetAllImagesListByStoragePoolIdQuery<P extends GetAllImagesListByStoragePoolIdParameters> extends AbstractGetAllImagesListQuery<P> {

    public AbstractGetAllImagesListByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected Guid getStorageDomainId() {
        if (doesUserHavePermissionsOnStoragePool()) {
            return getDbFacade().getStorageDomainDao().getIsoStorageDomainIdForPool(getStoragePoolId());
        }
        return null;
    }

    @Override
    protected List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList() {
        return IsoDomainListSyncronizer.getInstance().getUserRequestForStoragePoolAndDomainRepoFileList
                (getStoragePoolId(), getStorageDomainId(),
                        getFileTypeExtension(),
                        getParameters().getForceRefresh());
    }

    /**
     * Checks if the user have query permissions on the storage <b>pool</b>.
     * @return <code>true</code> if the user has permissions on the storage pool, <code>false</code> if not.
     */
    private boolean doesUserHavePermissionsOnStoragePool() {
        storage_pool pool =
                getDbFacade().getStoragePoolDao().get(getStoragePoolId(),
                        getUserID(),
                        getParameters().isFiltered());
        return pool != null;
    }

    /**
     * @return The Storage Pool ID from the parameters
     */
    private Guid getStoragePoolId() {
        return getParameters().getStoragePoolId();
    }
}
