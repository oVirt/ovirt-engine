package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ProcessDownVmParameters extends IdParameters {
    private static final long serialVersionUID = 6766562035910087308L;

    private boolean skipHostRefresh;

    private Guid hostId;

    public ProcessDownVmParameters() {
        super();
    }

    public ProcessDownVmParameters(Guid id, Guid hostId) {
        super(id);
        this.hostId = hostId;
    }

    public ProcessDownVmParameters(Guid id, boolean skipHostRefresh, Guid hostId) {
        this(id, hostId);
        this.skipHostRefresh = skipHostRefresh;
    }

    public boolean isSkipHostRefresh() {
        return skipHostRefresh;
    }

    public Guid getHostId() {
        return hostId;
    }
}
