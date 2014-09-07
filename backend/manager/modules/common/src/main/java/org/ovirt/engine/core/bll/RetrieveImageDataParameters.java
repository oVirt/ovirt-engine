package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class RetrieveImageDataParameters extends ImagesContainterParametersBase {
    private Long length;

    public RetrieveImageDataParameters(Guid storagePoolId,
                                  Guid storageDomainId,
                                  Guid imageGroupId,
                                  Guid imageId,
                                  Long length) {
        super(imageId);
        this.length = length;
        setStoragePoolId(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
    }

    public RetrieveImageDataParameters() {
        super();
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}
