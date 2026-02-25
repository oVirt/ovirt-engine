package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DeleteImageUnusedLinksVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    /**
     * ID storage domain.
     */
    private Guid sdUUID;

    /**
     * ID storage pool.
     */
    private Guid spUUID;

    /**
     * ID image.
     */
    private Guid imgUUID;

    public DeleteImageUnusedLinksVDSCommandParameters() {
    }

    public DeleteImageUnusedLinksVDSCommandParameters(Guid vdsId, Guid sdUUID, Guid spUUID, Guid imgUUID) {
        super(vdsId);
        this.sdUUID = sdUUID;
        this.spUUID = spUUID;
        this.imgUUID = imgUUID;
    }

    public Guid getSdUUID() {
        return sdUUID;
    }

    public void setSdUUID(Guid sdUUID) {
        this.sdUUID = sdUUID;
    }

    public Guid getSpUUID() {
        return spUUID;
    }

    public void setSpUUID(Guid spUUID) {
        this.spUUID = spUUID;
    }

    public Guid getImgUUID() {
        return imgUUID;
    }

    public void setImgUUID(Guid imgUUID) {
        this.imgUUID = imgUUID;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("sdUUID", getSdUUID())
                .append("spUUID", getSpUUID())
                .append("imgUUID", getImgUUID());
    }
}
