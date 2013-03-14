package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;

/** A base class for parameters of queries that retrieve images lists */
public abstract class GetImagesListParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2562476365144558247L;
    private boolean forceRefresh;
    private FileTypeExtension fileTypeExt = FileTypeExtension.All;

    public GetImagesListParametersBase() {
    }

    public GetImagesListParametersBase(FileTypeExtension fileTypeExt) {
        setFileTypeExtension(fileTypeExt);
    }

    public boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public FileTypeExtension getFileTypeExtension() {
        return fileTypeExt;
    }

    public void setFileTypeExtension(FileTypeExtension value) {
        fileTypeExt = value;
    }
}
