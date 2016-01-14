package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.system.HeaderOffsetChangeEvent;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.common.widget.TitleMenuItemSeparator;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RequiresResize;
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
        ProvidesElementId, RequiresResize {
    /**
     * The cascading menu/panel CSS resources.
     */
    public interface CascadeActionPanelCss extends CssResource {
        String cascadeButton();
        String actionPanel();
    }

    /**
     * Resources for the ActionPanel.
     */
    public interface ActionPanelResources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/CascadeActionPanel.css")
        CascadeActionPanelCss actionPanelCss();
        @Source("org/ovirt/engine/ui/common/images/cascade_button.png")
        ImageResource cascadeButtonArrow();
    }

    private static final ActionPanelResources RESOURCES = GWT.create(ActionPanelResources.class);

    private static final String GWT_PREFIX = "gwt-"; //$NON-NLS-1$
    private static final String MIN_WIDTH = "minWidth"; //$NON-NLS-1$
    private static final String MAX_WIDTH = "maxWidth"; //$NON-NLS-1$
    /**
     * The padding to the left of each action button bar, if you change this value also change the value of the
     * left-padding in the actionPadding class in SimpleActionTable.ui.xml.
     */
    private static final int ACTION_PANEL_PADDING_LEFT = 5; //There seems to be no good way to determine left padding
                                                            //No getElement().getStyle().getPaddingLeft() doesn't work.

    private final CascadeActionPanelCss style;

    @UiField
    public FlowPanel actionPanel;

    private final FlowPanel contentPanel;

    // List of action buttons that show in the tool-bar and context menu
    private final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<>();
    // List of buttons that only show in the tool-bar.
    private final List<ActionButtonDefinition<T>> toolbarOnlyActionButtonList = new ArrayList<>();
    // List of original visibility state for each button
    private final Map<Widget, Boolean> originallyVisible = new HashMap<>();

    private final SearchableModelProvider<T, ?> dataProvider;
    private final EventBus eventBus;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    private final MenuPanelPopup actionPanelPopupPanel;

    /**
     * The popup panel containing the {@link MenuBar} with the cascaded action buttons.
     */
    private final PopupPanel cascadePopupPanel;
    /**
     * The button used to open up the menu containing the cascaded action buttons.
     */
    private final PushButton cascadeButton;
    /**
     * The menu containing the cascaded action buttons.
     */
    private final MenuBar cascadeMenu;

    private String elementId = DOM.createUniqueId();

    /**
     * Handler registration for the resize handler.
     */
    private HandlerRegistration resizeHandlerRegistration;

    /**
     * Minimum width needed to display all the {@code ActionButton}s.
     */
    private int widgetMinWidth;
    /**
     * The width of any {@code Widget}s that are siblings of this {@code AbstractActionPanel} in the DOM tree.
     */
    private int siblingWidth;

    /**
     * Constructor.
     * @param dataProvider The data provider.
     * @param eventBus The GWT event bus.
     */
    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider, EventBus eventBus) {
        this.dataProvider = dataProvider;
        this.eventBus = eventBus;
        contextPopupPanel = new PopupPanel(true);
        contextMenuBar = new MenuBar(true);
        actionPanelPopupPanel = new MenuPanelPopup(true);
        //Cascading items.
        contentPanel = new FlowPanel();
        style = RESOURCES.actionPanelCss();
        style.ensureInjected();
        cascadePopupPanel = new PopupPanel(true);
        cascadeMenu = new MenuBar(true);
        cascadeButton = new PushButton(new Image(RESOURCES.cascadeButtonArrow()), getCascadeButtonClickHandler());
        configureCascadeMenu();
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
        cascadePopupPanel.setWidget(cascadeMenu);
        contentPanel.add(cascadeButton);
        actionPanel.add(contentPanel);
        actionPanel.addStyleName(style.actionPanel());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // Defer size calculations until sizes are available.
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

            @Override
            public void execute() {
                int minWidth = calculateWidgetMinWidthNeeded();
                contentPanel.getElement().getStyle().setProperty(MIN_WIDTH, minWidth, Unit.PX);
                if (widgetMinWidth > 0) {
                    siblingWidth = calculateSiblingWidth();
                }
                initializeCascadeMenuPanel();
            }
        });
        resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                initializeCascadeMenuPanel();
            }
        });
        eventBus.addHandler(HeaderOffsetChangeEvent.getType(),
                new HeaderOffsetChangeEvent.HeaderOffsetChangeHandler() {

            @Override
            public void onHeaderOffsetChange(HeaderOffsetChangeEvent event) {
                initializeCascadeMenuPanel();
                //Unregister the resize handler, we don't need it because resizes trigger the
                //HeaderOffsetChangeEvents.
                unregisterResizeHandler();
            }
        });
    }

    @Override
    public void onResize() {
        initializeCascadeMenuPanel();
    }

    /**
     * Initialize the cascade menu panel.
     */
    private void initializeCascadeMenuPanel() {
        if (widgetMinWidth > 0) {
            cascadePopupPanel.hide();
            int currentWidth = actionPanel.getParent().getOffsetWidth() - siblingWidth - ACTION_PANEL_PADDING_LEFT;
            actionPanel.getElement().getStyle().setProperty(MAX_WIDTH, currentWidth - 1, Unit.PX);
            if (currentWidth <= widgetMinWidth) {
                cascadeButton.setVisible(true);
            } else {
                cascadeButton.setVisible(false);
            }
            toggleVisibleWidgets(currentWidth - cascadeButton.getOffsetWidth());
        }
    }

    /**
     * Toggles the visible {@code ActionButton}s on the action panel based on the current width of the panel.
     * This method enumerates the buttons and totals the width of each button until we reach the width passed in.
     * Any buttons that would pass the width passed in are hidden, the other buttons are visible.
     *
     * @param currentWidth The width to check against.
     */
    private void toggleVisibleWidgets(int currentWidth) {
        int widgetWidth = 0;
        boolean foundEdge = false;
        if (contentPanel.getWidgetCount() > 1) {
            for (int i = 0; i < contentPanel.getWidgetCount() - 1; i++) {
                Widget widget = contentPanel.getWidget(i);
                if (originallyVisible.get(widget)) {
                    widget.setVisible(true); //temporarily show the widget, so we get the actual width of the widget.
                    if (foundEdge || (widgetWidth + widget.getOffsetWidth() > currentWidth)) {
                        widget.setVisible(false);
                        toolbarOnlyActionButtonList.get(i).setCascaded(true);
                        foundEdge = true;
                    } else {
                        toolbarOnlyActionButtonList.get(i).setCascaded(false);
                        widgetWidth += widget.getOffsetWidth();
                    }
                }
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unregisterResizeHandler();
    }

    private void unregisterResizeHandler() {
        if (resizeHandlerRegistration != null) {
            resizeHandlerRegistration.removeHandler();
        }
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getElementId() {
        return elementId;
    }


    /**
     * Adds a new button to the action panel.
     * @param buttonDef The button definition.
     */
    @Override
    public void addActionButton(final ActionButtonDefinition<T> buttonDef) {
        addActionButton(buttonDef, createNewActionButton(buttonDef));
    }

    /**
     * Adds a new button to the action panel.
     */
    public void addActionButton(final ActionButtonDefinition<T> buttonDef, final ActionButton newActionButton) {
        // Configure the button according to its definition
        newActionButton.setEnabledHtml(buttonDef.getEnabledHtml());
        newActionButton.setDisabledHtml(buttonDef.getDisabledHtml());

        // Set button element ID for better accessibility
        String buttonId = buttonDef.getUniqueId();
        if (buttonId != null) {
            newActionButton.asWidget().getElement().setId(
                    ElementIdUtils.createElementId(elementId, buttonId));
        }

        // Add the button to the action panel
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromToolBar)) {
            copyStyleToCascadeButton(newActionButton);
            contentPanel.insert(newActionButton.asWidget(), contentPanel.getWidgetCount() - 1);
            toolbarOnlyActionButtonList.add(buttonDef);
        }

        // Add the button to the context menu
        if (buttonDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || buttonDef.getCommandLocation().equals(CommandLocation.OnlyFromContext)) {
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

    /**
     * Calculate the width of all the sibling widgets to this widget (this is for the case where there are extra
     * buttons and other things on the same row).<br />
     * <br />
     * <b>NOTE</b> This calculation breaks down if the siblings have left or/and right margins. The reported width
     * is inaccurate if margins exist.
     * @return The total width of all the sibling widgets in pixels.
     */
    private int calculateSiblingWidth() {
        int width = 0;
        Widget parent = actionPanel.getParent();
        if (parent instanceof HasWidgets) {
            Iterator<Widget> widgetIterator = ((HasWidgets) parent).iterator();
            while (widgetIterator.hasNext()) {
                Widget widget = widgetIterator.next();
                if (widget != actionPanel) {
                    width += widget.getOffsetWidth();
                }
            }
        }
        return width;
    }

    /**
     * Calculate the minimum width needed to display all the {@code ActionButtons} in the action panel. This width
     * is needed to determine when to show the button for the cascading menu.
     * @return The minimum width needed in pixels.
     */
    private int calculateWidgetMinWidthNeeded() {
        int minWidth = 0;
        if (contentPanel.getWidgetCount() > 1) {
            for (int i = 0; i < contentPanel.getWidgetCount() - 1; i++) {
                Widget widget = contentPanel.getWidget(i);
                boolean widgetVisible = widget.isVisible();
                widget.setVisible(true);
                minWidth += widget.getElement().getOffsetWidth();
                widget.setVisible(widgetVisible);
            }
        }
        // Store this in a variable so we don't have to calculate it all the time.
        // This assumes that when resizes/etc happen this gets called to recalculate everything.
        widgetMinWidth = minWidth;
        return minWidth;
    }

    /**
     * This method copies the appropriate styles from the {@code ActionButton} to the new menu items for
     * the cascading menu.
     * @param newActionButton The {@code ActionButton} to copy the style from.
     */
    private void copyStyleToCascadeButton(ActionButton newActionButton) {
        String styleString = ((Widget) newActionButton).getStyleName().trim();
        if (styleString != null && !styleString.isEmpty()) {
            String[] stylesArray = styleString.split("\\s+"); //$NON-NLS-1$
            for (String singleStyle : stylesArray) {
                if (!singleStyle.startsWith(GWT_PREFIX)) {
                    cascadeButton.addStyleName(singleStyle);
                }
            }
        }
    }

    /**
     * Get the cascade drop down button click handler.
     * @return The {@code ClickHandler}
     */
    private ClickHandler getCascadeButtonClickHandler() {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!cascadePopupPanel.isShowing()) {
                    List<ActionButtonDefinition<T>> cascadeActionButtonList = new ArrayList<>();
                    for (int i = 0; i < contentPanel.getWidgetCount() - 1; i++) {
                        if (!contentPanel.getWidget(i).isVisible()) {
                            cascadeActionButtonList.add(toolbarOnlyActionButtonList.get(i));
                        }
                    }
                    updateContextMenu(cascadeMenu, cascadeActionButtonList, cascadePopupPanel);
                    cascadePopupPanel.showRelativeToAndFitToScreen(cascadeButton);
                } else {
                    cascadePopupPanel.hide();
                }
            }
        };
    }

    /**
     * Configure the options of the cascade menu and button.
     */
    private void configureCascadeMenu() {
        cascadeButton.addStyleName(style.cascadeButton());
        cascadeButton.setVisible(false); //Initially hide the button.
        cascadePopupPanel.setAutoHideEnabled(true);
        cascadePopupPanel.setModal(false);
        cascadePopupPanel.setGlassEnabled(false);
        cascadePopupPanel.addAutoHidePartner(cascadeButton.getElement());
    }

    void registerSelectionChangeHandler(final ActionButtonDefinition<T> buttonDef) {
        // Update button definition whenever list model item selection changes
        final IEventListener<EventArgs> itemSelectionChangeHandler = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                // Update action button on item selection change
                buttonDef.update();
            }
        };

        addSelectionChangeListener(itemSelectionChangeHandler);
    }

    void addSelectionChangeListener(IEventListener<EventArgs> itemSelectionChangeHandler) {
        dataProvider.getModel().getSelectedItemChangedEvent().addListener(itemSelectionChangeHandler);
        dataProvider.getModel().getSelectedItemsChangedEvent().addListener(itemSelectionChangeHandler);
    }

    /**
     * Adds a context menu handler to the given widget.
     * @param widget The widget.
     */
    public void addContextMenuHandler(Widget widget) {
        widget.addDomHandler(new ContextMenuHandler() {
            @Override
            public void onContextMenu(ContextMenuEvent event) {
                AbstractActionPanel.this.onContextMenu(event);
            }
        }, ContextMenuEvent.getType());
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
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // Avoid showing empty context menu
                if (hasActionButtons()) {
                    updateContextMenu(contextMenuBar, actionButtonList, contextPopupPanel);
                    contextPopupPanel.showAndFitToScreen(eventX, eventY);
                }
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
                MenuItem item = new MenuItem(buttonDef.getText(), new Command() {
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
            TooltipMixin.addTooltipToElement(buttonDef.getMenuItemTooltip(), item.getElement());
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
