package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskCommandParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 2876214350273132268L;
    private String _cdImagePath;

    public ChangeDiskCommandParameters(Guid vmId, String cdImagePath) {
        super(vmId);
        _cdImagePath = cdImagePath;
    }

    public String getCdImagePath() {
        return _cdImagePath;
    }

    public ChangeDiskCommandParameters() {
    }
}
