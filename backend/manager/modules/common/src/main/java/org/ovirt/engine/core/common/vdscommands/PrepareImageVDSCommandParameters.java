package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class PrepareImageVDSCommandParameters extends ImageActionsVDSCommandParameters{
    private boolean allowIllegal;

    public PrepareImageVDSCommandParameters(Guid vdsId, Guid spId, Guid sdId, Guid imgGroupId,
            Guid imgId, boolean allowIllegal) {
        super(vdsId, spId, sdId, imgGroupId, imgId);
        setAllowIllegal(allowIllegal);
    }
    public PrepareImageVDSCommandParameters() {};

    public void setAllowIllegal(boolean allowIllegal) {
        this.allowIllegal = allowIllegal;
    }

    public boolean getAllowIllegal() {
        return allowIllegal;
    }
}
