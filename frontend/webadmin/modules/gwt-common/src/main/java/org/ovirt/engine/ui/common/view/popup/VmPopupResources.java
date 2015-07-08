package org.ovirt.engine.ui.common.view.popup;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VmPopupResources extends ClientBundle {
    @Source({"org/ovirt/engine/ui/common/css/VmPopupStyle.css"})
    VmPopupStyle createStyle();
}

interface VmPopupStyle extends CssResource {
    String showAdvancedOptionsButton();
}
