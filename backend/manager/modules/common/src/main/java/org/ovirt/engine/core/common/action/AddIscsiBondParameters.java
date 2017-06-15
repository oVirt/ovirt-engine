package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.IscsiBond;

public class AddIscsiBondParameters extends ActionParametersBase {

    private static final long serialVersionUID = 8709128730744559712L;

    @Valid
    private IscsiBond iscsiBond;

    public AddIscsiBondParameters() {
    }

    public AddIscsiBondParameters(IscsiBond iscsiBond) {
        this.iscsiBond = iscsiBond;
    }

    public IscsiBond getIscsiBond() {
        return iscsiBond;
    }

}
