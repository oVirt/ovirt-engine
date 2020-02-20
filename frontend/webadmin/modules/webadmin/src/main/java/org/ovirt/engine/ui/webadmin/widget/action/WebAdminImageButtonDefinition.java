package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.shared.EventBus;

public abstract class WebAdminImageButtonDefinition<E, T> extends ImageUiCommandButtonDefinition<E, T> {

    public WebAdminImageButtonDefinition(String title, HasCssName icon) {
        super(getEventBus(), title, icon);
    }

    public WebAdminImageButtonDefinition(String title,
            HasCssName icon,
            boolean showTitle) {
        super(getEventBus(), title, icon, showTitle, false);
    }

    public WebAdminImageButtonDefinition(String title,
            HasCssName icon,
            List<ActionButtonDefinition<E, T>> subActions) {
        super(getEventBus(), title, icon, false, false, subActions);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
