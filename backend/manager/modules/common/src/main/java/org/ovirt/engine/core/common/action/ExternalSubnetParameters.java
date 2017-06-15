package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;

public class ExternalSubnetParameters extends ActionParametersBase {

    private static final long serialVersionUID = 308877238353433739L;

    @Valid
    @NotNull
    private ExternalSubnet subnet;

    public ExternalSubnetParameters() {
    }

    public ExternalSubnetParameters(ExternalSubnet subnet) {
        super();
        this.subnet = subnet;
    }

    public ExternalSubnet getSubnet() {
        return subnet;
    }

    public void setSubnet(ExternalSubnet subnet) {
        this.subnet = subnet;
    }
}
