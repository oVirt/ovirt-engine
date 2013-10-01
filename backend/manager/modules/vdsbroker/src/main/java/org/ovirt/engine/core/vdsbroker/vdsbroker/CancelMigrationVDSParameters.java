package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class CancelMigrationVDSParameters extends VdsAndVmIDVDSParametersBase {

    private boolean rerunAfterCancel;

    public CancelMigrationVDSParameters(Guid vdsId, Guid vmId, boolean rerunAfterCancel) {
        super(vdsId, vmId);
        this.rerunAfterCancel = rerunAfterCancel;
    }

    public boolean isRerunAfterCancel() {
        return rerunAfterCancel;
    }
}
