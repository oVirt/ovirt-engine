package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllIsoImagesListQuery<P extends GetAllIsoImagesListParameters> extends QueriesCommandBase<P> {
    public GetAllIsoImagesListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid sdId = getParameters().getStorageDomainId();
        getQueryReturnValue().setReturnValue(IsoDomainListSyncronizer.getInstance()
                .getUserRequestForStorageDomainRepoFileList(sdId, FileTypeExtension.ISO, getParameters().getForceRefresh()));
        if (getQueryReturnValue().getReturnValue() == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND.toString());
        }
    }
}
