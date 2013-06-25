package org.ovirt.engine.ui.common.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Base class for common application views (excluding popups).
 * <p>
 * Holds the reference to the actual UI widget. Concrete views should call {@link ViewImpl#initWidget initWidget} within
 * their constructors. This is somewhat similar to using {@link com.google.gwt.user.client.ui.Composite Composite}
 * widget as the base class for your views.
 */
public abstract class AbstractView extends ViewImpl {

    /**
     * Sets a content widget into the given panel.
     */
    protected void setPanelContent(Panel panel, IsWidget content) {
        panel.clear();

        if (content != null) {
            panel.add(content);
        }
    }

}
