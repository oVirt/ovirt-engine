package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.IscsiBond;

public class EditIscsiBondParameters extends ActionParametersBase {

    private static final long serialVersionUID = -5787650546020967357L;

    @Valid
    private IscsiBond iscsiBond;

    public EditIscsiBondParameters() {
    }

    public EditIscsiBondParameters(IscsiBond iscsiBond) {
        setIscsiBond(iscsiBond);
    }

    public IscsiBond getIscsiBond() {
        return iscsiBond;
    }

    public void setIscsiBond(IscsiBond iscsiBond) {
        this.iscsiBond = iscsiBond;
    }
}
