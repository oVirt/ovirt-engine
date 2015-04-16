package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ProcessDownVmParameters extends IdParameters {
    private static final long serialVersionUID = 6766562035910087308L;

    private boolean skipHostRefresh;

    public ProcessDownVmParameters() {
        super();
    }

    public ProcessDownVmParameters(Guid id) {
        super(id);
    }

    public ProcessDownVmParameters(Guid id, boolean skipHostRefresh) {
        this(id);
        this.skipHostRefresh = skipHostRefresh;
    }

    public boolean isSkipHostRefresh() {
        return skipHostRefresh;
    }
}
