package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;

public class NetworkFilterRenderer extends NameRenderer<NetworkFilter> {
    private CommonApplicationConstants constants;

    public NetworkFilterRenderer(CommonApplicationConstants constants) {
        this.constants = constants;
    }

    @Override
    protected String renderNullSafe(NetworkFilter object) {
        String filterName = super.renderNullSafe(object);
        return filterName == null ? constants.vnicProfileNoFilter() : filterName;
    }
}
