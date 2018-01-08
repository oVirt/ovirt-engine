package org.ovirt.engine.core.common.vdscommands;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.compat.Guid;

public class TimeBoundPollVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private PollTechnique pollTechnique;
    private long timeout;
    private TimeUnit unit;

    public enum PollTechnique {
        /**
         * general poll technique (Host.ping) used for cluster compatibility < 4.2
         */
        @Deprecated
        POLL,
        /**
         * poll using Host.ping2 which enables the engine to ascertain the host is
         * reachable
         */
        POLL2,
        /**
         * poll using Host.confirmConnectivity which enables vdsm to ascertain the
         * engine is reachable
         */
        CONFIRM_CONNECTIVITY
    }

    public TimeBoundPollVDSCommandParameters(Guid vdsId, PollTechnique pollTechnique) {
        this(vdsId, pollTechnique, 2, TimeUnit.SECONDS);
    }

    TimeBoundPollVDSCommandParameters(Guid vdsId, PollTechnique pollTechnique, long timeout, TimeUnit unit) {
        super(vdsId);
        this.pollTechnique = pollTechnique;
        this.timeout = timeout;
        this.unit = unit;
    }

    private TimeBoundPollVDSCommandParameters() {
        // only here because checkstyle demands a no-args c'tor
    }

    public PollTechnique getPollTechnique() {
        return pollTechnique;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

}
