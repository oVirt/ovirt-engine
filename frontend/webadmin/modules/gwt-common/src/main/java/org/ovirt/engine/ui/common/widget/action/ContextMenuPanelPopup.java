package org.ovirt.engine.ui.common.widget.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public class ContextMenuPanelPopup extends MenuPanelPopup {

    public interface Resources extends ClientBundle {

        @Source("org/ovirt/engine/ui/common/css/ContextMenuPanelPopup.css")
        Style style();

    }

    private static final Resources resources = GWT.create(Resources.class);

    public ContextMenuPanelPopup(boolean autoHide) {
        super(autoHide);
    }

    protected void ensureStyleInjected() {
        resources.style().ensureInjected();
    }

    protected String getMenuBarStyle() {
        return resources.style().actionPanelPopupMenuBar();
    }

    protected String getPopupPanelStyle() {
        return resources.style().actionPanelPopupPanel();
    }
}
