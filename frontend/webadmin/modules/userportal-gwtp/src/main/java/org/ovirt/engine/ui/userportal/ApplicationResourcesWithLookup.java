package org.ovirt.engine.ui.userportal;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;

public interface ApplicationResourcesWithLookup extends ClientBundleWithLookup {

    @Source("images/vmtypes/desktop_vm_icon.png")
    ImageResource desktopVmIcon();

    @Source("images/vmtypes/pool_icon.png")
    ImageResource poolVmIcon();

    @Source("images/vmtypes/server_vm_icon.png")
    ImageResource serverVmIcon();

}
