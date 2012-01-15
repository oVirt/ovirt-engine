package org.ovirt.engine.ui.webadmin.view;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Base class for common application views (excluding popups).
 * <p>
 * Holds the reference to the actual UI widget. Concrete views should call {@link #initWidget(Widget)} within their
 * constructors. This is somewhat similar to using the {@link com.google.gwt.user.client.ui.Composite} widget as the base class for your views.
 */
public abstract class AbstractView extends ViewImpl {

    private Widget widget;

    protected void initWidget(Widget widget) {
        this.widget = widget;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    /**
     * Sets a content widget into the given panel.
     */
    protected void setPanelContent(Panel panel, Widget content) {
        panel.clear();

        if (content != null)
            panel.add(content);
    }

}
