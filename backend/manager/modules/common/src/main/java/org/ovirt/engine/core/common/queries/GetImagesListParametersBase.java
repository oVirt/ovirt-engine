package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ImageType;

/** A base class for parameters of queries that retrieve images lists */
public abstract class GetImagesListParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2562476365144558247L;
    private boolean forceRefresh;
    private ImageType imageType = ImageType.All;

    public GetImagesListParametersBase() {
    }

    public GetImagesListParametersBase(ImageType imageType) {
        setImageType(imageType);
    }

    public boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType value) {
        imageType = value;
    }
}
