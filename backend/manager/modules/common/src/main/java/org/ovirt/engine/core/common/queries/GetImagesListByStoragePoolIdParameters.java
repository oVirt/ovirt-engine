package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.compat.Guid;


public class GetImagesListByStoragePoolIdParameters extends GetImagesListParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetImagesListByStoragePoolIdParameters() {
    }

    public GetImagesListByStoragePoolIdParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public GetImagesListByStoragePoolIdParameters(Guid storagePoolId, FileTypeExtension fileTypeExt) {
        super(fileTypeExt);
        setStoragePoolId(storagePoolId);
    }

    private Guid storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }
}
