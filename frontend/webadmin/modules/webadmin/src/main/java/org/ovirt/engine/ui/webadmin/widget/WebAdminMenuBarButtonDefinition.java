package org.ovirt.engine.ui.webadmin.widget;

import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;

public class WebAdminMenuBarButtonDefinition<T> extends UiMenuBarButtonDefinition<T> {

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions, boolean asTitle) {
        super(title, subActions, asTitle);
    }

    public WebAdminMenuBarButtonDefinition(String title, List<ActionButtonDefinition<T>> subActions) {
        super(title, subActions);
    }

    public WebAdminMenuBarButtonDefinition(String title,
            List<ActionButtonDefinition<T>> subActions,
            boolean subTitledAction,
            boolean availableOnlyFromContext) {
        super(title, subActions, availableOnlyFromContext, availableOnlyFromContext, false);
    }

    @Override
    protected EventBus getEventBus() {
        return ClientGinjectorProvider.instance().getEventBus();
    }

    @Override
    protected CommonApplicationTemplates getCommonApplicationTemplates() {
        return ClientGinjectorProvider.instance().getApplicationTemplates();
    }
}
