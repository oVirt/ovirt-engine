package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.common.widget.TitleMenuItemSeparator;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.UIObject;
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
public abstract class AbstractActionPanel<T> extends Composite implements ActionPanel<T>, HasElementId,
        ProvidesElementId {

    // List of action buttons that show in the tool-bar and context menu
    private final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<>();
    // List of buttons that only show in the tool-bar.
    private final List<ActionButtonDefinition<T>> toolbarOnlyActionButtonList = new ArrayList<>();
    // List of original visibility state for each button
    private final Map<Widget, Boolean> originallyVisible = new HashMap<>();

    private final SearchableModelProvider<T, ?> dataProvider;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    private final MenuPanelPopup actionPanelPopupPanel;

    private String elementId = DOM.createUniqueId();

    /**
     * Constructor.
     * @param dataProvider The data provider.
     * @param eventBus The GWT event bus.
     */
    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        contextPopupPanel = new PopupPanel(true);
        contextMenuBar = new MenuBar(true);
        actionPanelPopupPanel = new MenuPanelPopup(true);
    }

    /**
     * Returns the model data provider.
     * @return The {@code SearchableModelProvider}.
     */
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

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public ActionButton addMenuListItem(final ActionButtonDefinition<T> buttonDef) {
        ActionAnchorListItem result = new ActionAnchorListItem(buttonDef.getText());
        // Set button element ID for better accessibility
        String buttonId = buttonDef.getUniqueId();
        if (buttonId != null) {
            result.asWidget().getElement().setId(
                    ElementIdUtils.createElementId(elementId, buttonId));
        }

        // Add the button to the context menu
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromContext)) {
            actionButtonList.add(buttonDef);
        }

        // Add button widget click handler
        result.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (buttonDef instanceof UiMenuBarButtonDefinition) {
                    actionPanelPopupPanel.asPopupPanel().addAutoHidePartner(((UIObject) result).getElement());
                } else {
                    buttonDef.onClick(getSelectedItems());
                }
            }
        });

        registerSelectionChangeHandler(buttonDef);

        // Update button whenever its definition gets re-initialized
        buttonDef.addInitializeHandler(new InitializeHandler() {
            @Override
            public void onInitialize(InitializeEvent event) {
                updateActionButton(result, buttonDef);
            }
        });

        updateActionButton(result, buttonDef);
        return result;
    }

    /**
     * Adds a new button to the action panel.
     * @param buttonDef The button definition.
     */
    public ActionButton addActionButton(final ActionButtonDefinition<T> buttonDef) {
        return addActionButton(buttonDef, createNewActionButton(buttonDef));
    }

    /**
     * Adds a new button to the action panel.
     */
    public ActionButton addActionButton(final ActionButtonDefinition<T> buttonDef, final ActionButton newActionButton) {
        newActionButton.setText(buttonDef.getText());
        // Set button element ID for better accessibility
        String buttonId = buttonDef.getUniqueId();
        if (buttonId != null) {
            newActionButton.asWidget().getElement().setId(
                    ElementIdUtils.createElementId(elementId, buttonId));
        }

        // Add the button to the action panel
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromToolBar)) {
            toolbarOnlyActionButtonList.add(buttonDef);
        }

        // Add the button to the context menu
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromContext)) {
            actionButtonList.add(buttonDef);
        }

        // Add button widget click handler
        newActionButton.addClickHandler(event -> {
                if (buttonDef instanceof UiMenuBarButtonDefinition) {
                    actionPanelPopupPanel.asPopupPanel().addAutoHidePartner(((UIObject) newActionButton).getElement());
                } else {
                    actionPanelPopupPanel.asPopupPanel().hide();
                }
                buttonDef.onClick(getSelectedItems());
        });

        registerSelectionChangeHandler(buttonDef);

        // Update button whenever its definition gets re-initialized
        buttonDef.addInitializeHandler(event -> updateActionButton(newActionButton, buttonDef));

        updateActionButton(newActionButton, buttonDef);
        return newActionButton;
    }

    void registerSelectionChangeHandler(final ActionButtonDefinition<T> buttonDef) {
        // Update button definition whenever list model item selection changes
        final IEventListener<EventArgs> itemSelectionChangeHandler = (ev, sender, args) -> {
            // Update action button on item selection change
            buttonDef.update();
        };

        addSelectionChangeListener(itemSelectionChangeHandler);
    }

    @SuppressWarnings("unchecked")
    void addSelectionChangeListener(IEventListener<EventArgs> itemSelectionChangeHandler) {
        dataProvider.getModel().getSelectedItemChangedEvent().addListener(itemSelectionChangeHandler);
        dataProvider.getModel().getSelectedItemsChangedEvent().addListener(itemSelectionChangeHandler);
    }

    /**
     * Adds a context menu handler to the given widget.
     * @param widget The widget.
     */
    public void addContextMenuHandler(Widget widget) {
        widget.addDomHandler(event -> AbstractActionPanel.this.onContextMenu(event), ContextMenuEvent.getType());
    }

    /**
     * Show the context menu.
     * @param event The {@code ContextMenuEvent}
     */
    protected void onContextMenu(ContextMenuEvent event) {
        final int eventX = event.getNativeEvent().getClientX();
        final int eventY = event.getNativeEvent().getClientY();

        // Suppress default browser context menu
        event.preventDefault();
        event.stopPropagation();

        // Use deferred command to ensure that the context menu
        // is shown only after other event handlers do their job
        Scheduler.get().scheduleDeferred(() -> {
            // Avoid showing empty context menu
            if (hasActionButtons()) {
                updateContextMenu(contextMenuBar, actionButtonList, contextPopupPanel);
                contextPopupPanel.showAndFitToScreen(eventX, eventY);
            }
        });
    }

    MenuBar updateContextMenu(MenuBar menuBar, List<ActionButtonDefinition<T>> actions, final PopupPanel popupPanel) {
        return updateContextMenu(menuBar, actions, popupPanel, true);
    }

    /**
     * Rebuilds context menu items to match the action button list.
     * @param menuBar The menu bar to populate.
     * @param actions A list of {@code ActionButtonDefinition}s used to populate the {@code MenuBar}.
     * @param popupPanel The pop-up panel containing the {@code MenuBar}.
     * @param removeOldItems A flag to indicate if we should remove old items.
     * @return A {@code MenuBar} containing all the action buttons as menu items.
     */
    MenuBar updateContextMenu(MenuBar menuBar,
            List<ActionButtonDefinition<T>> actions,
            final PopupPanel popupPanel,
            boolean removeOldItems) {
        if (removeOldItems) {
            ElementTooltipUtils.destroyMenuItemTooltips(menuBar);
            menuBar.clearItems();
        }

        for (final ActionButtonDefinition<T> buttonDef : actions) {
            if (buttonDef instanceof UiMenuBarButtonDefinition) {
                UiMenuBarButtonDefinition<T> menuBarDef = (UiMenuBarButtonDefinition<T>) buttonDef;
                if (menuBarDef.isAsTitle()) {
                    MenuItemSeparator titleItem = new TitleMenuItemSeparator(buttonDef.getText());
                    menuBar.addSeparator(titleItem);
                    titleItem.setVisible(buttonDef.isVisible(getSelectedItems()));
                    updateContextMenu(menuBar, menuBarDef.getSubActions(), popupPanel, false);
                } else {
                    MenuItem newMenu = new MenuItem(buttonDef.getText(),
                            updateContextMenu(new MenuBar(true),
                                    menuBarDef.getSubActions(),
                                    popupPanel));

                    updateMenuItem(newMenu, buttonDef);
                    menuBar.addItem(newMenu);
                }
            } else {
                MenuItem item = new MenuItem(buttonDef.getText(), () -> {
                    popupPanel.hide();
                    buttonDef.onClick(getSelectedItems());
                });

                updateMenuItem(item, buttonDef);
                menuBar.addItem(item);
            }
        }

        return menuBar;
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     * @param button The {@code ActionButton} to update.
     * @param buttonDef The {@code ActionButtonDefinition} used to determine the new state of the button.
     */
    void updateActionButton(ActionButton button, ActionButtonDefinition<T> buttonDef) {
        button.asWidget().setVisible(buttonDef.isAccessible(getSelectedItems())
                && buttonDef.isVisible(getSelectedItems()) && !buttonDef.isCascaded());
        button.setEnabled(buttonDef.isEnabled(getSelectedItems()));
        if (buttonDef.getTooltip() != null) {
            // this Panel is special. show the tooltips below the buttons because they're too
            // hard to read with the default TOP placement.
            button.setTooltip(buttonDef.getTooltip(), Placement.BOTTOM);
        }
        originallyVisible.put(button.asWidget(), buttonDef.isAccessible(getSelectedItems())
                && buttonDef.isVisible(getSelectedItems()));
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     * @param item The {@code MenuItem} to enabled/disable/hide based on the {@code ActionButtonDefinition}
     * @param buttonDef The button definition to use to change the menu item.
     */
    protected void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        item.setVisible(buttonDef.isAccessible(getSelectedItems()) && buttonDef.isVisible(getSelectedItems()));
        item.setEnabled(buttonDef.isEnabled(getSelectedItems()));

        if (buttonDef.getMenuItemTooltip() != null) {
            ElementTooltipUtils.setTooltipOnElement(item.getElement(), buttonDef.getMenuItemTooltip());
        }
    }

    /**
     * @return {@code true} if this action panel has at least one action button, {@code false} otherwise.
     */
    boolean hasActionButtons() {
        return !actionButtonList.isEmpty();
    }

    /**
     * Returns a new action button widget based on the given definition.
     * @param buttonDef The button definition to use to create the {@code ActionButton}
     * @return An {@code ActionButton} created from the passed in {@code ActionButtonDefinition}
     */
    protected abstract ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef);

}
