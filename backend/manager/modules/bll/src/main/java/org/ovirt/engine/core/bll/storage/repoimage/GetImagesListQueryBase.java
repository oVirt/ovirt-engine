package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParametersBase;
import org.ovirt.engine.core.compat.Guid;


public abstract class GetImagesListQueryBase<P extends GetImagesListParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

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
        Guid storageDomainId = getStorageDomainIdForQuery();
        if (Guid.Empty.equals(storageDomainId)) {
            return new ArrayList<>();
        }

        return isoDomainListSynchronizer.getUserRequestForStorageDomainRepoFileList
                (storageDomainId, getParameters().getImageType(), getParameters().getForceRefresh());
    }
}
