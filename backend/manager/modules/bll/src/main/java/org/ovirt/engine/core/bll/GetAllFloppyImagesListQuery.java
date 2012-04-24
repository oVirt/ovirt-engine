package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;

public class GetAllFloppyImagesListQuery<P extends GetAllIsoImagesListParameters> extends AbstractGetAllImagesListQuery<P> {
    public GetAllFloppyImagesListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected FileTypeExtension getFileTypeExtension() {
        return FileTypeExtension.Floppy;
    }
}
