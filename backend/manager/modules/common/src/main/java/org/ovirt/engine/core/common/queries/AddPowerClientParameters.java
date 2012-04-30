package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.action.*;

public class AddPowerClientParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3805094506832541836L;

    public AddPowerClientParameters(AddVdsActionParameters AddVdsParams) {
        _AddVdsParams = AddVdsParams;
    }

    private AddVdsActionParameters _AddVdsParams;

    public AddVdsActionParameters getAddVdsParams() {
        return _AddVdsParams;
    }

    public AddPowerClientParameters() {
    }
}
