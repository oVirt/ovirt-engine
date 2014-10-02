package org.ovirt.engine.core.common.vdscommands;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.compat.Guid;

public class TimeBoundPollVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private long timeout;
    private TimeUnit unit;

    public TimeBoundPollVDSCommandParameters() {
    }

    public TimeBoundPollVDSCommandParameters(Guid vdsId, long timeout, TimeUnit unit) {
        super(vdsId);
        this.timeout = timeout;
        this.unit = unit;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

}
