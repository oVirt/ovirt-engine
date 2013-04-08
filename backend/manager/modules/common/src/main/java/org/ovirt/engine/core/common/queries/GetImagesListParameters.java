package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.compat.Guid;


public class GetImagesListParameters extends GetImagesListParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetImagesListParameters() {
    }

    public GetImagesListParameters(Guid storageDomainId) {
        setStorageDomainId(storageDomainId);
    }

    public GetImagesListParameters(Guid storageDomainId, ImageFileType imageType) {
        super(imageType);
        setStorageDomainId(storageDomainId);
    }

    private Guid storageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }
}
