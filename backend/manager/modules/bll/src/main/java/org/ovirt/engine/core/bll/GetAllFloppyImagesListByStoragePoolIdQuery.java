package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.queries.GetAllImagesListByStoragePoolIdParameters;

public class GetAllFloppyImagesListByStoragePoolIdQuery<P extends GetAllImagesListByStoragePoolIdParameters> extends AbstractGetAllImagesListByStoragePoolIdQuery<P> {

    public GetAllFloppyImagesListByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected FileTypeExtension getFileTypeExtension() {
        return FileTypeExtension.Floppy;
    }

}
