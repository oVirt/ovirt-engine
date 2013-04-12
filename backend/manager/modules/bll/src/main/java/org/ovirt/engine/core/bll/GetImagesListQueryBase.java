package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
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
        if (getQueryReturnValue().getReturnValue() == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND.toString());
        }
    }

    /**
     * @return The storage domain to get the images from
     */
    protected abstract Guid getStorageDomainId();

    protected List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList() {
        return IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList
                (getStorageDomainId(), getParameters().getImageType(), getParameters().getForceRefresh());
    }
}
