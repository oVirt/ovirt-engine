package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class used to implement action panel widgets.
 *
 * @param <T> Action panel item type.
 */
public abstract class AbstractActionPanel<T> extends Composite implements ActionPanel<T>, HasElementId,
        ProvidesElementId {

    // List of action buttons that show in the tool-bar and context menu
    private final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<>();
    // List of buttons that only show in the tool-bar.
    private final List<ActionButtonDefinition<T>> toolbarOnlyActionButtonList = new ArrayList<>();

    private final SearchableModelProvider<T, ?> dataProvider;

    @UiField
    public DropDown menuContainer = new DropDown();
    @UiField
    public Button clickButton = new Button();
    @UiField
    public DropDownMenu menu = new DropDownMenu();

    private String elementId = DOM.createUniqueId();

    /**
     * Constructor.
     * @param dataProvider The data provider.
     */
    public AbstractActionPanel(SearchableModelProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
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
        // Hide the button, we need it to set the attributes on so jquery can manipulate it.
        clickButton.setDataToggle(Toggle.DROPDOWN);
        clickButton.setVisible(false);
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
    public ActionButton addMenuListItem(final ActionButtonDefinition<T> menuItemDef) {
        ActionAnchorListItem result = new ActionAnchorListItem(menuItemDef.getText());
        // Set menu item ID for better accessibility
        String menuItemId = menuItemDef.getUniqueId();
        if (menuItemId != null) {
            result.asWidget().getElement().setId(
                    ElementIdUtils.createElementId(elementId, menuItemId));
        }

        // Add the menu item to the context menu
        if (menuItemDef.getCommandLocation().equals(CommandLocation.ContextAndToolBar)
                || menuItemDef.getCommandLocation().equals(CommandLocation.OnlyFromContext)) {
            actionButtonList.add(menuItemDef);
        }

        // Add menu item widget click handler
        result.addClickHandler(e -> {
            menuItemDef.onClick(getSelectedItems());
        });

        registerSelectionChangeHandler(menuItemDef);

        // Update menu item whenever its definition gets re-initialized
        menuItemDef.addInitializeHandler(e -> {
            updateActionButton(result, menuItemDef);
        });

        updateActionButton(result, menuItemDef);
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
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     * @param item The {@code MenuItem} to enabled/disable/hide based on the {@code ActionButtonDefinition}
     * @param buttonDef The button definition to use to change the menu item.
     */
    protected void updateMenuItem(AnchorListItem item, ActionButtonDefinition<T> buttonDef) {
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
