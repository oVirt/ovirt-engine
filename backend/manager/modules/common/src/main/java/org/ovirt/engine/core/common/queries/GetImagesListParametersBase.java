package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;

/** A base class for parameters of queries that retrieve images lists */
public abstract class GetImagesListParametersBase extends QueryParametersBase {
    private static final long serialVersionUID = 2209924540414198112L;
    private Boolean forceRefresh;
    private ImageFileType imageType;

    public GetImagesListParametersBase() {
        this (ImageFileType.All);
    }

    public GetImagesListParametersBase(ImageFileType imageType) {
        setImageType(imageType);
    }

    public Boolean getForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(Boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    public ImageFileType getImageType() {
        return imageType;
    }

    public void setImageType(ImageFileType value) {
        imageType = value;
    }
}
