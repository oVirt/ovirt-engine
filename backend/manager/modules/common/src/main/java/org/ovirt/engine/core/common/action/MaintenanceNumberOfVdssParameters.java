package org.ovirt.engine.core.common.action;

import java.util.LinkedList;

import org.ovirt.engine.core.compat.Guid;

public class MaintenanceNumberOfVdssParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 8806810521151850069L;

    private java.util.List<Guid> _vdsIdList;

    private boolean _isInternal;

    public MaintenanceNumberOfVdssParameters(java.util.List<Guid> vdsIdList, boolean isInternal) {
        _vdsIdList = vdsIdList;
        _isInternal = isInternal;
    }

    public Iterable<Guid> getVdsIdList() {
        return _vdsIdList == null ? new LinkedList<Guid>() : _vdsIdList;
    }

    public void setVdsIdList(java.util.List<Guid> value) {
        _vdsIdList = value;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public MaintenanceNumberOfVdssParameters() {
    }
}
