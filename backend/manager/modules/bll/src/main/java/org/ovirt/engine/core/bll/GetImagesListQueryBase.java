package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParametersBase;
import org.ovirt.engine.core.compat.Guid;


public abstract class GetImagesListQueryBase<P extends GetImagesListParametersBase> extends QueriesCommandBase<P> {

    public GetImagesListQueryBase(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Fetch all the Iso files of a given type for storage pool with active storage domain of this domain Id.
        getQueryReturnValue().setReturnValue(getUserRequestForStorageDomainRepoFileList());
    }

    /**
     * @return The storage domain to get the images from
     */
    protected abstract Guid getStorageDomainIdForQuery();

    protected List<RepoImage> getUserRequestForStorageDomainRepoFileList() {
        return IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList
                (getStorageDomainIdForQuery(), getParameters().getImageType(), getParameters().getForceRefresh());
    }
}
