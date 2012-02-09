package org.ovirt.engine.ui.common.widget.action;

import org.ovirt.engine.ui.common.widget.TitleMenuItemSeparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class MenuPanelPopup extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, MenuPanelPopup> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    PopupPanel panel;

    @UiField(provided = true)
    MenuBar menu;


    public MenuPanelPopup(boolean autoHide) {
        panel = new PopupPanel(autoHide);
        menu = new MenuBar(true) {
            @Override
            public MenuItem addItem(MenuItem item) {
                item.setStylePrimaryName("menuItem");
                return super.addItem(item);
            }

            @Override
            public MenuItemSeparator addSeparator(MenuItemSeparator separator) {
                if (separator instanceof TitleMenuItemSeparator) {
                    separator.setStylePrimaryName("menuItem");
                }
                return super.addSeparator(separator);
            }
        };
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        panel.setWidget(menu);
    }

    public PopupPanel asPopupPanel() {
        return panel;
    }

    public MenuBar getMenuBar() {
        return menu;
    }
}
