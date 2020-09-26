package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for the disk snapshots queries
 */
public class DiskSnapshotsQueryParameters extends IdQueryParameters {

    private static final long serialVersionUID = 160560832090642566L;

    // If true, query includes active disk snapshots. Otherwise active disk snapshots
    // are filtered out. For backward compatibility we must keep the default false.
    private boolean includeActive;

    public DiskSnapshotsQueryParameters() {
    }

    public DiskSnapshotsQueryParameters(Guid id, boolean includeActive) {
        super(id);
        this.includeActive = includeActive;
    }

    public boolean getIncludeActive() {
        return includeActive;
    }
}
