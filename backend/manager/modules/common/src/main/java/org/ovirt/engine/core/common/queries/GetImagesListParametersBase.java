package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;

/** A base class for parameters of queries that retrieve images lists */
public abstract class GetImagesListParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2562476365144558247L;
    private boolean forceRefresh;
    private ImageFileType imageType;

    public GetImagesListParametersBase() {
        this (ImageFileType.All);
    }

    public GetImagesListParametersBase(ImageFileType imageType) {
        setImageType(imageType);
    }

    public boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public ImageFileType getImageType() {
        return imageType;
    }

    public void setImageType(ImageFileType value) {
        imageType = value;
    }
}
