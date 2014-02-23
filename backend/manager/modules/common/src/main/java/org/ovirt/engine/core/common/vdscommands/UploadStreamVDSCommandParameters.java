package org.ovirt.engine.core.common.vdscommands;

import java.io.InputStream;

import org.ovirt.engine.core.compat.Guid;

public class UploadStreamVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private InputStream inputStream;
    private Long streamLength;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;

    public UploadStreamVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId, InputStream inputStream, Long streamLength) {
        super(vdsId);
        this.inputStream = inputStream;
        this.streamLength = streamLength;
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
    }

    public UploadStreamVDSCommandParameters() {
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Long getStreamLength() {
        return streamLength;
    }

    public void setStreamLength(Long streamLength) {
        this.streamLength = streamLength;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }
}
