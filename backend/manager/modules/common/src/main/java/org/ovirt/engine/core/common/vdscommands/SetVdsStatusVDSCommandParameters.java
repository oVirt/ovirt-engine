package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

public class SetVdsStatusVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private VDSStatus _status = VDSStatus.forValue(0);
    private NonOperationalReason nonOperationalReason = NonOperationalReason.NONE;

    public SetVdsStatusVDSCommandParameters(Guid vdsId, VDSStatus status) {
        super(vdsId);
        _status = status;
    }

    public SetVdsStatusVDSCommandParameters(Guid vdsId, VDSStatus status, NonOperationalReason nonOperationalReason) {
        this(vdsId, status);
        this.nonOperationalReason = nonOperationalReason;
    }

    public VDSStatus getStatus() {
        return _status;
    }

    public SetVdsStatusVDSCommandParameters() {
    }

    public NonOperationalReason getNonOperationalReason() {
        return nonOperationalReason;
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = (nonOperationalReason == null ? NonOperationalReason.NONE : nonOperationalReason);
    }

    @Override
    public String toString() {
        return String.format("%s, status=%s, nonOperationalReason=%s",
                super.toString(),
                getStatus(),
                getNonOperationalReason());
    }
}
