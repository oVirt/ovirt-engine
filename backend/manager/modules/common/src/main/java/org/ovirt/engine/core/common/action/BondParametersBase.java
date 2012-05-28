package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public abstract class BondParametersBase extends VdsActionParameters {

    private static final long serialVersionUID = 737030937575537504L;

    private String bondName;

    public BondParametersBase() {
    }

    public BondParametersBase(Guid vdsId, String bondName) {
        super(vdsId);
        setBondName(bondName);
    }

    public String getBondName() {
        return bondName;
    }

    private void setBondName(String value) {
        bondName = value;
    }

}
