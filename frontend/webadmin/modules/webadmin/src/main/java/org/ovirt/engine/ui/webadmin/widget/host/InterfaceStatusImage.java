package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.user.client.ui.Image;

class InterfaceStatusImage extends Image {

    public InterfaceStatusImage(InterfaceStatus status, ApplicationResources resources) {
        super();

        switch (status) {
        case Up:
            setResource(resources.upImage());
            break;
        case None:
        case Down:
        default:
            setResource(resources.downImage());
            break;
        }
    }

}
