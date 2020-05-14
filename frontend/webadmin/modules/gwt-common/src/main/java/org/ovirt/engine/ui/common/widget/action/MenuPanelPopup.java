package org.ovirt.engine.ui.common.widget.action;

import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.common.widget.TitleMenuItemSeparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.SimplePanel;

public class MenuPanelPopup extends Composite {

    public interface Resources extends ClientBundle {

        @Source("org/ovirt/engine/ui/common/css/MenuPanelPopup.css")
        Style style();

    }

    public interface Style extends CssResource {

        String actionPanelPopupPanel();

        String actionPanelPopupMenuBar();

    }

    private static final Resources resources = GWT.create(Resources.class);

    private final PopupPanel panel;
    private final MenuBar menu;

    public MenuPanelPopup(boolean autoHide) {
        panel = new PopupPanel(autoHide);
        menu = new MenuBar(true) {
            @Override
            public MenuItem addItem(MenuItem item) {
                item.setStylePrimaryName("menuItem"); //$NON-NLS-1$
                return super.addItem(item);
            }

            @Override
            public MenuItemSeparator addSeparator(MenuItemSeparator separator) {
                if (separator instanceof TitleMenuItemSeparator) {
                    separator.setStylePrimaryName("menuItem"); //$NON-NLS-1$
                }
                return super.addSeparator(separator);
            }
        };
        panel.setWidget(menu);
        initWidget(new SimplePanel(panel));

        ensureStyleInjected();
        panel.setStylePrimaryName(getPopupPanelStyle());
        menu.setStylePrimaryName(getMenuBarStyle());

        NodeList<Element> table = menu.getElement().getElementsByTagName("table"); //$NON-NLS-1$
        table.getItem(0).getStyle().setProperty("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
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

    public PopupPanel asPopupPanel() {
        return panel;
    }

    public MenuBar getMenuBar() {
        return menu;
    }

}
