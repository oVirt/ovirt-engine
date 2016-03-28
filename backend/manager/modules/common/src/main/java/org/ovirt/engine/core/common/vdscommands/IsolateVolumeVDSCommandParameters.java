package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class IsolateVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {
    private Guid image;
    private Guid sourceImageGroupId;
    private Guid destImageGroupId;

    public IsolateVolumeVDSCommandParameters() {
    }

    public IsolateVolumeVDSCommandParameters(Guid storageDomainId, Guid jobId, Guid image, Guid sourceImageGroupId,
                                             Guid destImageGroupId) {
        super(storageDomainId, jobId);
        this.image = image;
        this.sourceImageGroupId = sourceImageGroupId;
        this.destImageGroupId = destImageGroupId;
    }

    public Guid getSourceImageGroupId() {
        return sourceImageGroupId;
    }

    public void setSourceImageGroupId(Guid sourceImageGroupId) {
        this.sourceImageGroupId = sourceImageGroupId;
    }

    public Guid getDestImageGroupId() {
        return destImageGroupId;
    }

    public void setDestImageGroupId(Guid destImageGroupId) {
        this.destImageGroupId = destImageGroupId;
    }

    public Guid getImage() {
        return image;
    }

    public void setImage(Guid image) {
        this.image = image;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("image", getImage())
                .append("sourceImageGroupId", getSourceImageGroupId())
                .append("destImageGroupId", getDestImageGroupId());
    }
}
