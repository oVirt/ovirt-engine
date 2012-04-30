package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class RemoveBondParameters extends VdsActionParameters {
    private static final long serialVersionUID = 8082833148763122313L;
    private String privateBondName;

    public String getBondName() {
        return privateBondName;
    }

    private void setBondName(String value) {
        privateBondName = value;
    }

    public RemoveBondParameters(Guid vdsId, String bondName) {
        super(vdsId);
        setBondName(bondName);
    }

    public RemoveBondParameters() {
    }
}
