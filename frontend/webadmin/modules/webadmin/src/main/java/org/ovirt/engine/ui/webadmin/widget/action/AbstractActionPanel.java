package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.idhandler.HasElementId;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent.CommonModelChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.webadmin.utils.ElementIdUtils;
import org.ovirt.engine.ui.webadmin.widget.FeatureNotImplementedYetPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class used to implement action panel widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #actionPanel} widget into which action button widgets will be rendered
 * </ul>
 * 
 * @param <T>
 *            Action panel item type.
 */
public abstract class AbstractActionPanel<T> extends Composite implements HasElementId {

    @UiField
    public FlowPanel actionPanel;

    // List of action buttons managed by this action panel
    private final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<ActionButtonDefinition<T>>();

    private final SearchableModelProvider<T, ?> dataProvider;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    private String elementId = DOM.createUniqueId();

    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        this.contextPopupPanel = new PopupPanel(true);
        this.contextMenuBar = new MenuBar(true);
    }

    protected SearchableModelProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        contextPopupPanel.setWidget(contextMenuBar);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    protected String getElementId() {
        return elementId;
    }

    /**
     * Adds a new button to the action panel.
     */
    public void addActionButton(final ActionButtonDefinition<T> buttonDef) {
        final ActionButton newActionButton = createNewActionButton(buttonDef);

        // Configure the button according to its definition
        newActionButton.setEnabledHtml(buttonDef.getEnabledHtml());
        newActionButton.setDisabledHtml(buttonDef.getDisabledHtml());
        newActionButton.setTitle(buttonDef.getTitle());

        // Set button element ID for better accessibility
        newActionButton.asWidget().getElement().setId(
                ElementIdUtils.createElementId(elementId, buttonDef.getTitle()));

        // Add the button to the action panel
        if (!buttonDef.isAvailableOnlyFromContext()) {
            actionPanel.add(newActionButton.asWidget());
        }

        actionButtonList.add(buttonDef);

        // Add button widget click handler
        newActionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (buttonDef.isImplemented()) {
                    buttonDef.onClick(getSelectedItems());
                }
                else {
                    FeatureNotImplementedYetPopup fniyp = new FeatureNotImplementedYetPopup(
                            (Widget) event.getSource(), buttonDef.isImplInUserPortal());
                    fniyp.show();
                }
            }
        });

        registerSelectionChangeHandler(buttonDef);

        // Update button whenever its definition gets re-initialized
        buttonDef.addInitializeHandler(new InitializeHandler() {
            @Override
            public void onInitialize(InitializeEvent event) {
                updateActionButton(newActionButton, buttonDef);
            }
        });

        updateActionButton(newActionButton, buttonDef);
    }

    private void registerSelectionChangeHandler(final ActionButtonDefinition<T> buttonDef) {
        // Update button definition whenever list model item selection changes
        final IEventListener itemSelectionChangeHandler = new IEventListener() {
            @Override
            public void eventRaised(org.ovirt.engine.core.compat.Event ev, Object sender, EventArgs args) {
                // Update action button on item selection change
                buttonDef.update();
            }
        };

        addSelectionChangeListener(itemSelectionChangeHandler);

        ClientGinjectorProvider.instance().getEventBus()
                .addHandler(CommonModelChangeEvent.getType(), new CommonModelChangeHandler() {
                    @Override
                    public void onCommonModelChange(CommonModelChangeEvent event) {
                        addSelectionChangeListener(itemSelectionChangeHandler);
                    }
                });
    }

    private void addSelectionChangeListener(final IEventListener itemSelectionChangeHandler) {
        dataProvider.getModel().getSelectedItemChangedEvent().addListener(itemSelectionChangeHandler);
        dataProvider.getModel().getSelectedItemsChangedEvent().addListener(itemSelectionChangeHandler);
    }

    /**
     * Adds a context menu handler to the given widget.
     */
    public void addContextMenuHandler(Widget widget) {
        widget.addDomHandler(new ContextMenuHandler() {
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
    }

    /**
     * Rebuilds context menu items to match the action button list.
     */
    void updateContextMenu() {
        contextMenuBar.clearItems();

        for (final ActionButtonDefinition<T> buttonDef : actionButtonList) {
            MenuItem item = new MenuItem(buttonDef.getTitle(), new Command() {
                @Override
                public void execute() {
                    contextPopupPanel.hide();
                    buttonDef.onClick(getSelectedItems());
                }
            });

            updateMenuItem(item, buttonDef);
            contextMenuBar.addItem(item);
        }
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    void updateActionButton(ActionButton button, ActionButtonDefinition<T> buttonDef) {
        button.asWidget().setVisible(buttonDef.isAccessible());
        button.setEnabled(buttonDef.isEnabled(getSelectedItems()));
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     */
    void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        item.setVisible(buttonDef.isAccessible());
        item.setEnabled(buttonDef.isEnabled(getSelectedItems()));
    }

    /**
     * Returns {@code true} if this action panel has at least one action button, {@code false} otherwise.
     */
    boolean hasActionButtons() {
        return !actionButtonList.isEmpty();
    }

    /**
     * Returns items currently selected in the action panel.
     */
    protected abstract List<T> getSelectedItems();

    /**
     * Returns a new action button widget based on the given definition.
     */
    protected abstract ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef);

}
