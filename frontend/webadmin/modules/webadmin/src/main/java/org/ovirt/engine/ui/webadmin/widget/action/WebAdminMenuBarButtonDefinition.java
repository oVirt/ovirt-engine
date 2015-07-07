package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.action.UiMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

public class WebAdminMenuBarButtonDefinition<T> extends UiMenuBarButtonDefinition<T> {

    private static final Resources resources = GWT.create(Resources.class);

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions, boolean asTitle) {
        super(getEventBus(), title, subActions, asTitle, resources);
    }

    public WebAdminMenuBarButtonDefinition(String title,
            List<ActionButtonDefinition<T>> subActions,
            CommandLocation commandLocation) {
        super(getEventBus(), title, subActions, false, commandLocation, false, resources);
    }

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions) {
        super(getEventBus(), title, subActions, resources);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
