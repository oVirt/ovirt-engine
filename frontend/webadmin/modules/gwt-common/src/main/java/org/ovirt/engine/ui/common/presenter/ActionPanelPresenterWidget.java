package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.idhandler.ProvidesElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.ActionPanel;
import org.ovirt.engine.ui.common.widget.action.DropdownActionButton.SelectedItemsProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public abstract class ActionPanelPresenterWidget<E, T, M extends SearchableListModel> extends
    PresenterWidget<ActionPanelPresenterWidget.ViewDef<E, T>> implements ActionPanel<E, T>, SelectedItemsProvider<E, T> {

    public interface ViewDef<E, T> extends View, HasElementId, ProvidesElementId {
        /**
         * Add new menu list item to the Kebab menu.
         * @param menuItemDef The menu item definition
         * @return The 'action button' representing the menu item.
         */
        ActionButton addMenuListItem(ActionButtonDefinition<E, T> menuItemDef);
        /**
         * Add new button to action panel.
         * @param buttonDef The button definition.
         * @return The action button representing the buttons.
         */
        ActionButton addActionButton(ActionButtonDefinition<?, T> buttonDef);

        ActionButton addDropdownActionButton(ActionButtonDefinition<E, T> buttonDef,
                List<ActionButtonDefinition<E, T>> subActions, SelectedItemsProvider<E, T> selectedItemsProvider);

        ActionButton addDropdownComboActionButton(ActionButtonDefinition<E, T> buttonDef,
                List<ActionButtonDefinition<E, T>> subActions, SelectedItemsProvider<E, T> selectedItemsProvider);
        /**
         * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
         * @param isVisible should the menu item be visible.
         * @param isEnabled should the menu itemm be enabled.
         * @param menuItemDef The {@code ActionButtonDefinition}.
         */
        void updateMenuItem(boolean isVisible, boolean isEnabled, ActionButtonDefinition<?, T> menuItemDef);
        /**
         * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
         * @param isVisible should the button be visible.
         * @param isEnabled should the button be enabled.
         * @param buttonDef The {@code ActionButtonDefinition}.
         */
        void updateActionButton(boolean isVisible, boolean isEnabled, ActionButtonDefinition<?, T> buttonDef);

        /**
         * Add a dividing line to the kebab menu.
         */
        void addDividerToKebab();

        /**
         * Create the container to hold the search panel as necessary and insert the
         * provided search panel to be displayed.
         */
        void addSearchPanel(IsWidget searchPanel);

        /**
         * Add the toolbar's search result / applied filter row to the action panel below the search panel and buttons
         */
        void setFilterResult(IsWidget result);

        /**
         * Get a map of button definitions to action items.
         * @return A map of the action button definitions to the action items (this includes buttons and kebab menu).
         */
        Map<ActionButtonDefinition<?, T>, ActionButton> getActionItems();
    }

    private final SearchableTableModelProvider<T, M> dataProvider;
    private final List<ActionButtonDefinition<?, T>> actionButtonDefinitions = new ArrayList<>();

    public ActionPanelPresenterWidget(EventBus eventBus, ViewDef<E, T> view, SearchableTableModelProvider<T, M> dataProvider) {
        super(eventBus, view);
        this.dataProvider = dataProvider;
        initializeButtons();
    }

    @Override
    public void addMenuListItem(final ActionButtonDefinition<E, T> menuItemDef) {
        ActionButton newActionMenuListItem = getView().addMenuListItem(menuItemDef);
        registerSelectionChangeHandler(menuItemDef);
        // Add menu item widget click handler
        registerHandler(newActionMenuListItem.addClickHandler(e -> {
            ((ActionButtonDefinition<E, T>) menuItemDef).onClick(getParentEntity(), getSelectedItems());
        }));
        // Update menu item whenever its definition gets re-initialized
        registerHandler(menuItemDef.addInitializeHandler(e -> {
            updateMenuItem(menuItemDef);
        }));

        updateMenuItem(menuItemDef);
    }

    public void addDividerToKebab() {
        getView().addDividerToKebab();
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     * @param item The {@code MenuItem} to enabled/disable/hide based on the {@code ActionButtonDefinition}
     * @param buttonDef The button definition to use to change the menu item.
     */
    protected void updateMenuItem(ActionButtonDefinition<E, T> menuItemDef) {
        boolean isVisible = menuItemDef.isAccessible(getParentEntity(), getSelectedItems()) && menuItemDef.isVisible(getParentEntity(), getSelectedItems());
        boolean isEnabled = menuItemDef.isEnabled(getParentEntity(), getSelectedItems());
        getView().updateMenuItem(isVisible, isEnabled, menuItemDef);
    }

    /**
     * Adds a new button to the action panel.
     * @param buttonDef The button definition.
     */
    @Override
    public void addActionButton(ActionButtonDefinition<E, T> buttonDef) {
        ActionButton newButton = getView().addActionButton(buttonDef);
        if (buttonDef.getIndex() > actionButtonDefinitions.size()) {
            actionButtonDefinitions.add(buttonDef);
        } else {
            actionButtonDefinitions.add(buttonDef.getIndex(), buttonDef);
        }
        initButton((ActionButtonDefinition<E, T>) buttonDef, newButton);
    }

    private void initButton(ActionButtonDefinition<E, T> buttonDef, ActionButton newButton) {
        registerSelectionChangeHandler(buttonDef);
        // Add button widget click handler
        registerHandler(newButton.addClickHandler(event -> {
            buttonDef.onClick(getParentEntity(), getSelectedItems());
        }));
        // Update button whenever its definition gets re-initialized
        registerHandler(buttonDef.addInitializeHandler(event -> updateActionButton(buttonDef)));

        updateActionButton(buttonDef);
    }

    public void addComboActionButton(ActionButtonDefinition<E, T> buttonDef, List<ActionButtonDefinition<E, T>> subActions) {
        ActionButton newButton = getView().addDropdownComboActionButton(buttonDef, subActions, this);
        actionButtonDefinitions.add(buttonDef);
        initButton(buttonDef, newButton);
    }

    // Add an option to include a subaction in the right-click menu
    public void addComboActionButtonWithContexts(ActionButtonDefinition<E, T> buttonDef, List<ActionButtonDefinition<E, T>> subActions) {
        ActionButton newButton = getView().addDropdownComboActionButton(buttonDef, subActions, this);
        actionButtonDefinitions.add(buttonDef);
        actionButtonDefinitions.addAll(subActions);
        initButton(buttonDef, newButton);
    }

    public void addActionButton(ActionButtonDefinition<E, T> buttonDef, List<ActionButtonDefinition<E, T>> subActions) {
        ActionButton newButton = getView().addDropdownActionButton(buttonDef, subActions, this);
        initButton(buttonDef, newButton);
    }

    protected void updateActionButton(ActionButtonDefinition<E, T> buttonDef) {
        boolean isVisible = buttonDef.isAccessible(getParentEntity(), getSelectedItems()) && buttonDef.isVisible(getParentEntity(), getSelectedItems());
        boolean isEnabled = buttonDef.isEnabled(getParentEntity(), getSelectedItems());
        getView().updateActionButton(isVisible, isEnabled, buttonDef);
    }

    void registerSelectionChangeHandler(final ActionButtonDefinition<?, T> buttonDef) {
        // Update button definition whenever list model item selection changes
        addSelectionChangeListener((ev, sender, args) -> {
            // Update action button on item selection change
            buttonDef.update();
        });
    }

    @SuppressWarnings("unchecked")
    void addSelectionChangeListener(IEventListener<EventArgs> itemSelectionChangeHandler) {
        dataProvider.getModel().getSelectedItemChangedEvent().addListener(itemSelectionChangeHandler);
        dataProvider.getModel().getSelectedItemsChangedEvent().addListener(itemSelectionChangeHandler);
    }

    protected M getModel() {
        return getDataProvider().getModel();
    }

    public E getParentEntity() {
        return (E) getModel().getEntity();
    }

    @Override
    public List<T> getSelectedItems() {
        return getModel().getSelectedItems();
    }

    public void addSearchPanel(IsWidget searchPanel) {
        if (searchPanel != null) {
            getView().addSearchPanel(searchPanel);
        }
    }

    public void setFilterResultPanel(IsWidget resultPanel) {
        getView().setFilterResult(resultPanel);
    }

    public SearchableTableModelProvider<T, M> getDataProvider() {
        return dataProvider;
    }

    protected com.google.gwt.event.shared.EventBus getSharedEventBus() {
        return (com.google.gwt.event.shared.EventBus) getEventBus();
    }

    public void removeButton(ActionButtonDefinition<?, T> buttonDef) {
        ActionButton buttonToRemove = getView().getActionItems().get(buttonDef);
        if (buttonToRemove != null) {
            buttonToRemove.asWidget().removeFromParent();
        }
    }

    public List<ActionButtonDefinition<?, T>> getActionButtons() {
        return actionButtonDefinitions;
    }

    protected abstract void initializeButtons();

}
