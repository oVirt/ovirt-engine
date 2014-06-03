package org.ovirt.engine.core.common.vdscommands;

import java.io.InputStream;

import org.ovirt.engine.core.compat.Guid;

public class UploadStreamVDSCommandParameters extends ImageHttpAccessVDSCommandParameters {
    private InputStream inputStream;

    public UploadStreamVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId, Long size, InputStream inputStream) {
        super(vdsId, storagePoolId, storageDomainId, imageGroupId, imageId, size);
        this.inputStream = inputStream;
    }

    public UploadStreamVDSCommandParameters() {
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
