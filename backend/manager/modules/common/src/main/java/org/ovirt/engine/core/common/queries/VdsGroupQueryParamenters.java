package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class VdsGroupQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1936229921452072377L;

    public VdsGroupQueryParamenters(Guid vdsgroupid) {
        _vdsgroupid = vdsgroupid;
    }

    private Guid _vdsgroupid;

    public Guid getVdsGroupId() {
        return _vdsgroupid;
    }

    public VdsGroupQueryParamenters() {
    }
}
