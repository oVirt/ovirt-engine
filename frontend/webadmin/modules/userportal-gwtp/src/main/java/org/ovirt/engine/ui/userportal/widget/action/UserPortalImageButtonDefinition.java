package org.ovirt.engine.ui.userportal.widget.action;

import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.shared.EventBus;

public abstract class UserPortalImageButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    public UserPortalImageButtonDefinition(String title, HasCssName icon) {
        super(getEventBus(), title, icon);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
