package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.queries.GetAllImagesListByStoragePoolIdParameters;

public class GetAllIsoImagesListByStoragePoolIdQuery<P extends GetAllImagesListByStoragePoolIdParameters> extends AbstractGetAllImagesListByStoragePoolIdQuery<P> {

    public GetAllIsoImagesListByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected FileTypeExtension getFileTypeExtension() {
        return FileTypeExtension.ISO;
    }

}
