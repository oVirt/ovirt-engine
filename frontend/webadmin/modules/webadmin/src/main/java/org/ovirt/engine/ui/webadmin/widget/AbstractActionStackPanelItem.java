package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.webadmin.widget.table.ActionButtonDefinition;

import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractActionStackPanelItem<T> extends Composite {

    @UiField(provided = true)
    public SimpleActionPanel<T> actionPanel;

    @UiField(provided = true)
    public Widget dataDisplayWidget;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    public AbstractActionStackPanelItem(Widget dataDisplayWidget, SimpleActionPanel<T> actionPanel) {
        this.dataDisplayWidget = dataDisplayWidget;
        this.actionPanel = actionPanel;

        this.contextPopupPanel = new PopupPanel(true);
        this.contextMenuBar = new MenuBar(true);
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        initTable();
    }

    /**
     * Initialize the table widget and attach it to the corresponding panel.
     */
    void initTable() {
        // Add context menu handler
        dataDisplayWidget.addDomHandler(new ContextMenuHandler() {
            @Override
            public void onContextMenu(ContextMenuEvent event) {
                event.preventDefault();
                event.stopPropagation();

                // Show context menu only when not empty
                if (hasActionButtons()) {
                    int eventX = event.getNativeEvent().getClientX();
                    int eventY = event.getNativeEvent().getClientY();

                    updateContextMenu();
                    contextPopupPanel.setPopupPosition(eventX, eventY);
                    contextPopupPanel.show();
                }
            }
        }, ContextMenuEvent.getType());
        contextPopupPanel.setWidget(contextMenuBar);
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     */
    void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        item.setVisible(buttonDef.isAccessible());
        item.setEnabled(buttonDef.isEnabled(actionPanel.getSelectedList()));
    }

    /**
     * Rebuilds context menu items to match the action button list.
     */
    void updateContextMenu() {
        contextMenuBar.clearItems();

        for (final ActionButtonDefinition<T> buttonDef : actionPanel.getActionButtonList()) {
            MenuItem item = new MenuItem(buttonDef.getTitle(), new Command() {
                @Override
                public void execute() {
                    contextPopupPanel.hide();
                    buttonDef.onClick(actionPanel.getSelectedList());
                }
            });

            updateMenuItem(item, buttonDef);
            contextMenuBar.addItem(item);
        }
    }

    boolean hasActionButtons() {
        return actionPanel.hasActionButtons();
    }
}
