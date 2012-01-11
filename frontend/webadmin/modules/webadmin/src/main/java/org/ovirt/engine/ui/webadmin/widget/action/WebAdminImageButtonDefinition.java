package org.ovirt.engine.ui.webadmin.widget.action;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;

public abstract class WebAdminImageButtonDefinition<T> extends ImageUiCommandButtonDefinition<T> {

    public WebAdminImageButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage) {
        super(title, enabledImage, disabledImage);
    }

    public WebAdminImageButtonDefinition(String title, ImageResource enabledImage, ImageResource disabledImage,
            boolean showTitle) {
        super(title, enabledImage, disabledImage, showTitle);
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
