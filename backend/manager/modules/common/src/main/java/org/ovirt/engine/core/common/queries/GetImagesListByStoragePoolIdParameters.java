package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.compat.Guid;


public class GetImagesListByStoragePoolIdParameters extends GetImagesListParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetImagesListByStoragePoolIdParameters() {
    }

    public GetImagesListByStoragePoolIdParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public GetImagesListByStoragePoolIdParameters(Guid storagePoolId, ImageFileType imageType) {
        super(imageType);
        setStoragePoolId(storagePoolId);
    }

    private Guid storagePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }
}
