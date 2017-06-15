package org.ovirt.engine.ui.webadmin.widget.host;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class InterfaceStatusImage extends Image {

    private static final ApplicationResources resources = AssetProvider.getResources();

    public InterfaceStatusImage(InterfaceStatus status) {
        super();
        setResource(getResource(status));
    }

    public static ImageResource getResource(InterfaceStatus status){
        switch (status) {
        case UP:
            return resources.upImage();
        case NONE:
        case DOWN:
        default:
            return resources.downImage();
        }
    }

}
