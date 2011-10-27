package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllFloppyImagesListQuery<P extends GetAllIsoImagesListParameters> extends QueriesCommandBase<P> {
    public GetAllFloppyImagesListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Fetch all the Iso floppy files for stprage pool with active storage domain of this domain Id.
        Guid sdId = getParameters().getStorageDomainId();
        getQueryReturnValue().setReturnValue(IsoDomainListSyncronizer.getInstance()
                .getUserRequestForStorageDomainRepoFileList(sdId, FileTypeExtension.Floppy, getParameters().getForceRefresh()));
        if (getQueryReturnValue().getReturnValue() == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND.toString());
        }
    }
}
