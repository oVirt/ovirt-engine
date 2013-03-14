package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.compat.Guid;


public class GetImagesListQuery<P extends GetImagesListParameters> extends GetImagesListQueryBase<P> {

    public GetImagesListQuery(P parameters) {
        super(parameters);
    }

    protected List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList() {
        return IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList
                (getStorageDomainId(),
                        getParameters().getImageType(),
                        getParameters().getForceRefresh());
    }

    /**
     * @return The storage domain to get the images from
     */
    protected Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }
}
