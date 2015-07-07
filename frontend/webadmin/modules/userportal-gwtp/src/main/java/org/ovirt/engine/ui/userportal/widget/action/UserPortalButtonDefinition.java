package org.ovirt.engine.ui.userportal.widget.action;

import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import com.google.gwt.event.shared.EventBus;

public abstract class UserPortalButtonDefinition<T> extends UiCommandButtonDefinition<T> {

    public UserPortalButtonDefinition(String title) {
        super(getEventBus(), title);
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.getEventBus();
    }

}
