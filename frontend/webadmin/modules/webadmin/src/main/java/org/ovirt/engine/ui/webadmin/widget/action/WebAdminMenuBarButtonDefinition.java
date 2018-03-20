package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;

public class WebAdminMenuBarButtonDefinition<T> extends UiMenuBarButtonDefinition<T> {

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions, boolean asTitle) {
        super(getEventBus(), title, subActions, asTitle);
    }

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions) {
        super(getEventBus(), title, subActions);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
