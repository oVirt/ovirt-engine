package org.ovirt.engine.core.bll;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UploadStreamParameters extends ImagesContainterParametersBase {
    @JsonIgnore
    InputStream inputStream;

    @JsonIgnore
    Long streamLength;

    public UploadStreamParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            ByteArrayInputStream inputStream,
            Long streamLength) {
        super(imageId);
        this.inputStream = inputStream;
        this.streamLength = streamLength;
        setStoragePoolId(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
    }

    public UploadStreamParameters() {
        super();
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
}
