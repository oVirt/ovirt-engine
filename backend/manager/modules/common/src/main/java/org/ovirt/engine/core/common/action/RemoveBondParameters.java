package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveBondParameters extends BondParametersBase {

    private static final long serialVersionUID = 8082833148763122313L;

    public RemoveBondParameters() {
    }

    public RemoveBondParameters(Guid vdsId, String bondName) {
        super(vdsId, bondName);
    }

}
