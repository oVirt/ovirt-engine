package org.ovirt.engine.core.bll.storage.repoimage;


import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * This query retrieves repo files for a storage pool.
 * Note that there are no permissions on non-data storage domains, so this query is filtered according to the permissions of the storage pool
 */
public class GetImagesListByStoragePoolIdQuery<P extends GetImagesListByStoragePoolIdParameters> extends GetImagesListQueryBase<P> {

    public GetImagesListByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    /**
     * @return The storage domain to get the images from
     */
    @Override
    protected Guid getStorageDomainIdForQuery() {
        if (doesUserHavePermissionsOnStoragePool()) {
            return getDbFacade().getStorageDomainDao().getIsoStorageDomainIdForPool(
                    getStoragePoolId(), StorageDomainStatus.Active);
        }
        return null;
    }

    /**
     * Checks if the user have query permissions on the storage <b>pool</b>.
     * @return <code>true</code> if the user has permissions on the storage pool, <code>false</code> if not.
     */
    private boolean doesUserHavePermissionsOnStoragePool() {
        StoragePool pool =
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
