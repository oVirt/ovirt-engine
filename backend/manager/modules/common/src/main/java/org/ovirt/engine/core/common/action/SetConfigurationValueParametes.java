package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class SetConfigurationValueParametes extends VdcActionParametersBase {
    private static final long serialVersionUID = -4966875942424830052L;

    public SetConfigurationValueParametes(VdcOption option) {
        setOption(option);
    }

    private VdcOption privateOption;

    public VdcOption getOption() {
        return privateOption;
    }

    private void setOption(VdcOption value) {
        privateOption = value;
    }

    public SetConfigurationValueParametes() {
    }
}
