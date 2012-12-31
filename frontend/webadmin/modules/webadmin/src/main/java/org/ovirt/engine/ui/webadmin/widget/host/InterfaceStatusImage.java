package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class InterfaceStatusImage extends Image {

    private static ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();
    public InterfaceStatusImage(InterfaceStatus status) {
        super();
        setResource(getResource(status));
    }

    public static ImageResource getResource(InterfaceStatus status){
        switch (status) {
        case Up:
            return resources.upImage();
        case None:
        case Down:
        default:
            return resources.downImage();
        }
    }

}
