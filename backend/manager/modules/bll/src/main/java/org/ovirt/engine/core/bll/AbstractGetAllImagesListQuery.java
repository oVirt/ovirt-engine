package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.GetAllImagesListParametersBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractGetAllImagesListQuery<P extends GetAllImagesListParametersBase> extends QueriesCommandBase<P> {

    public AbstractGetAllImagesListQuery(P parameters) {
        super(parameters);
    }

    protected abstract FileTypeExtension getFileTypeExtension();

    protected abstract Guid getStorageDomainId();

    @Override
    protected void executeQueryCommand() {
        // Fetch all the Iso files of a given type for storage pool with active storage domain of this domain Id.
        getQueryReturnValue().setReturnValue(getUserRequestForStorageDomainRepoFileList());
        if (getQueryReturnValue().getReturnValue() == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND.toString());
        }
    }

    protected List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList() {
        return IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList
                (getStorageDomainId(),
                        getFileTypeExtension(),
                        getParameters().getForceRefresh());
    }
}
