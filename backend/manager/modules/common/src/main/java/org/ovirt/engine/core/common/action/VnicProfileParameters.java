package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;

public class VnicProfileParameters extends ActionParametersBase {

    private static final long serialVersionUID = 7238781812367042839L;

    @Valid
    private VnicProfile vnicProfile;
    private boolean publicUse;

    public VnicProfileParameters() {
    }

    public VnicProfileParameters(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public VnicProfile getVnicProfile() {
        return vnicProfile;
    }

    public void setVnicProfile(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }
}
