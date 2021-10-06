package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class NbdServerVDSParameters extends VdsIdVDSCommandParametersBase {

    private Guid serverId;

    private Guid storageDomainId;

    private Guid imageId;

    private Guid volumeId;

    private boolean readonly;

    private boolean detectZeroes;

    private boolean discard;

    // If true, export entire backing chain under specified volume. Otherwise
    // export only the specified volume.
    private boolean backingChain = true;

    private Guid bitmap;

    public NbdServerVDSParameters() {
    }

    public NbdServerVDSParameters(Guid vdsId) {
        super(vdsId);
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid serverId) {
        this.serverId = serverId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isDetectZeroes() {
        return detectZeroes;
    }

    public void setDetectZeroes(boolean detectZeroes) {
        this.detectZeroes = detectZeroes;
    }

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    public boolean getBackingChain() {
        return backingChain;
    }

    public void setBackingChain(boolean backingChain) {
        this.backingChain = backingChain;
    }

    public Guid getBitmap() {
        return bitmap;
    }

    public void setBitmap(Guid bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("serverId", serverId)
                .append("storageDomainId", storageDomainId)
                .append("imageId", imageId)
                .append("volumeId", volumeId)
                .append("readonly", readonly)
                .append("discard", discard)
                .append("detectZeroes", detectZeroes)
                .append("backingChain", backingChain)
                .append("bitmap", bitmap);
    }
}
