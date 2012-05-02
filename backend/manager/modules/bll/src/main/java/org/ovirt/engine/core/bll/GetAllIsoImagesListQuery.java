package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;

public class GetAllIsoImagesListQuery<P extends GetAllIsoImagesListParameters> extends AbstractGetAllImagesListByStorageDomainIdQuery<P> {
    public GetAllIsoImagesListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected FileTypeExtension getFileTypeExtension() {
        return FileTypeExtension.ISO;
    }
}
