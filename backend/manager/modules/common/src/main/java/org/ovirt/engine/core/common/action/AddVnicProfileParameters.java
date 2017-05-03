package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;

public class AddVnicProfileParameters extends VnicProfileParameters {

    private static final long serialVersionUID = 835297474010108808L;

    /**
     * boolean indicating whether to calculate the {@link org.ovirt.engine.core.common.businessentities.network.Network}
     * 's default value of {@link org.ovirt.engine.core.common.businessentities.network.Network} default
     * {@link org.ovirt.engine.core.common.businessentities.network.VnicProfile#networkFilterId} or to keep it given
     * value. {@code useDefaultNetworkFiterId} by default is false.
     */
    private boolean useDefaultNetworkFiterId = false;

    public AddVnicProfileParameters() {
    }

    public AddVnicProfileParameters(VnicProfile vnicProfile) {
        this(vnicProfile, false);
    }

    public AddVnicProfileParameters(VnicProfile vnicProfile, boolean useDefaultNetworkFiterId) {
        super(vnicProfile);
        this.useDefaultNetworkFiterId = useDefaultNetworkFiterId;
    }

    public boolean isUseDefaultNetworkFilterId() {
        return useDefaultNetworkFiterId;
    }
}
