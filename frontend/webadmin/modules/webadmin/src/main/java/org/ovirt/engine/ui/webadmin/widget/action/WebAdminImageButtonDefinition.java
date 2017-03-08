package org.ovirt.engine.ui.webadmin.widget.action;

import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.shared.EventBus;

public abstract class WebAdminImageButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    public WebAdminImageButtonDefinition(String title, HasCssName icon) {
        super(getEventBus(), title, icon);
    }

    public WebAdminImageButtonDefinition(String title, HasCssName icon,
            boolean showTitle) {
        super(getEventBus(), title, icon, showTitle, false);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
