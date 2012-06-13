package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Network;

import com.google.gwt.resources.client.ImageResource;

public class NetworkStatusColumn extends WebAdminImageResourceColumn<Network> {

    @Override
    public ImageResource getValue(Network nwk) {
        switch (nwk.getStatus()) {
        case Operational:
            return getApplicationResources().upImage();
        case NonOperational:
            return getApplicationResources().downImage();
        default:
            return getApplicationResources().downImage();
        }
    }

}
