package org.ovirt.engine.ui.userportal.widget.action;

import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;

public abstract class UserPortalImageButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    public UserPortalImageButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        super(getEventBus(), title, enabledImage, disabledImage);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
