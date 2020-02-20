package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;

public class WebAdminMenuBarButtonDefinition<E, T> extends UiMenuBarButtonDefinition<E, T> {

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<E, T>> subActions) {
        super(getEventBus(), title, subActions);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
