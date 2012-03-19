package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.FeatureNotImplementedYetPopup;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.common.widget.TitleMenuItemSeparator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
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
    private final EventBus eventBus;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    private final MenuPanelPopup actionPanelPopupPanel;

    private String elementId = DOM.createUniqueId();

    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider, EventBus eventBus) {
        this.dataProvider = dataProvider;
        this.eventBus = eventBus;
        this.contextPopupPanel = new PopupPanel(true);
        this.contextMenuBar = new MenuBar(true);
        this.actionPanelPopupPanel = new MenuPanelPopup(true);
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
        newActionButton.setTitle(buttonDef.getCustomToolTip() != null ? buttonDef.getCustomToolTip()
                : buttonDef.getTitle());

        // Set button element ID for better accessibility
        newActionButton.asWidget().getElement().setId(
                ElementIdUtils.createElementId(elementId, buttonDef.getUniqueId()));

        // Add the button to the action panel
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromToolBar)) {
            actionPanel.add(newActionButton.asWidget());
        }

        // Add the button to the context menu
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromFromContext)) {
            actionButtonList.add(buttonDef);
        }

        actionPanelPopupPanel.asPopupPanel()
                .addCloseHandler(new CloseHandler<com.google.gwt.user.client.ui.PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<com.google.gwt.user.client.ui.PopupPanel> event) {
                        newActionButton.asToggleButton().setDown(false);
                    }
                });

        // Add button widget click handler
        newActionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (buttonDef.isImplemented()) {
                    if (buttonDef instanceof UiMenuBarButtonDefinition) {
                        actionPanelPopupPanel.asPopupPanel().addAutoHidePartner(newActionButton.asToggleButton()
                                .getElement());
                        if (newActionButton.asToggleButton().isDown()) {
                            updateContextMenu(actionPanelPopupPanel.getMenuBar(),
                                    ((UiMenuBarButtonDefinition<T>) buttonDef).getSubActions(),
                                    actionPanelPopupPanel.asPopupPanel());
                            actionPanelPopupPanel.asPopupPanel()
                                    .showRelativeToAndFitToScreen(newActionButton.asWidget());
                        } else {
                            actionPanelPopupPanel.asPopupPanel().hide();
                        }
                    } else {
                        buttonDef.onClick(getSelectedItems());
                    }
                } else {
                    new FeatureNotImplementedYetPopup((Widget) event.getSource(),
                            buttonDef.isImplInUserPortal()).show();
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

    void registerSelectionChangeHandler(final ActionButtonDefinition<T> buttonDef) {
        // Update button definition whenever list model item selection changes
        final IEventListener itemSelectionChangeHandler = new IEventListener() {
            @Override
            public void eventRaised(org.ovirt.engine.core.compat.Event ev, Object sender, EventArgs args) {
                // Update action button on item selection change
                buttonDef.update();
            }
        };

        addSelectionChangeListener(itemSelectionChangeHandler);

        // Add handler to be notified when UiCommon models are (re)initialized
        eventBus.addHandler(UiCommonInitEvent.getType(), new UiCommonInitHandler() {
            @Override
            public void onUiCommonInit(UiCommonInitEvent event) {
                addSelectionChangeListener(itemSelectionChangeHandler);
            }
        });
    }

    void addSelectionChangeListener(IEventListener itemSelectionChangeHandler) {
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

                    updateContextMenu(contextMenuBar, actionButtonList, contextPopupPanel);
                    contextPopupPanel.showAndFitToScreen(eventX, eventY);
                }
            }
        }, ContextMenuEvent.getType());
    }

    MenuBar updateContextMenu(MenuBar menuBar, List<ActionButtonDefinition<T>> actions, final PopupPanel popupPanel) {
        return updateContextMenu(menuBar, actions, popupPanel, true);
    }

    /**
     * Rebuilds context menu items to match the action button list.
     */
    MenuBar updateContextMenu(MenuBar menuBar,
            List<ActionButtonDefinition<T>> actions,
            final PopupPanel popupPanel,
            boolean removeOldItems) {

        if (removeOldItems) {
            menuBar.clearItems();
        }

        for (final ActionButtonDefinition<T> buttonDef : actions) {

            if (buttonDef instanceof UiMenuBarButtonDefinition) {
                UiMenuBarButtonDefinition<T> menuBarDef = ((UiMenuBarButtonDefinition<T>) buttonDef);
                if (menuBarDef.isAsTitle()) {
                    MenuItemSeparator titleItem = new TitleMenuItemSeparator(buttonDef.getTitle());
                    menuBar.addSeparator(titleItem);
                    titleItem.setVisible(buttonDef.isVisible(getSelectedItems()));
                    updateContextMenu(menuBar, menuBarDef.getSubActions(), popupPanel, false);
                } else {
                    MenuItem newMenu = new MenuItem(buttonDef.getTitle(),
                            updateContextMenu(new MenuBar(true),
                                    menuBarDef.getSubActions(),
                                    popupPanel));

                    updateMenuItem(newMenu, buttonDef);
                    menuBar.addItem(newMenu);
                }
            } else {
                MenuItem item = new MenuItem(buttonDef.getTitle(), new Command() {
                    @Override
                    public void execute() {
                        popupPanel.hide();
                        buttonDef.onClick(getSelectedItems());
                    }
                });

                updateMenuItem(item, buttonDef);
                menuBar.addItem(item);
            }
        }
        return menuBar;
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    void updateActionButton(ActionButton button, ActionButtonDefinition<T> buttonDef) {
        button.asWidget().setVisible(buttonDef.isAccessible() && buttonDef.isVisible(getSelectedItems()));
        button.setEnabled(buttonDef.isEnabled(getSelectedItems()));
        button.setTitle(buttonDef.getCustomToolTip() != null ? buttonDef.getCustomToolTip() : buttonDef.getTitle());
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     */
    protected void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        item.setVisible(buttonDef.isAccessible() && buttonDef.isVisible(getSelectedItems()));
        item.setEnabled(buttonDef.isEnabled(getSelectedItems()));

        if (buttonDef.getToolTip() != null) {
            item.setTitle(buttonDef.getToolTip());
        }
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
