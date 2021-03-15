package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.ui.common.CommonApplicationConstants;

public class FailoverVnicProfileRenderer extends NameRenderer<VnicProfile> {
    private CommonApplicationConstants constants;

    public FailoverVnicProfileRenderer(CommonApplicationConstants constants) {
        this.constants = constants;
    }

    @Override
    protected String renderNullSafe(VnicProfile object) {
        String vnicProfileName = super.renderNullSafe(object);
        return vnicProfileName == null ? constants.noneFailover() : vnicProfileName;
    }
}
