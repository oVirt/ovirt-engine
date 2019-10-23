package org.ovirt.engine.ui.webadmin.widget.action;

import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;

public abstract class WebAdminButtonDefinition<E, T> extends UiCommandButtonDefinition<E, T> {

    public WebAdminButtonDefinition(String title, boolean subTitledAction) {
        super(getEventBus(), title, subTitledAction);
    }

    public WebAdminButtonDefinition(String title) {
        super(getEventBus(), title);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
